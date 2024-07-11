package com.diode.lilypadoc.extension.apiDoc;

import com.diode.lilypadoc.extension.apiDoc.domain.CustomConfig;
import com.diode.lilypadoc.extension.apiDoc.domain.Doc;
import com.diode.lilypadoc.extension.apiDoc.domain.TableData;
import com.diode.lilypadoc.extension.apiDoc.processor.BadgePostProcessor;
import com.diode.lilypadoc.extension.apiDoc.processor.TablePostProcessor;
import com.diode.lilypadoc.standard.api.IHttpCall;
import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.plugin.FactoryPlugin;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.http.HttpCallContext;
import com.diode.lilypadoc.standard.utils.JsonTool;
import com.diode.lilypadoc.standard.utils.OSTool;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.renderer.HeaderIdGenerator;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class ApiDoc extends FactoryPlugin implements IHttpCall {

    private Parser parser;
    private TablePostProcessor tablePostProcessor;

    @Override
    public void customInit() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS,
                Arrays.asList(TocExtension.create(), TablesExtension.create(), StrikethroughExtension.create()));
        tablePostProcessor = TablePostProcessor.getInstance();
        // 自定义解析器配置
        parser = Parser.builder(options).postProcessorFactory(new TablePostProcessor.Factory()).build();
    }

    @Override
    public Result<List<ILilypadocComponent>> process(LilypadocContext lilypadocContext,
                                                     Map<String, List<ILilypadocComponent>> dependencies) {
        Doc doc = new Doc();

        File file = lilypadocContext.getDoc();
        try (FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8)) {
            Document document = parser.parseReader(fileReader);
            BadgePostProcessor.appendBadge(document, genModifiedTime(file));
            HeaderIdGenerator headerIdGenerator = new HeaderIdGenerator.Factory().create();
            headerIdGenerator.generateIds(document.getDocument());
            doc.setDocument(document);
        } catch (Exception e) {
            log.error("解析文档异常，path:{}", lilypadocContext.getDocRPath(), e);
            return Result.fail(StandardErrorCodes.SYS_ERROR.of("markdownConverter解析文档异常"));
        }
        return Result.ok(Collections.singletonList(doc));
    }


    @Override
    public Result<Map<String, String>> httpCall(Map<String, String> map, HttpCallContext httpCallContext) {
        String id = map.get("id");
        if(Objects.isNull(id)){
            return Result.fail(StandardErrorCodes.BIZ_ERROR.of("插件ApiDoc:前端调用接口但是缺少必备参数"));
        }
        TableData tableData = tablePostProcessor.getTableData(id);
        if(Objects.isNull(tableData)){
            return Result.fail(StandardErrorCodes.BIZ_ERROR.of("插件ApiDoc:找不到表格数据"));
        }
        Map<String, String> res = new HashMap<>();
        res.put("data", JsonTool.toJson(tableData));
        return Result.ok(res);
    }

    private String genModifiedTime(File file) throws IOException {
        Result<CustomConfig> result = getCustomConfig();
        CustomConfig customConfig = result.get();
        int timeType = customConfig.getTimeType();
        if (timeType == CustomConfig.GIT_TIME_TYPE) {
            return genGitTime(file);
        }
        return genLocalTime(file);
    }

    private String genLocalTime(File file) throws IOException {
        FileTime lastModifiedTime = Files.getLastModifiedTime(file.toPath());
        LocalDateTime localDateTime = lastModifiedTime
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.format(localDateTime);
    }

    private String genGitTime(File file) {
        try {
            // 设定文件路径和仓库路径
            String filePath = file.getPath();
            String repoPath = MPath.of(filePath).getParent().toString();
            String output = OSTool.execCommand(new File(repoPath), new String[]{"git", "-C", repoPath, "log", "-1", "--format=%cd", "--date=format:'%Y-%m-%d %H:%M:%S'", filePath});
            return output.replace("'", "");
        } catch (Exception e) {
            log.error("file:{}获取git时间错误", file.getPath(), e);
            throw e;
        }
    }


    public static void main(String[] args) {
        ApiDoc apiDoc = new ApiDoc();
        apiDoc.customInit();
        LilypadocContext context = new LilypadocContext();
        context.setDoc(new File("D:\\Projects\\code\\java\\lilypadoc-extension\\api-doc\\src\\main\\resources\\api.md"));
        Result<List<ILilypadocComponent>> process = apiDoc.process(context, new HashMap<>());
        Doc doc = (Doc) process.get().get(0);
        String string = doc.parse().parse();
        apiDoc.tablePostProcessor.getTableData("");
    }
}