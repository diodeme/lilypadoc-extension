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

public class CopyButton extends Node implements DelimitedNode {
    protected BasedSequence text = BasedSequence.NULL;

    @Override
    public BasedSequence getOpeningMarker() { return null; }

    @Override
    public void setOpeningMarker(BasedSequence basedSequence) {

    }

    @Override
    public BasedSequence getText() { return text; }

    @Override
    public void setText(BasedSequence text) { this.text = text; }

    @Override
    public BasedSequence getClosingMarker() { return null; }

    @Override
    public void setClosingMarker(BasedSequence basedSequence) {

    }

    @Override
    public @NotNull BasedSequence[] getSegments() { return new BasedSequence[0]; }

    static class CopyButtonRenderer implements NodeRenderer {

        @Override
        public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
            return new HashSet<>(Collections.singletonList(new NodeRenderingHandler<>(CopyButton.class, this::render)));
        }

        private void render(CopyButton node, NodeRendererContext context, HtmlWriter html) {
            String tag = "button";
            html.srcPos(node.getText()).withAttr()
                    .attr(Attribute.CLASS_ATTR, "btn btn-outline btn-sm").tag(tag)
                    .append("""
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none"
                            viewBox="0 0 24 24"
                            stroke-width="1.5" stroke="currentColor"
                            aria-hidden="true" class="w-4 h-4">
                            <path stroke-linecap="round" stroke-linejoin="round"
                            d="M15.75 17.25v3.375c0 .621-.504 1.125-1.125 1.125h-9.75a1.125 1.125 0 01-1.125-1.125V7.875c0-.621.504-1.125 1.125-1.125H6.75a9.06 9.06 0 011.5.124m7.5 10.376h3.375c.621 0 1.125-.504 1.125-1.125V11.25c0-4.46-3.243-8.161-7.5-8.876a9.06 9.06 0 00-1.5-.124H9.375c-.621 0-1.125.504-1.125 1.125v3.5m7.5 10.375H9.375a1.125 1.125 0 01-1.125-1.125v-9.25m12 6.625v-1.875a3.375 3.375 0 00-3.375-3.375h-1.5a1.125 1.125 0 01-1.125-1.125v-1.5a3.375 3.375 0 00-3.375-3.375H9.75">
                            </path>
                            </svg>""");
            context.renderChildren(node);
            html.tag("/" + tag);
        }

        public static class Factory implements NodeRendererFactory {

            @Override
            public @NotNull NodeRenderer apply(@NotNull DataHolder options) {
                return new CopyButtonRenderer();
            }
        }
    }
}