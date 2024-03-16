package com.diode.lilypadoc.extension.searcherIndex.domain;

public class Index {

    private CustomConfig customConfig;

    public Index(CustomConfig customConfig) { this.customConfig = customConfig; }

    public String parse() {
        String format = """
                <div class="hero min-h-screen items-start">
                    <div class="h-80"></div>
                    <div class="hero-content text-center">
                        <div class="max-w-lg">
                            <h1 class="text-3xl font-bold">%s</h1>
                            <p class="py-6">%s</p>
                        </div>
                    </div>
                </div>""";
        return String.format(format, customConfig.getIndexTitle(), customConfig.getIndexTip());
    }
}