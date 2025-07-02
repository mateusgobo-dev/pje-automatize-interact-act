package br.com.grerj.enumerators;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum ElementBy {
    ID("id",  0),
    XPATH("xpath",  1),
    CSS_SELETOR("css_selector", 2),
    CLASS_NAME("classname",  3),
    URL("url", 4),
    TEXTFIELD("text", 5);
    private final String name;
    private final int index;

    public String getName() {
        return name;
    }
    public int getIndex() {
        return index;
    }

    Optional<ElementBy> findByIndex(final int index) {
        return  Optional.of(Stream.of(ElementBy.values()).filter(e -> e.getIndex() == index).findFirst().orElse(null));
    }
}