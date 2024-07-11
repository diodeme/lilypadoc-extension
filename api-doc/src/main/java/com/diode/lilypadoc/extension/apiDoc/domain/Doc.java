package com.diode.lilypadoc.extension.apiDoc.domain;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.Text;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.html.Attribute;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Doc implements ILilypadocComponent {
    @Getter
    @Setter
    private Node document;

    private HtmlRenderer renderer;

    public Doc() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS,
                Arrays.asList(TocExtension.create(), TablesExtension.create(), StrikethroughExtension.create()));

        // 自定义渲染器配置
        renderer = HtmlRenderer.builder(options).nodeRendererFactory(new CopyButton.CopyButtonRenderer.Factory())
//                .nodeRendererFactory(new DetailTableRenderer.Factory()).attributeProviderFactory(new DetailTableAttributeProvider.Factory())
                .nodeRendererFactory(new TableRender.Factory())
                .nodeRendererFactory(new Badge.BadgeRender.Factory())
                .build();
    }

    @Override
    public Html parse() {
        Html html = new Html();
        if (Objects.isNull(document)) {
            return html;
        }

        String template =
                """
                        <div
                        class="prose max-w-none w-11/12 dark:prose-invert
                        prose-headings:m-2 prose-a:font-semibold prose-a:no-underline hover:prose-a:underline"
                        id="doc">
                        %s</div>
                        """;
        return html.element(new Text(String.format(template, renderer.render(document))));
    }

    static class TableRender implements NodeRenderer {

        @Override
        public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
            return new HashSet<>(Collections.singletonList(new NodeRenderingHandler<>(TableBlock.class, this::render)));
        }

        private void render(TableBlock node, NodeRendererContext context, HtmlWriter html) {
            String tag = "div";
            String id = node.getChars().unescape();
            html.srcPos(node.getChars())
                    .withAttr()
                    .attr(Attribute.CLASS_ATTR, "antd-table")
                    .attr(Attribute.CLASS_ATTR, "not-prose")
                    .attr(Attribute.ID_ATTR, id)
                    .tag(tag);
//            html.srcPos(node.getText())
//                    .append("""
//                            <tr>
//                                <td class="py-0">
//                                    <label class="swap">
//                                        <input type="checkbox" class="details-open"/>
//                                        <div class="swap-off btn btn-xs">展开</div>
//                                        <div class="swap-on btn btn-xs">收起</div>
//                                    </label>
//                                </td>
//                            </tr>
//                            </tbody>
//                            """)
//                    .withAttr()
//                    .attr(Attribute.CLASS_ATTR, "details border-l-2 border-accent")
//                    .attr(Attribute.STYLE_ATTR, "display: none")
//                    .tag(tag);
//            context.renderChildren(node);
            html.tag("/" + tag);
        }

        static class Factory implements NodeRendererFactory {

            @Override
            public @NotNull NodeRenderer apply(@NotNull DataHolder options) {
                return new TableRender();
            }
        }
    }

}