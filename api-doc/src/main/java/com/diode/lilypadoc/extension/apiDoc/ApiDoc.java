package com.diode.lilypadoc.extension.apiDoc;

import com.diode.lilypadoc.extension.apiDoc.domain.Doc;
import com.diode.lilypadoc.extension.apiDoc.domain.TableData;
import com.diode.lilypadoc.extension.apiDoc.parser.TableProcessor;
import com.diode.lilypadoc.standard.api.IHttpCall;
import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.plugin.FactoryPlugin;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.diode.lilypadoc.standard.domain.http.HttpCallContext;
import com.diode.lilypadoc.standard.utils.JsonTool;
import com.diode.lilypadoc.standard.utils.StringTool;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.renderer.HeaderIdGenerator;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.StringUtil;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class ApiDoc extends FactoryPlugin implements IHttpCall {

    private Parser parser;
    private TableProcessor tableProcessor;

    @Override
    public void customInit() {
        MutableDataSet options = new MutableDataSet();

        options.set(Parser.EXTENSIONS,
                Arrays.asList(TocExtension.create(), TablesExtension.create(), StrikethroughExtension.create()));
        tableProcessor = TableProcessor.getInstance();
        // 自定义解析器配置
        parser = Parser.builder(options).postProcessorFactory(new TableProcessor.Factory()).build();
    }

    @Override
    public Result<List<ILilypadocComponent>> process(LilypadocContext lilypadocContext,
                                                     Map<String, List<ILilypadocComponent>> dependencies) {
        Doc doc = new Doc();

        try (FileReader fileReader = new FileReader(lilypadocContext.getDoc(), StandardCharsets.UTF_8)) {
            Node document = parser.parseReader(fileReader);
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
        TableData tableData = tableProcessor.getTableData(id);
        if(Objects.isNull(tableData)){
            return Result.fail(StandardErrorCodes.BIZ_ERROR.of("插件ApiDoc:找不到表格数据"));
        }
        Map<String, String> res = new HashMap<>();
        res.put("data", JsonTool.toJson(tableData));
        return Result.ok(res);
    }

    public static void main(String[] args) {
        ApiDoc apiDoc = new ApiDoc();
        apiDoc.customInit();
        LilypadocContext context = new LilypadocContext();
        context.setDoc(new File("D:\\Projects\\code\\java\\lilypadoc-extension\\api-doc\\src\\main\\resources\\api.md"));
        Result<List<ILilypadocComponent>> process = apiDoc.process(context, new HashMap<>());
        Doc doc = (Doc) process.get().get(0);
        String string = doc.parse().parse();
        apiDoc.tableProcessor.getTableData("");
    }
}