package com.diode.lilypadoc.extension.sidebar;

import com.diode.lilypadoc.extension.sidebar.domain.CustomConfig;
import com.diode.lilypadoc.extension.sidebar.domain.Side;
import com.diode.lilypadoc.extension.sidebar.domain.Sides;
import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.plugin.FactoryPlugin;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.utils.FileTool;
import com.diode.lilypadoc.standard.utils.ListTool;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Sidebar extends FactoryPlugin {

    @Override
    public void customInit() {
    }

    @Override
    protected Result<List<ILilypadocComponent>> process(LilypadocContext lilypadocContext,
                                                        Map<String, List<ILilypadocComponent>> dependencies) {
        File lastCateFile = new File(lilypadocContext.getLastCategory().toString());
        MPath docRPath = lilypadocContext.getDocRPath();
        MPath lastCateRPath = docRPath.substring(0,
                docRPath.indexOf(lastCateFile.getName()) + lastCateFile.getName().length());
        Sides sides = genSides(lastCateFile, lastCateRPath, docRPath, lilypadocContext.getHtmlDocRPath());
        Result<CustomConfig> result = getCustomConfig(CustomConfig.class);
        if (result.isFailed()) {
            return Result.fail(result.errorCode());
        }
        CustomConfig customConfig = result.get();
        Integer titleLevel = customConfig.getCustomTitleLevel();
        Result<File> categoryDir = FileTool.getCategoryDir(lilypadocContext.getDoc(), lilypadocContext.getDocRootDir(),
                titleLevel);
        if(categoryDir.isFailed()){
            log.warn("doc:{} root:{} 指定的层级:{}不存在", lilypadocContext.getDocRPath(), lilypadocContext.getDocRootDir(), titleLevel);
            return Result.ok(Collections.singletonList(sides));
        }
        Side side = sides.getSide();
        side.setTitle(categoryDir.get().getName());
        return Result.ok(Collections.singletonList(sides));
    }

    public Sides genSides(File file, MPath path, MPath activePath, MPath htmlDocRPath) {
        Side side = genSide(file, path, 0);
        return new Sides(side, MPath.ofHtml(activePath), htmlDocRPath);
    }

    private Side genSide(File file, MPath path, int level) {
        Side side = new Side(file, level, path);
        File[] files = file.listFiles();
        if (Objects.isNull(files)) {
            if(!file.getName().endsWith(".md")){
                return null;
            }
            side.setPath(MPath.ofHtml(path));
            return side;
        }
        List<Side> sideBarList = new ArrayList<>();
        for (File childFile : files) {
            Side childSidebar = genSide(childFile, path.appendChild(childFile.getName()), level + 1);
            if (Objects.isNull(childSidebar)) {
                continue;
            }
            childSidebar.setParent(side);
            sideBarList.add(childSidebar);
        }
        List<Side> children = ListTool.safeArrayList(sideBarList).stream().sorted(Comparator.comparing(e1 ->
                e1.hasChild() ? "0" : "1" + e1.getTitle())).collect(Collectors.toList());
        side.setChildren(children);
        return side;
    }
}