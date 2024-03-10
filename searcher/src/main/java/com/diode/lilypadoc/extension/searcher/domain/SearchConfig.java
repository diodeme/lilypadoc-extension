package com.diode.lilypadoc.extension.searcher.domain;

import lombok.Data;

@Data
public class SearchConfig {

    private String target;
    private int categoryLevel;

    public SearchConfig(String target, int categoryLevel) {
        this.target = target;
        this.categoryLevel = categoryLevel;
    }

    public String parse() {
        return "<option value=\"" + categoryLevel + "\">" + target + "</option>";
    }
}