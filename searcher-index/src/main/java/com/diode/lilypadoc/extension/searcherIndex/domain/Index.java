package com.diode.lilypadoc.extension.searcherIndex.domain;

public class Index {

    private CustomConfig customConfig;

    public Index(CustomConfig customConfig) { this.customConfig = customConfig; }

    public String parse() {
        String format = "<div class=\"hero min-h-screen items-start\">\n"
                + "    <div class=\"h-80\"></div>\n"
                + "    <div class=\"hero-content text-center\">\n"
                + "        <div class=\"max-w-lg\">\n"
                + "            <h1 class=\"text-3xl font-bold\">%s</h1>\n"
                + "            <p class=\"py-6\">%s</p>\n"
                + "        </div>\n"
                + "    </div>\n"
                + "</div>";
        return String.format(format, customConfig.getIndexTitle(), customConfig.getIndexTip());
    }
}