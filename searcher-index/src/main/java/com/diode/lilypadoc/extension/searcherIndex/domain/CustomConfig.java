package com.diode.lilypadoc.extension.searcherIndex.domain;

import com.diode.lilypadoc.standard.api.BaseCustomConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomConfig extends BaseCustomConfig {

    private String notFoundTitle = "";
    private String notFoundTip = "";
    private String indexTitle = "";
    private String indexTip = "";

}