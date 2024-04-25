package com.diode.lilypadoc.extension.apiDoc.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TableData {

    private List<Column> columns;

    private List<Map<String, Object>> data;


    @Data
    public static class Column{
        private String title;
        private String dataIndex;
        private String key;
        private String width;
    }
}
