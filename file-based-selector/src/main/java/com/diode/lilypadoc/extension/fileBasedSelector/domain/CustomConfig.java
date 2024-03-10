package com.diode.lilypadoc.extension.fileBasedSelector.domain;

import com.diode.lilypadoc.standard.api.BaseCustomConfig;
import com.diode.lilypadoc.standard.utils.JsonTool;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomConfig extends BaseCustomConfig {

    private Map<String, Integer> fileCateMap;

    public static void main(String[] args) {
        CustomConfig customConfig = new CustomConfig();
        Map<String, Integer> map = new HashMap<>();
        map.put("化妆", 4);
        map.put("分支", 5);
        customConfig.setFileCateMap(map);
        System.out.println(JsonTool.toJson(customConfig));
    }
}