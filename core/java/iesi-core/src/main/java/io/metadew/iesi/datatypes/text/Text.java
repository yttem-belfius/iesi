package io.metadew.iesi.datatypes.text;

import io.metadew.iesi.datatypes.DataType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Text extends DataType {

    private final String string;

    @Override
    public String toString() {
        return string;
    }

}


