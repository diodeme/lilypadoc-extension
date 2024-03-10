package com.diode.lilypadoc.extension.docToc.domain;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.IHtmlElement;
import com.diode.lilypadoc.standard.domain.html.Tag;
import com.diode.lilypadoc.standard.domain.html.Text;
import com.vladsch.flexmark.ast.Heading;
import lombok.Data;

import java.util.Map;
import java.util.Objects;

@Data
public class Toc implements ILilypadocComponent {
    private String text;
    private String id;
    private int level;
    private Toc parent;
    private Toc firstChild;
    private Toc next;
    private Map<Integer, Toc> levelLast;

    public Toc(Heading heading) {
        this.text = heading.getAnchorRefText();
        this.level = heading.getLevel();
        this.id = heading.getAnchorRefId();
    }

    public Toc() {
        this.level = Integer.MAX_VALUE;
    }

    public boolean hasChild() {
        return Objects.nonNull(firstChild);
    }

    public boolean hasNext() {
        return Objects.nonNull(next);
    }

    public boolean hasParent() {
        return Objects.nonNull(parent);
    }

    @Override
    public Html parse() {
        Html recursion = recursion();
        String template =
                "<div class=\"fixed items-start justify-start overflow-auto thin-scrollbar\"\n"
                        + "     id=\"toc\">\n"
                        + "<ul>\n"
                        + "     class=\"menu menu-sm text-gray-500 border-l border-0.1\">\n"
                        + "%s"
                        + "     </ul>\n"
                        + "</div>";
        return new Html().element(new Text(String.format(template, recursion.parse())));
    }

    public Html recursion() {
        Html html = new Html();
        IHtmlElement title = new Text(text);
        Tag a = new Tag("a").property("href", "#" + id)
                .child(title);
        Tag li = new Tag("li").child(a);
        html.element(li);
        if (hasChild()) {
            li.child(new Tag("ul").child(firstChild.recursion().getElements().toArray(new IHtmlElement[0])));
        }
        if (hasNext()) {
            html.union(next.recursion());
        }
        return html;
    }

    @Override
    public String toString() {
        String temp = text;
        if (Objects.nonNull(firstChild)) {
            temp += firstChild.toString();
        }
        if (Objects.nonNull(next)) {
            temp += next.toString();
        }
        return temp;
    }
}