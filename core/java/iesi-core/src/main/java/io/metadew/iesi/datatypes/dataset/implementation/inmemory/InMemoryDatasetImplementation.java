package io.metadew.iesi.datatypes.dataset.implementation.inmemory;

import io.metadew.iesi.datatypes.array.Array;
import io.metadew.iesi.datatypes.dataset.DatasetKey;
import io.metadew.iesi.datatypes.dataset.implementation.DatasetImplementation;
import io.metadew.iesi.datatypes.dataset.implementation.DatasetImplementationKey;
import io.metadew.iesi.datatypes.dataset.implementation.DatasetImplementationKeyValue;
import io.metadew.iesi.datatypes.dataset.implementation.label.DatasetImplementationLabel;
import io.metadew.iesi.datatypes.text.Text;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class InMemoryDatasetImplementation extends DatasetImplementation {

    private Set<DatasetImplementationKeyValue> keyValues;

    @Builder
    public InMemoryDatasetImplementation(DatasetImplementationKey metadataKey, DatasetKey datasetKey, String name, Set<DatasetImplementationLabel> datasetImplementationLabels) {
        super(metadataKey, datasetKey, name, datasetImplementationLabels);
    }

    public String toString() {
        return "{{^dataset(" + new Text(getName()).toString() + ", " +
                new Array(getDatasetImplementationLabels().stream()
                        .map(DatasetImplementationLabel::getValue)
                        .map(Text::new)
                        .collect(Collectors.toList())).toString() + ", in_memory" + ")}}";
    }
}

