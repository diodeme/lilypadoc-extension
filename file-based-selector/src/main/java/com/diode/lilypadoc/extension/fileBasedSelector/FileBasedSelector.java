package com.diode.lilypadoc.extension.fileBasedSelector;

import com.diode.lilypadoc.extension.fileBasedSelector.domain.CustomConfig;
import com.diode.lilypadoc.extension.fileBasedSelector.domain.Option;
import com.diode.lilypadoc.extension.fileBasedSelector.domain.Selector;
import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.plugin.FactoryPlugin;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.utils.FileTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.io.File;
import java.util.*;

@Slf4j
public class FileBasedSelector extends FactoryPlugin {

    @Override
    public void customInit() {
    }

    @Override
    public Result<List<ILilypadocComponent>> process(LilypadocContext lilypadocContext,
                                                     Map<String, List<ILilypadocComponent>> dependencies) {
        Result<CustomConfig> result = getCustomConfig(CustomConfig.class);
        if (result.isFailed()) {
            return Result.fail(result.errorCode());
        }
        CustomConfig customConfig = result.get();
        Map<String, Integer> fileCateMap = customConfig.getFileCateMap();
        if (MapUtils.isEmpty(fileCateMap)) {
            log.warn("文件层级map为空");
            return Result.ok(new ArrayList<>());
        }
        List<ILilypadocComponent> componentList = new ArrayList<>(fileCateMap.size());
        fileCateMap.forEach((k, v) -> {
            Result<File> categoryDirResult = FileTool.getCategoryDir(lilypadocContext.getDoc(), lilypadocContext.getDocRootDir(), v);
            if (categoryDirResult.isFailed()) {
                log.warn("doc:{} root:{} 指定的层级:{} 不存在", lilypadocContext.getDocRPath(), lilypadocContext.getDocRootDir(), v);
                return;
            }
            File categoryDir = categoryDirResult.get();
            List<Option> options = getOptionList(categoryDir, lilypadocContext.getDocRPath(), lilypadocContext.getDocRootDir());
            Selector selector = new Selector();
            selector.setName(k);
            selector.setOptions(options);
            selector.setActiveOption(new Option(categoryDir.getName(), lilypadocContext.getDocRPath()));
            selector.setHtmlDocPath(lilypadocContext.getHtmlDocRPath());
            componentList.add(selector);
        });
        return Result.ok(componentList);
    }

    public List<Option> getOptionList(File optionDir, MPath docPath, MPath rootDir) {
        String curSelection = optionDir.getName();
        File parentDir = optionDir.getParentFile();
        File[] files = parentDir.listFiles();
        if (Objects.isNull(files)) {
            return new ArrayList<>();
        }
        List<Option> selectionList = new ArrayList<>();
        Arrays.stream(files).forEach(e -> {
            Option option = new Option();
            option.setVal(e.getName());
            // 本身直接赋值
            if (curSelection.equals(e.getName())) {
                option.setRef(MPath.ofHtml(docPath));
                selectionList.add(option);
                return;
            }
            // 先查包路径一致的，再查名称一致的
            File sameDoc = FileTool.findSameDoc(e,
                    docPath.substring(0, docPath.indexOf(curSelection) + curSelection.length()), true);
            if (Objects.nonNull(sameDoc)) {
                option.setRef(MPath.ofHtml(sameDoc.getPath()).remove(rootDir));
                selectionList.add(option);
                return;
            }
            // 没有历史文件，跳转目录下第一个文件 目录空不跳转
            File firstFile = FileTool.findFirstFile(e);
            if (Objects.isNull(firstFile)) {
                option.setRef(MPath.ofHtml(docPath));
            } else {
                option.setRef(MPath.ofHtml(firstFile.getPath()).remove(rootDir));
            }
            selectionList.add(option);
        });
        return selectionList;
    }
}