package com.diode.lilypadoc.extension.apiDoc.domain;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.Text;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
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

    @Override
    public Html parse() {
        Html html = new Html();
        if (Objects.isNull(document)) {
            return html;
        }
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS,
                Arrays.asList(TocExtension.create(), TablesExtension.create(), StrikethroughExtension.create()));

        // 自定义渲染器配置
        HtmlRenderer renderer = HtmlRenderer.builder(options).nodeRendererFactory(new CopyButtonRenderer.Factory())
                .build();
        String template = "<div\n"
                + "     class=\"prose max-w-none w-11/12 dark:prose-invert\n"
                + "              prose-headings:m-2 prose-a:font-semibold prose-a:no-underline hover:prose-a:underline\"\n"
                + "              id=\"doc\">\n"
                + "%s"
                + "</div>";
        return html.element(new Text(String.format(template, renderer.render(document))));
    }

    static class CopyButtonRenderer implements NodeRenderer {

        @Override
        public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
            return new HashSet<>(Collections.singletonList(new NodeRenderingHandler<>(CopyButton.class, this::render)));
        }

        private void render(CopyButton node, NodeRendererContext context, HtmlWriter html) {
            String tag = "button";
            HtmlWriter lineInfos = html.srcPos(node.getText()).withAttr()
                    .attr(Attribute.CLASS_ATTR, "btn btn-outline btn-sm").tag(tag)
                    .append("<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\"\n"
                            + "                        viewBox=\"0 0 24 24\"\n"
                            + "                        stroke-width=\"1.5\" stroke=\"currentColor\"\n"
                            + "                        aria-hidden=\"true\" class=\"w-4 h-4\">\n"
                            + "                        <path stroke-linecap=\"round\" stroke-linejoin=\"round\"\n"
                            + "                          d=\"M15.75 17.25v3.375c0 .621-.504 1.125-1.125 1.125h-9.75a1.125 1.125 0 01-1.125-1.125V7.875c0-.621.504-1.125 1.125-1.125H6.75a9.06 9.06 0 011.5.124m7.5 10.376h3.375c.621 0 1.125-.504 1.125-1.125V11.25c0-4.46-3.243-8.161-7.5-8.876a9.06 9.06 0 00-1.5-.124H9.375c-.621 0-1.125.504-1.125 1.125v3.5m7.5 10.375H9.375a1.125 1.125 0 01-1.125-1.125v-9.25m12 6.625v-1.875a3.375 3.375 0 00-3.375-3.375h-1.5a1.125 1.125 0 01-1.125-1.125v-1.5a3.375 3.375 0 00-3.375-3.375H9.75\">\n"
                            + "                        </path>\n"
                            + "                      </svg>");
            context.renderChildren(node);
            html.tag("/" + tag);
        }

        static class Factory implements NodeRendererFactory {

            @Override
            public @NotNull NodeRenderer apply(@NotNull DataHolder options) {
                return new CopyButtonRenderer();
            }
        }
    }
}