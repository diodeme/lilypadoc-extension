package com.diode.lilypadoc.extension.apiDoc.parser;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import org.jetbrains.annotations.NotNull;

public class HeadingPostProcessor extends NodePostProcessor {

    @Override
    public void process(@NotNull NodeTracker nodeTracker, @NotNull Node node) {

    }

    public static class Factory extends NodePostProcessorFactory {

        public Factory() {
            super(false);
            addNodes(Heading.class);
        }

        @Override
        public @NotNull NodePostProcessor apply(@NotNull Document document) {
            return TablePostProcessor.getInstance();
        }
    }
}
