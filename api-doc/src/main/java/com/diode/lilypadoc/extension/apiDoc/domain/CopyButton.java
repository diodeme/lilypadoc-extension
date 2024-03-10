package com.diode.lilypadoc.extension.apiDoc.domain;

import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class CopyButton extends Node implements DelimitedNode {
    protected BasedSequence text = BasedSequence.NULL;

    @Override
    public BasedSequence getOpeningMarker() { return null; }

    @Override
    public void setOpeningMarker(BasedSequence basedSequence) {

    }

    @Override
    public BasedSequence getText() { return text; }

    @Override
    public void setText(BasedSequence text) { this.text = text; }

    @Override
    public BasedSequence getClosingMarker() { return null; }

    @Override
    public void setClosingMarker(BasedSequence basedSequence) {

    }

    @Override
    public @NotNull BasedSequence[] getSegments() { return new BasedSequence[0]; }
}