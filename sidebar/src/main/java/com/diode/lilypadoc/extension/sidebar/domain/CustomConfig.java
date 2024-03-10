package com.diode.lilypadoc.extension.sidebar.domain;

import com.diode.lilypadoc.standard.api.BaseCustomConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomConfig extends BaseCustomConfig {
    private Integer customTitleLevel;
}