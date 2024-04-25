package com.diode.lilypadoc.extension.apiDoc.parser;

import com.diode.lilypadoc.extension.apiDoc.domain.DetailTable;
import com.diode.lilypadoc.extension.apiDoc.domain.TableData;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TableProcessor extends NodePostProcessor {

    @Getter
    private static final TableProcessor instance = new TableProcessor();

    private TableProcessor() {
        tableDataMap = new HashMap<>();
    }

    public Map<String, TableData> tableDataMap;

    public TableData getTableData(String id){
        return tableDataMap.get(id);
    }

    @Override
    public void process(@NotNull NodeTracker state, @NotNull Node node) {
//            if (node instanceof TableHead cell) {
//                CopyButton copyButton = new CopyButton();
//                cell.prependChild(copyButton);
//            }
        if(node instanceof TableBlock block){
            block.setChars(BasedSequence.of(UUID.randomUUID().toString()));
            handleTableBlock(block);
        }
    }

    private void handleTableBlock(TableBlock block){
        TableData tableData = new TableData();
        List<TableData.Column> columns = genColumn(block);
        List<Map<String, Object>> data = genData(block);
        tableData.setColumns(columns);
        tableData.setData(data);
        String uuid = UUID.randomUUID().toString();
        block.setChars(BasedSequence.of(uuid));
        tableDataMap.put(uuid, tableData);
    }

    private List<TableData.Column> genColumn(TableBlock block){
        Node firstChild = block.getFirstChild();
        TableHead tableHead = (TableHead) firstChild;
        assert tableHead != null;
        String headStr = tableHead.getChars().toString();
        TableRow row = (TableRow) tableHead.getFirstChild();
        assert row != null;
        List<TableData.Column> columnList = new ArrayList<>();
        TableCell cell = (TableCell) row.getFirstChild();
        while(Objects.nonNull(cell)){
            String s = cell.getText().unescape();
            TableData.Column column = new TableData.Column();
            column.setTitle(s);
            column.setKey(s);
            column.setDataIndex(s);
            columnList.add(column);
            cell = (TableCell) cell.getNext();
        }
        return columnList;
    }

    private List<Map<String,Object>> genData(TableBlock block){
        Node lastChild = block.getLastChild();
        TableBody tableBody = (TableBody) lastChild;
        List<Map<String, Object>> res = new ArrayList<>();
        assert tableBody != null;
        TableRow row = (TableRow) tableBody.getFirstChild();
        convertRow(0,res, new HashMap<>(), row);
        return res;
    }

    public void convertRow(int curLevel, List<Map<String, Object>> parent, Map<Integer, List<Map<String, Object>>> root, TableRow row){
        Map<String, Object> curRowData = handleSingleRow(row);
        parent.add(curRowData);
        TableRow next = (TableRow) row.getNext();
        if(Objects.isNull(next)){
            return;
        }
        TableCell tableCell = (TableCell) next.getFirstChild();
        assert tableCell != null;
        int nextLevel = calLevel(tableCell);
        if(nextLevel < curLevel){
            List<Map<String, Object>> maps = root.getOrDefault(nextLevel, new ArrayList<>());
            convertRow(nextLevel, maps, root, next);
            return;
        }
        if(nextLevel > curLevel) {
            root.put(curLevel, parent);
            List<Map<String, Object>> children = new ArrayList<>();
            curRowData.put("children", children);
            convertRow(nextLevel, children, root, next);
            return;
        }
        convertRow(nextLevel, parent, root, next);
    }

    private Map<String, Object> handleSingleRow(TableRow row){
        Map<String, Object> res = new HashMap<>();
        TableCell firstChild = (TableCell) row.getFirstChild();
        if(Objects.isNull(firstChild)){
            return res;
        }
        changeText(firstChild);
        TableCell next = firstChild;
        while(Objects.nonNull(next)){
            TableCell columnCell = getColumnCell(next);
            res.put(columnCell.getText().unescape(), next.getText().unescape());
            next = (TableCell) next.getNext();
        }
        res.put("key", UUID.randomUUID().toString());
        return res;
    }

    private int calLevel(TableCell cell){
        String unescape = cell.getText().unescape();
        int count = 0;

        for (int i = 0; i < unescape.length() - 1; i++) {
            if (unescape.charAt(i) == '-' && unescape.charAt(i + 1) == '-') {
                count++;
                i++; // 跳过下一个字符，因为它已经被计算过了
            } else {
                break; // 一旦遇到非--，跳出循环
            }
        }
        return count;
    }

    public TableCell getColumnCell(TableCell tableCell) {
        if (Objects.isNull(tableCell)) {
            return null;
        }
        int columnIndex = getColumnIndex(tableCell);
        return getColumnCell(tableCell, columnIndex);
    }

    private TableCell getColumnCell(TableCell tableCell, int index) {
        if (Objects.isNull(tableCell)) {
            return null;
        }
        Node parent = tableCell.getParent();
        while (!(parent instanceof TableBlock)) {
            parent = parent.getParent();
        }
        Node tableHead = parent.getFirstChild();
        Node tableRow = tableHead.getFirstChild();
        Node headCell = tableRow.getFirstChild();
        Node result = headCell;
        for (int i = index; i > 0; i--) {
            result = result.getNext();
        }
        return (TableCell) result;
    }

    private void handleTableCell(TableCell cell){
        genDetailTable(cell);
    }

    private void changeText(TableCell cell){

        cell.setText(BasedSequence.of(cell.getText().unescape().replaceFirst("^-+", "")));
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

    public static class Factory extends NodePostProcessorFactory {

        public Factory() {
            super(false);
            addNodes(TableCell.class, TableRow.class, TableBlock.class, TableHead.class, TableBody.class);
        }

        @Override
        public @NotNull NodePostProcessor apply(@NotNull Document document) {
            return TableProcessor.getInstance();
        }
    }
}