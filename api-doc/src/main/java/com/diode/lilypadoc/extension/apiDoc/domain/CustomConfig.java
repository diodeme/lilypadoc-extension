package com.diode.lilypadoc.extension.apiDoc.domain;

import com.diode.lilypadoc.standard.api.BaseCustomConfig;
import com.diode.lilypadoc.standard.api.ConfigDesc;
import com.diode.lilypadoc.standard.api.ConfigTitle;
import com.diode.lilypadoc.standard.utils.JsonTool;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomConfig extends BaseCustomConfig {

    public static final int LOCAL_TIME_TYPE = 0;
    public static final int GIT_TIME_TYPE = 1;

    @ConfigTitle("最后修改时间类型")
    @ConfigDesc("0:本地修改时间 1:git修改时间")
    private int timeType;
}