package com.diode.lilypadoc.extension.searcherIndex;

import com.diode.lilypadoc.extension.searcher.domain.CustomConfig;
import com.diode.lilypadoc.extension.searcherIndex.domain.Index;
import com.diode.lilypadoc.extension.sidebar.domain.Sides;
import com.diode.lilypadoc.standard.api.IHttpCall;
import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.ITemplate;
import com.diode.lilypadoc.standard.api.plugin.AbstractPlugin;
import com.diode.lilypadoc.standard.api.plugin.BehaviourPlugin;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.Pair;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.PluginMeta;
import com.diode.lilypadoc.standard.domain.Resource;
import com.diode.lilypadoc.standard.domain.event.PageSyncFinishEvent;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.Text;
import com.diode.lilypadoc.standard.domain.http.HttpCallContext;
import com.diode.lilypadoc.standard.utils.FileTool;
import com.diode.lilypadoc.standard.utils.ListTool;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SearcherIndex extends BehaviourPlugin<PageSyncFinishEvent> implements IHttpCall {

    private final static String SIDEBAR_NAME = "Sidebar";
    private final static String SEARCHER_NAME = "Searcher";
    private final static String INDEX_HTML = "index.html";

    /**
     * targetSearchCategoryName, mxPath
     */
    private final Map<String, String> maxPathMap;

    public SearcherIndex() {
        this.maxPathMap = new ConcurrentHashMap<>();
    }

    @Override
    protected ErrorCode process(PageSyncFinishEvent event) {
        log.info("SearcherIndex插件接收事件:{}", event);
        LilypadocContext lilypadocContext = event.getLilypadocContext();
        MPath htmlDocMPath = lilypadocContext.getHtmlDocRPath();
        ITemplate template = event.getTemplate();
        if (Objects.isNull(template)) {
            return StandardErrorCodes.BIZ_ERROR.of("SearcherIndex插件未获取到依赖的template数据");
        }
        MPath htmlRootPath = event.getHtmlRootPath();
        Map<AbstractPlugin, List<ILilypadocComponent>> componentMap = event.getComponentMap();
        Sides sides = null;
        List<ILilypadocComponent> searchBoxList = null;
        AbstractPlugin sideBarPlugin = null;
        AbstractPlugin searcherPlugin = null;
        for (Map.Entry<AbstractPlugin, List<ILilypadocComponent>> entry : componentMap.entrySet()) {
            AbstractPlugin k = entry.getKey();
            List<ILilypadocComponent> v = entry.getValue();
            if (SIDEBAR_NAME.equals(k.name())) {
                sideBarPlugin = k;
                if (Objects.nonNull(v) && !v.isEmpty()) {
                    sides = (Sides) v.get(0);
                }
            }
            if (SEARCHER_NAME.equals(k.name())) {
                searcherPlugin = k;
                searchBoxList = v;
            }
        }
        if (Objects.isNull(searcherPlugin)) {
            return StandardErrorCodes.BIZ_ERROR.of("SearcherIndex插件未获取到searcher组件");
        }
        if (Objects.isNull(sideBarPlugin) || Objects.isNull(sides)) {
            return StandardErrorCodes.BIZ_ERROR.of("SearcherIndex插件未获取到sidebar组件");
        }
        Result<CustomConfig> result = searcherPlugin.getCustomConfig(CustomConfig.class);
        if (result.isFailed()) {
            return result.errorCode();
        }
        CustomConfig customConfig = result.get();
        Map<String, Integer> fileCateMap = customConfig.getFileCateMap();
        if (Objects.isNull(fileCateMap) || fileCateMap.isEmpty()) {
            log.warn("Searcher插件文件层级map为空");
            return StandardErrorCodes.OK;
        }
        Sides newSides = new Sides(sides.getSide(), htmlDocMPath);
        List<Pair<AbstractPlugin, List<ILilypadocComponent>>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(searcherPlugin, searchBoxList));
        //此处未来可以扩展的插件
        Result<com.diode.lilypadoc.extension.searcherIndex.domain.CustomConfig> searcherIndexCustomConfig = getCustomConfig(
                com.diode.lilypadoc.extension.searcherIndex.domain.CustomConfig.class);
        if (searcherIndexCustomConfig.isFailed()) {
            return searcherIndexCustomConfig.errorCode();
        }
        Index index = new Index(searcherIndexCustomConfig.get());
        pairList.add(new Pair<>(sideBarPlugin, Collections.singletonList(newSides)));
        Result<Html> indexHtmlResult = getIndexHtml(pairList, template, index);
        if (indexHtmlResult.isFailed()) {
            log.error("SearcherIndex插件生成索引页模板失败");
            return indexHtmlResult.errorCode();
        }
        Html indexHtml = indexHtmlResult.get();
        String indexContent = indexHtml.parse();
        for (Map.Entry<String, Integer> entry : fileCateMap.entrySet()) {
            Integer v = entry.getValue();
            if (v <= 0) {
                continue;
            }
            ErrorCode errorCode = syncIndex(lilypadocContext, v, htmlRootPath, indexContent);
            if (StandardErrorCodes.OK.notEquals(errorCode)) {
                return errorCode;
            }
        }
        return StandardErrorCodes.OK;
    }

    public Result<Html> getIndexHtml(List<Pair<AbstractPlugin, List<ILilypadocComponent>>> componentList,
                                     ITemplate template, Index index) {
        ILilypadocComponent component = () -> new Html().element(new Text(index.parse()));
        PluginMeta tempMeta = new PluginMeta();
        tempMeta.setName("SearcherIndex");
        tempMeta.setDomains(meta().getDomains());
        tempMeta.setOrder(0);
        Map<PluginMeta, List<ILilypadocComponent>> newComponentMap = new HashMap<>();
        List<Resource> resourceList = new ArrayList<>();
        for (Pair<AbstractPlugin, List<ILilypadocComponent>> pair : ListTool.safeArrayList(componentList)) {
            newComponentMap.put(pair.getKey().meta(), pair.getValue());
            resourceList.add(pair.getKey().resource());
        }
        newComponentMap.put(tempMeta, Collections.singletonList(component));
        resourceList.add(resource());
        return template.inject(resourceList, newComponentMap);
    }

    public boolean judgeMaxPath(Integer targetSearchCateDepth, String targetSearchCateName,
                                LilypadocContext lilypadocContext) {
        Integer categoryDepth = lilypadocContext.getCategoryDepth();
        StringBuilder path = new StringBuilder();
        MPath category = lilypadocContext.getLastCategory();
        for (int i = categoryDepth; i > targetSearchCateDepth; i--) {
            String name = category.getName();
            path.insert(0, name);
            category = category.getParent();
        }
        String curPath = path.toString();
        String maxPath = maxPathMap.get(targetSearchCateName);
        if (Objects.isNull(maxPath) || maxPath.compareTo(curPath) < 0) {
            maxPathMap.put(targetSearchCateName, curPath);
            return true;
        }
        return false;
    }

    public synchronized ErrorCode syncIndex(LilypadocContext lilypadocContext, Integer targetCate,
                                            MPath htmlRootPath, String indexContent) {
        Result<File> categoryDirResult = FileTool.getCategoryDir(lilypadocContext.getDoc(), lilypadocContext.getDocRootDir(), targetCate);
        if (categoryDirResult.isFailed()) {
            log.warn("doc:{} root:{} 指定的层级{}不存在", lilypadocContext.getDocRPath(), lilypadocContext.getDocRootDir(), targetCate);
            return StandardErrorCodes.OK;
        }
        File categoryDir = categoryDirResult.get();
        //判断是否是最大的路径（按string排序，是的话才会去更新index页）
        boolean needRefresh = judgeMaxPath(targetCate, categoryDir.getName(), lilypadocContext);
        if (!needRefresh) {
            return StandardErrorCodes.OK;
        }
        MPath mPath = htmlRootPath.appendChild(lilypadocContext.getHtmlDocRPath()).appendChild(
                MPath.of(categoryDir.getPath()).remove(lilypadocContext.getDocRootDir()));
        String indexPath = mPath.appendChild(INDEX_HTML).toString();
        log.info("SearcherIndex插件开始同步索引页面，indexPath:{} curDoc:{}", indexPath, lilypadocContext.getDocRPath());
        ErrorCode indexErrorCode = FileTool.writeStringToFile(indexPath, indexContent);
        if (StandardErrorCodes.OK.notEquals(indexErrorCode)) {
            log.error("SearcherIndex插件生成索引页面失败");
            return indexErrorCode;
        }
        return StandardErrorCodes.OK;
    }

    @Override
    public Result<Map<String, String>> httpCall(Map<String, String> paramMap, HttpCallContext httpCallContext) {
        String target = paramMap.get("target");
        String level = paramMap.get("level");
        if (Objects.isNull(target) || Objects.isNull(level)) {
            return Result.fail(StandardErrorCodes.BIZ_ERROR.of("插件Searcher:前端调用接口但是缺少必备参数"));
        }
        Result<List<MPath>> result;
        if ("0".equals(level)) {
            result = FileTool.findTargetFile(
                    httpCallContext.getHtmlRootPath().appendChild(httpCallContext.getHtmlDocRPath()), MPath.ofHtml(target).getName());
        } else {
            result = FileTool.findTargetDir(
                    httpCallContext.getHtmlRootPath().appendChild(httpCallContext.getHtmlDocRPath()), target,
                    Integer.parseInt(level));
        }
        if (result.isFailed()) {
            return Result.fail(result.errorCode());
        }
        String targetPath = "";
        List<MPath> pathList = result.get();
        if (Objects.nonNull(pathList) && !pathList.isEmpty()) {
            MPath mPath = pathList.get(0);
            targetPath = httpCallContext.getHtmlDocRPath().appendChild(mPath).toString();
        }
        Map<String, String> resMap = new HashMap<>();
        resMap.put("targetPath", targetPath);
        Result<com.diode.lilypadoc.extension.searcherIndex.domain.CustomConfig> searcherIndexCustomConfig = getCustomConfig(
                com.diode.lilypadoc.extension.searcherIndex.domain.CustomConfig.class);
        if (searcherIndexCustomConfig.isFailed()) {
            return Result.fail(searcherIndexCustomConfig.errorCode());
        }
        com.diode.lilypadoc.extension.searcherIndex.domain.CustomConfig customConfig = searcherIndexCustomConfig.get();
        resMap.put("notFoundTitle", customConfig.getNotFoundTitle());
        resMap.put("notFoundTip", customConfig.getNotFoundTip());
        return Result.ok(resMap);
    }

    @Override
    public void customInit() {

    }
}