package com.diode.lilypadoc.extension.apiDoc;

import com.diode.lilypadoc.extension.apiDoc.domain.CopyButton;
import com.diode.lilypadoc.extension.apiDoc.domain.DetailTable;
import com.diode.lilypadoc.extension.apiDoc.domain.Doc;
import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.plugin.FactoryPlugin;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.vladsch.flexmark.ast.Text;
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
import com.vladsch.flexmark.util.sequence.BasedSequence;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.util.*;

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
            if (node instanceof TableCell cell){
                handleTableCell(cell);
            }
        }

        private void handleTableCell(TableCell cell){
            genDetailTable(cell);
        }

        private void changeText(TableCell cell){
            Text text = (Text) cell.getFirstChild();
            if(Objects.isNull(text)){
                return;
            }
            text.setChars(BasedSequence.of(text.getChars().unescape().replaceFirst("^-+", "")));
        }

        private void genDetailTable(TableCell cell){
            if(getColumnIndex(cell) != 0 || !isDetail(cell)){
                return;
            }
            Node parent = cell.getParent();
            assert parent != null;
            Node previous = parent.getPrevious();
            if(previous == null){
                return;
            }
            TableCell firstChild = (TableCell) previous.getFirstChild();
            if(firstChild == null ){
                return;
            }
            if(isDetail(firstChild)){
                DetailTable detailTable = (DetailTable) previous.getPrevious();
                if(Objects.isNull(detailTable)){
                    return;
                }
                changeText(firstChild);
                detailTable.appendChild(previous);
                Node next = parent.getNext();
                if (next == null || next.getFirstChild() == null || !isDetail((TableCell) next.getFirstChild())) {
                    changeText(cell);
                    detailTable.appendChild(parent);
                }
                return;
            }
            DetailTable detailTable = new DetailTable();
            previous.insertAfter(detailTable);
        }
        private int getColumnIndex(TableCell tableCell){
            if(Objects.isNull(tableCell)){
                return -1;
            }
            int i =0;
            Node previous = tableCell.getPrevious();
            while (previous !=null){
                i +=1;
                previous = previous.getPrevious();
            }
            return i;
        }

        private boolean isDetail(TableCell cell){
            return !cell.getText().unescape().matches("^-+$") && cell.getText().unescape().startsWith("--");
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

    public static void main(String[] args) {
        ApiDoc apiDoc = new ApiDoc();
        LilypadocContext context = new LilypadocContext();
        context.setDoc(new File("D:\\Projects\\code\\java\\lilypadoc-extension\\api-doc\\src\\main\\resources\\api.md"));
        Result<List<ILilypadocComponent>> process = apiDoc.process(context, new HashMap<>());
        Doc doc = (Doc) process.get().get(0);
        String string = doc.parse().parse();

    }
}