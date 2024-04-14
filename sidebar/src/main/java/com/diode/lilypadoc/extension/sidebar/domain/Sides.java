package com.diode.lilypadoc.extension.sidebar.domain;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.Text;
import lombok.Getter;

import java.util.Objects;

public class Sides implements ILilypadocComponent {

    @Getter
    private final Side side;

    private MPath activePath;

    private final MPath htmlDocRPath;

    public Sides(Side side, MPath htmlDocRPath) {
        this.side = side;
        this.htmlDocRPath = htmlDocRPath;
    }

    public Sides(Side side, MPath activePath, MPath htmlDocRPath) {
        this.side = side;
        this.activePath = activePath;
        this.htmlDocRPath = htmlDocRPath;
    }

    @Override
    public Html parse() {
        String template = """
                <aside id="sidebar"
                class="sticky border-r border-0.1 w-1/5 font-medium">
                <div class="flex flex-col items-center pl-2 pt-4 pb-5 md:flex-row sidebar_header_icon">
                </div>
                <ul class="menu menu-md rounded-box text-base overflow-hidden hover:overflow-auto thin-scrollbar"
                id="sidebar_menu">
                %s
                </ul>
                </aside>""";
        Html html = Objects.isNull(side) ? new Html() : side.html(activePath, true, htmlDocRPath);
        return new Html().element(new Text(String.format(template, html.parse())));
    }
}