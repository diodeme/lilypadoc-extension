package com.diode.lilypadoc.extension.searcher.domain;

import com.diode.lilypadoc.standard.api.BaseCustomConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomConfig extends BaseCustomConfig {

    private Map<String, Integer> fileCateMap;
}