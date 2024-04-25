package com.diode.lilypadoc.extension.apiDoc.domain;

import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.html.Attribute;
import com.vladsch.flexmark.util.html.MutableAttributes;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DetailTable extends Node implements DelimitedNode {
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

    static class DetailTableRenderer implements NodeRenderer {

        @Override
        public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
            return new HashSet<>(Collections.singletonList(new NodeRenderingHandler<>(DetailTable.class, this::render)));
        }

        private void render(DetailTable node, NodeRendererContext context, HtmlWriter html) {
            String tag = "tbody";
            html.srcPos(node.getText())
                    .append("""
                            <tr>
                                <td class="py-0">
                                    <label class="swap">
                                        <input type="checkbox" class="details-open"/>
                                        <div class="swap-off btn btn-xs">展开</div>
                                        <div class="swap-on btn btn-xs">收起</div>
                                    </label>
                                </td>
                            </tr>
                            </tbody>
                            """)
                    .withAttr()
                    .attr(Attribute.CLASS_ATTR, "details border-l-2 border-accent")
                    .attr(Attribute.STYLE_ATTR, "display: none")
                    .tag(tag);
            context.renderChildren(node);
            html.tag("/" + tag);
            html.append("<tbody>");
        }

        static class Factory implements NodeRendererFactory {

            @Override
            public @NotNull NodeRenderer apply(@NotNull DataHolder options) {
                return new DetailTableRenderer();
            }
        }
    }

    static class DetailTableAttributeProvider implements AttributeProvider {

        @Override
        public void setAttributes(@NotNull Node node, @NotNull AttributablePart part,
                                  @NotNull MutableAttributes attributes) {

            if (node.getNext() instanceof DetailTable) {
                attributes.addValue("class", "details-parent border-0");
            }
            if (node.getPrevious() instanceof DetailTable) {
                attributes.addValue("class", "border-t");
            }
        }

        static class Factory implements AttributeProviderFactory {

            @Override
            public @Nullable Set<Class<?>> getAfterDependents() {
                return null;
            }

            @Override
            public @Nullable Set<Class<?>> getBeforeDependents() {
                return null;
            }

            @Override
            public boolean affectsGlobalScope() {
                return false;
            }

            @Override
            public @NotNull AttributeProvider apply(@NotNull LinkResolverContext linkResolverContext) {
                return new DetailTableAttributeProvider();
            }
        }
    }
}

