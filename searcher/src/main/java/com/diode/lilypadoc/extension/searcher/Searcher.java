package com.diode.lilypadoc.extension.searcher;

import com.diode.lilypadoc.extension.searcher.domain.CustomConfig;
import com.diode.lilypadoc.extension.searcher.domain.SearchConfig;
import com.diode.lilypadoc.extension.searcher.domain.SearcherBox;
import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.plugin.FactoryPlugin;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Searcher extends FactoryPlugin {

    @Override
    protected Result<List<ILilypadocComponent>> process(LilypadocContext lilypadocContext,
                                                        Map<String, List<ILilypadocComponent>> dependencies) {
        Result<CustomConfig> result = getCustomConfig();
        if (result.isFailed()) {
            return Result.fail(result.errorCode());
        }
        CustomConfig customConfig = result.get();
        Map<String, Integer> fileCateMap = customConfig.getFileCateMap();
        if (Objects.isNull(fileCateMap) || fileCateMap.isEmpty()) {
            log.warn("SearcherIndex插件文件层级map为空");
            return Result.ok(Collections.emptyList());
        }
        List<SearchConfig> searchConfigList = new ArrayList<>();
        customConfig.getFileCateMap().forEach((k, v) -> {
            searchConfigList.add(new SearchConfig(k, v));
        });
        SearcherBox searcherBox = new SearcherBox(searchConfigList, lilypadocContext.getHtmlDocRPath());
        return Result.ok(Collections.singletonList(searcherBox));
    }

    @Override
    public void customInit() {

    }
}