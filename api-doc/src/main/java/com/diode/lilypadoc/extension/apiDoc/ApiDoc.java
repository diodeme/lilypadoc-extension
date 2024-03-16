package com.diode.lilypadoc.extension.apiDoc;

import com.diode.lilypadoc.extension.apiDoc.domain.CopyButton;
import com.diode.lilypadoc.extension.apiDoc.domain.Doc;
import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.plugin.FactoryPlugin;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.renderer.HeaderIdGenerator;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class ApiDoc extends FactoryPlugin {

    @Override
    public void customInit() {

    }

    @Override
    public Result<List<ILilypadocComponent>> process(LilypadocContext lilypadocContext,
                                                     Map<String, List<ILilypadocComponent>> dependencies) {
        Doc doc = new Doc();

        MutableDataSet options = new MutableDataSet();

        options.set(Parser.EXTENSIONS,
                Arrays.asList(TocExtension.create(), TablesExtension.create(), StrikethroughExtension.create()));

        // 自定义解析器配置
        Parser parser = Parser.builder(options).postProcessorFactory(new TablePostProcessor.Factory()).build();

        try (FileReader fileReader = new FileReader(lilypadocContext.getDoc())) {
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

    static class TablePostProcessor extends NodePostProcessor {

        @Override
        public void process(@NotNull NodeTracker state, @NotNull Node node) {
            if (node instanceof TableHead cell) {
                CopyButton copyButton = new CopyButton();
                cell.prependChild(copyButton);
            }
        }

        static class Factory extends NodePostProcessorFactory {

            public Factory() {
                super(false);
                addNodes(TableCell.class, TableRow.class, TableBlock.class, TableHead.class, TableBody.class);
            }

            @Override
            public @NotNull NodePostProcessor apply(@NotNull Document document) {
                return new TablePostProcessor();
            }
        }
    }
}