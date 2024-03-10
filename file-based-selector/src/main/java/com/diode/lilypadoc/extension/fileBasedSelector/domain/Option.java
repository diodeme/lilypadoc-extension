package com.diode.lilypadoc.extension.fileBasedSelector.domain;

import com.diode.lilypadoc.standard.domain.MPath;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Option {

    private String val;
    private MPath ref;

    public String parse(Option activeOption, MPath prefix) {
        boolean active = isActive(activeOption);
        return active ? "<li class=\"disabled\"><a>" + val + "</a></li>"
                : "<li><a href=\"" + MPath.ofHtml(prefix.appendChild(ref)) + "\">" + val + "</a></li>";
    }

    private boolean isActive(Option activeOption) {
        if (Objects.isNull(activeOption)) {
            return false;
        }
        return Objects.equals(val, activeOption.getVal());
    }
}