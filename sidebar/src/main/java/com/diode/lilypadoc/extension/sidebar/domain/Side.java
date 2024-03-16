package com.diode.lilypadoc.extension.sidebar.domain;

import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.Tag;
import com.diode.lilypadoc.standard.domain.html.Text;
import com.diode.lilypadoc.standard.utils.FileTool;
import com.diode.lilypadoc.standard.utils.ListTool;
import lombok.Data;

import java.io.File;
import java.util.List;
import java.util.Objects;

@Data
public class Side {

    private String title;

    private List<Side> children;

    private Side parent;

    private int level;

    private MPath path;

    public Side(File file) {
        this.title = FileTool.removeExtension(file.getName());
    }

    public Side(File file, int level, MPath path) {
        this(file);
        this.level = level;
        this.path = path;
    }

    public boolean hasChild() {
        return Objects.nonNull(children) && !children.isEmpty();
    }

    public Html html(MPath activePath, boolean isRoot, MPath htmlDocRPath) {
        String formatParent = "<li>%s</li>";
        String content;
        if (hasChild()) {
            String formatDir;
            if (isRoot) {
                formatDir = """
                        <summary class="font-semibold text-base">%s</summary>
                        <ul>
                        %s</ul>
                        """;
            } else {
                formatDir = "<details " + (isActive(activePath) ? "open" : "close") + ">\n"
                        + "<summary " + (isActive(activePath) ? "class=\"sidebar-active\"" : "")
                        + ">%s</summary>\n"
                        + "<ul>\n"
                        + "%s"
                        + "</ul>\n"
                        + "</details>";
            }
            Html childHtml = new Html();
            for (Side child : children) {
                childHtml.union(child.html(activePath, false, htmlDocRPath));
            }
            content = String.format(formatDir, title, childHtml.parse());
        } else {
            content =
                    "<a " + (isActive(activePath) && !isRoot ? "class=\"sidebar-active\"" : "") + " href=\"" +
                            MPath.ofHtml(htmlDocRPath.appendChild(path)) + "\">" + title + "</a>";
        }
        String body = String.format(formatParent, content);
        return new Html().element(new Tag("li").child(new Text(body)));
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(title);
        for (Side sideBar : ListTool.safeArrayList(children)) {
            str.append(",").append(sideBar.toString());
        }
        return str.toString();
    }

    public boolean isActive(MPath activePath) {
        if (Objects.isNull(activePath)) {
            return false;
        }
        return activePath.contains(path);
    }
}