package com.diode.lilypadoc.extension.apiDoc.domain;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.html.Attribute;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Badge extends Node implements DelimitedNode {
    protected BasedSequence text = BasedSequence.NULL;

    @Override
    public BasedSequence getOpeningMarker() {
        return null;
    }

    @Override
    public void setOpeningMarker(BasedSequence basedSequence) {

    }

    @Override
    public BasedSequence getText() {
        return text;
    }

    @Override
    public void setText(BasedSequence text) {
        this.text = text;
    }

    @Override
    public BasedSequence getClosingMarker() {
        return null;
    }

    @Override
    public void setClosingMarker(BasedSequence basedSequence) {

    }

    @Override
    public @NotNull BasedSequence[] getSegments() {
        return new BasedSequence[0];
    }

    static class BadgeRender implements NodeRenderer {

        @Override
        public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
            return new HashSet<>(Collections.singletonList(new NodeRenderingHandler<>(Badge.class, this::render)));
        }

        private void render(Badge node, NodeRendererContext context, HtmlWriter html) {
            String tag = "div";
            html.srcPos(node.getText()).withAttr()
                    .attr(Attribute.CLASS_ATTR, "badge badge-ghost hover:border-black hover:border-2").tag(tag)
                    .append("""
                              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 1024 1024"
                                 class="inline-block w-4 h-4 stroke-current mr-3">
                                <path d="M618.666667 192l213.333333 213.333333 89.002667-89.002666a85.333333 85.333333 0 0 0 0-120.661334l-92.672-92.672a85.333333 85.333333 0 0 0-120.661334 0L618.666667 192zM95.872 901.76l71.765333-251.136a42.666667 42.666667 0 0 1 10.837334-18.432L554.666667 256l213.333333 213.333333-376.192 376.192a42.666667 42.666667 0 0 1-18.432 10.837334l-251.136 71.765333a21.333333 21.333333 0 0 1-26.368-26.368z"
                                      fill="#000000" p-id="2387"></path>
                            </svg>
                            """ + node.getText().unescape());
            context.renderChildren(node);
            html.tag("/" + tag);
        }

        public static class Factory implements NodeRendererFactory {

            @Override
            public @NotNull NodeRenderer apply(@NotNull DataHolder options) {
                return new BadgeRender();
            }
        }
    }
}