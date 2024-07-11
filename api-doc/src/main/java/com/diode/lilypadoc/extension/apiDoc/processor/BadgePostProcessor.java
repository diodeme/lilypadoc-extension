package com.diode.lilypadoc.extension.apiDoc.processor;

import com.diode.lilypadoc.extension.apiDoc.domain.Badge;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.Objects;

public class BadgePostProcessor {

    public static void appendBadge(Document doc, String value){
        Node firstChild = doc.getFirstChild();
        Badge badge = new Badge();
        badge.setText(BasedSequence.of(value));
        if(Objects.isNull(firstChild)){
            doc.appendChild(badge);
            return;
        }
        if(firstChild instanceof Heading){
            firstChild.insertAfter(badge);
        }else {
            firstChild.insertBefore(badge);
        }
    }
}
