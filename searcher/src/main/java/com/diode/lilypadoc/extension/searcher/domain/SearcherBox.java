package com.diode.lilypadoc.extension.searcher.domain;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.Text;
import com.diode.lilypadoc.standard.utils.ListTool;

import java.util.List;

public class SearcherBox implements ILilypadocComponent {

    private List<SearchConfig> searchConfigList;
    private MPath htmlDocRPath;

    public SearcherBox(List<SearchConfig> searchConfigList, MPath htmlDocRPath) {
        this.searchConfigList = searchConfigList;
        this.htmlDocRPath = htmlDocRPath;
    }

    @Override
    public Html parse() {
        String format = "<label\n"
                + "class=\"input input-bordered flex items-center justify-center gap-1 min-h-11 px-0\">\n"
                + "<select class=\"select select-bordered border-y-0 border-l-0 border-r focus:outline-none bg-inherit select-sm h-full rounded-r-none\" id='searchTypeSelect'>\n"
                + "%s"
                + "</select>\n"
                + "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 16 16\"\n"
                + "fill=\"currentColor\" class=\"w-4 h-4 opacity-70\"><path\n"
                + "fill-rule=\"evenodd\"\n"
                + "d=\"M9.965 11.026a5 5 0 1 1 1.06-1.06l2.755 2.754a.75.75 0 1 1-1.06 1.06l-2.755-2.754ZM10.5 7a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0Z\"\n"
                + "clip-rule=\"evenodd\" /></svg>\n"
                + "<input type=\"text\" class=\"grow bg-inherit\" id=\"searcher-input\"\n"
                + "placeholder=\"\" />\n"
                + "</label>";
        StringBuilder sb = new StringBuilder();
        for (SearchConfig searchConfig : ListTool.safeArrayList(searchConfigList)) {
            sb.append(searchConfig.parse());
        }
        String content = String.format(format, sb);
        return new Html().element(new Text(content));
    }
}