package com.diode.lilypadoc.extension.docToc;

import com.diode.lilypadoc.extension.apiDoc.domain.Doc;
import com.diode.lilypadoc.extension.docToc.domain.Toc;
import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.plugin.FactoryPlugin;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.html.renderer.HeaderIdGenerator;
import com.vladsch.flexmark.html.renderer.HeaderIdGenerator.Factory;
import com.vladsch.flexmark.util.ast.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * @author diode
 * @createDate 2024/1/17
 */
@Slf4j
public class DocToc extends FactoryPlugin {

    @Override
    public void customInit() {

    }

    @Override
    protected Result<List<ILilypadocComponent>> process(LilypadocContext lilypadocContext, Map<String, List<ILilypadocComponent>> dependencies) {
        List<ILilypadocComponent> componentList = dependencies.get("ApiDoc");
        if(Objects.isNull(componentList) || !(componentList.get(0) instanceof Doc)){
            return Result.fail(StandardErrorCodes.BIZ_ERROR.of("DocToc插件仅接收ApiDoc组件"));
        }
        ILilypadocComponent component = componentList.get(0);
        Toc toc = new Toc();
        Node document = ((Doc) component).getDocument();
        genToc(document, toc, new HashMap<>());
        Toc next = toc.getNext();
        List<ILilypadocComponent> res;
        if(Objects.isNull(next)){
            res = new ArrayList<>();
        } else {
            res = Collections.singletonList(next);
        }
        return Result.ok(res);
    }

    private void genToc(Node node, Toc toc, Map<Integer, Toc> levelLast) {
        if (Objects.isNull(node)) {
            return;
        }
        if (!node.hasChildren() && Objects.nonNull(node.getNext())) {
            return;
        }
        if (node instanceof Heading heading) {
            Toc tempToc = new Toc(heading);
            int headingLevel = heading.getLevel();
            int tocLevel = toc.getLevel();
            if (headingLevel <= tocLevel) {
                Toc levelLastToc = levelLast.get(headingLevel);
                if (Objects.nonNull(levelLastToc)) {
                    toc = levelLastToc;
                }
                toc.setNext(tempToc);
                if (toc.hasParent() && toc.getParent().getLevel() < headingLevel) {
                    tempToc.setParent(toc.getParent());
                }
                levelLast.put(headingLevel, tempToc);
            } else {
                tempToc.setParent(toc);
                toc.setFirstChild(tempToc);
                levelLast.put(tempToc.getLevel(), tempToc);
            }
            toc = tempToc;
        }
        genToc(node.getFirstChild(), toc, levelLast);
        genToc(node.getNext(), toc, levelLast);
    }
}