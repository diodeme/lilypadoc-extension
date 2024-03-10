package com.diode.lilypadoc.extension.fileBasedSelector.domain;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.Text;
import com.diode.lilypadoc.standard.utils.ListTool;
import lombok.Setter;

import java.util.List;

public class Selector implements ILilypadocComponent {

    @Setter
    private String name;

    @Setter
    private List<Option> options;

    @Setter
    private Option activeOption;

    @Setter
    private MPath htmlDocPath;

    @Override
    public Html parse() {
        String parentFormat = "<li>\n"
                + "<details>\n"
                + "<summary>\n"
                + activeOption.getVal()
                + "</summary>\n"
                + "<ul class=\"bg-base-100 rounded-t-none\">\n"
                + "%s"
                + "</ul>"
                + "</details>\n"
                + "</li>";

        StringBuilder sb = new StringBuilder();
        for (Option option : ListTool.safeArrayList(options)) {
            sb.append(option.parse(activeOption, htmlDocPath));
        }

        return new Html().element(new Text(String.format(parentFormat, sb)));
    }
}