package io.metadew.iesi.server.rest.dataset;

import io.metadew.iesi.common.configuration.metadata.repository.MetadataRepositoryConfiguration;
import io.metadew.iesi.datatypes.dataset.Dataset;
import io.metadew.iesi.datatypes.dataset.DatasetConfiguration;
import io.metadew.iesi.datatypes.dataset.DatasetKey;
import io.metadew.iesi.datatypes.dataset.implementation.DatasetImplementation;
import io.metadew.iesi.datatypes.dataset.implementation.DatasetImplementationKey;
import io.metadew.iesi.datatypes.dataset.implementation.inmemory.InMemoryDatasetImplementation;
import io.metadew.iesi.datatypes.dataset.implementation.inmemory.InMemoryDatasetImplementationKeyValue;
import io.metadew.iesi.datatypes.dataset.implementation.inmemory.InMemoryDatasetImplementationKeyValueKey;
import io.metadew.iesi.datatypes.dataset.implementation.label.DatasetImplementationLabel;
import io.metadew.iesi.datatypes.dataset.implementation.label.DatasetImplementationLabelKey;
import io.metadew.iesi.server.rest.Application;
import io.metadew.iesi.server.rest.configuration.TestConfiguration;
import io.metadew.iesi.server.rest.dataset.implementation.DatasetImplementationDto;
import io.metadew.iesi.server.rest.dataset.implementation.DatasetImplementationLabelDto;
import io.metadew.iesi.server.rest.dataset.implementation.inmemory.InMemoryDatasetImplementationDto;
import io.metadew.iesi.server.rest.dataset.implementation.inmemory.InMemoryDatasetImplementationKeyValueDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, properties = {"spring.main.allow-bean-definition-overriding=true"})
@ContextConfiguration(classes = TestConfiguration.class)
@ActiveProfiles("test")
@DirtiesContext
class DatasetDtoRepositoryTest {

    @Autowired
    private MetadataRepositoryConfiguration metadataRepositoryConfiguration;

    @Autowired
    private DatasetConfiguration datasetConfiguration;

    @Autowired
    private IDatasetDtoRepository datasetDtoRepository;

    @AfterEach
    void cleanup() {
        metadataRepositoryConfiguration.clearAllTables();
    }

    @Test
    void getAllPaginatedNoImplementationsLinkedToDataset() {
        Dataset dataset = Dataset.builder()
                .metadataKey(new DatasetKey(UUID.randomUUID()))
                .name("dataset")
                .datasetImplementations(new HashSet<>())
                .build();
        DatasetDto datasetDto = DatasetDto.builder()
                .uuid(dataset.getMetadataKey().getUuid())
                .name("dataset")
                .implementations(new HashSet<>())
                .build();
        datasetConfiguration.insert(dataset);
        Pageable pageable = PageRequest.of(0, 2);
        assertThat(
                datasetDtoRepository.fetchAll(pageable, new HashSet<>()))
                .containsOnly(datasetDto);
    }

    @Test
    void getAllPaginatedWithImplementationsLinkedToDataset() {
        Map<String, Object> dataset1Info = generateDataset(0, 2, 2, 2);
        Dataset dataset = (Dataset) dataset1Info.get("dataset");
        datasetConfiguration.insert(dataset);

        Pageable pageable = PageRequest.of(0, 2);
        assertThat(datasetDtoRepository.fetchAll(pageable, new HashSet<>()))
                .containsOnly((DatasetDto) dataset1Info.get("datasetDto"));
    }

    @Test
    void getDatasetImplementationByUuid() {
        Map<String, Object> dataset1Info = generateDataset(0, 2, 2, 2);
        Dataset dataset = (Dataset) dataset1Info.get("dataset");
        datasetConfiguration.insert(dataset);

        assertThat(datasetDtoRepository.fetchImplementationByUuid(((DatasetImplementation) dataset1Info.get("datasetImplementation1")).getMetadataKey().getUuid()))
                .hasValue((DatasetImplementationDto) dataset1Info.get("datasetImplementationDto1"));
    }

    @Test
    void getDatasetImplementationByUuidNotFound() {
        Map<String, Object> dataset1Info = generateDataset(0, 2, 2, 2);
        Dataset dataset = (Dataset) dataset1Info.get("dataset");
        datasetConfiguration.insert(dataset);

        assertThat(datasetDtoRepository.fetchImplementationByUuid(UUID.randomUUID()))
                .isEmpty();
    }

    @Test
    void getDatasetImplementationsByDatasetUuid() {
        Map<String, Object> dataset1Info = generateDataset(0, 2, 2, 2);
        Dataset dataset = (Dataset) dataset1Info.get("dataset");
        datasetConfiguration.insert(dataset);

        assertThat(datasetDtoRepository.fetchImplementationsByDatasetUuid(((Dataset) dataset1Info.get("dataset")).getMetadataKey().getUuid()))
                .containsOnly((DatasetImplementationDto) dataset1Info.get("datasetImplementationDto1"),
                        (DatasetImplementationDto) dataset1Info.get("datasetImplementationDto0"));
    }

    @Test
    void getDatasetImplementationsByDatasetUuidNotExisting() {
        Map<String, Object> dataset1Info = generateDataset(0, 2, 2, 2);
        Dataset dataset = (Dataset) dataset1Info.get("dataset");
        datasetConfiguration.insert(dataset);

        assertThat(datasetDtoRepository.fetchImplementationsByDatasetUuid(((Dataset) dataset1Info.get("dataset")).getMetadataKey().getUuid()))
                .doesNotContain((DatasetImplementationDto) dataset1Info.get("datasetImplementationDto3"),
                        (DatasetImplementationDto) dataset1Info.get("datasetImplementationDto4"));
    }

    @Test
    void getDatasetImplementationsByDatasetUuidEmpty() {
        Map<String, Object> dataset1Info = generateDataset(0, 0, 2, 2);
        Dataset dataset = (Dataset) dataset1Info.get("dataset");
        datasetConfiguration.insert(dataset);

        assertThat(datasetDtoRepository.fetchImplementationsByDatasetUuid(UUID.randomUUID())
                .isEmpty());
    }

    @Test
    void getAllPaginatedNoImplementationsLinkedToDatasetPageOverflow() throws InterruptedException {
        Dataset dataset1 = Dataset.builder()
                .metadataKey(new DatasetKey(UUID.randomUUID()))
                .name("dataset1")
                .datasetImplementations(new HashSet<>())
                .build();
        Dataset dataset2 = Dataset.builder()
                .metadataKey(new DatasetKey(UUID.randomUUID()))
                .name("dataset2")
                .datasetImplementations(new HashSet<>())
                .build();
        Dataset dataset3 = Dataset.builder()
                .metadataKey(new DatasetKey(UUID.randomUUID()))
                .name("dataset3")
                .datasetImplementations(new HashSet<>())
                .build();
        datasetConfiguration.insert(dataset1);
        datasetConfiguration.insert(dataset2);
        // sleep needed for the ordering of the datasets based on load timestamps
        sleep(1);
        datasetConfiguration.insert(dataset3);
        DatasetDto datasetDto3 = DatasetDto.builder()
                .uuid(dataset3.getMetadataKey().getUuid())
                .name("dataset3")
                .implementations(new HashSet<>())
                .build();
        Pageable pageable = PageRequest.of(1, 2);
        assertThat(
                datasetDtoRepository.fetchAll(pageable, new HashSet<>()))
                .containsOnly(datasetDto3);
    }

    @Test
    void getAllFilterByName() throws InterruptedException {
        Dataset dataset1 = Dataset.builder()
                .metadataKey(new DatasetKey(UUID.randomUUID()))
                .name("dataset1")
                .datasetImplementations(new HashSet<>())
                .build();
        Dataset dataset2 = Dataset.builder()
                .metadataKey(new DatasetKey(UUID.randomUUID()))
                .name("dataset11")
                .datasetImplementations(new HashSet<>())
                .build();
        Dataset dataset3 = Dataset.builder()
                .metadataKey(new DatasetKey(UUID.randomUUID()))
                .name("dataset3")
                .datasetImplementations(new HashSet<>())
                .build();
        datasetConfiguration.insert(dataset1);
        datasetConfiguration.insert(dataset2);
        datasetConfiguration.insert(dataset3);
        DatasetDto datasetDto1 = DatasetDto.builder()
                .uuid(dataset1.getMetadataKey().getUuid())
                .name("dataset1")
                .implementations(new HashSet<>())
                .build();
        DatasetDto datasetDto2 = DatasetDto.builder()
                .uuid(dataset2.getMetadataKey().getUuid())
                .name("dataset11")
                .implementations(new HashSet<>())
                .build();
        DatasetDto datasetDto3 = DatasetDto.builder()
                .uuid(dataset3.getMetadataKey().getUuid())
                .name("dataset3")
                .implementations(new HashSet<>())
                .build();
        assertThat(
                datasetDtoRepository.fetchAll(
                        Pageable.unpaged(),
                        Stream.of(
                                new DatasetFilter(DatasetFilterOption.NAME, "dataset", false)
                        ).collect(Collectors.toSet())))
                .containsOnly(datasetDto1, datasetDto2, datasetDto3);
        assertThat(
                datasetDtoRepository.fetchAll(
                        Pageable.unpaged(),
                        Stream.of(
                                new DatasetFilter(DatasetFilterOption.NAME, "dataset1", false)
                        ).collect(Collectors.toSet())))
                .containsOnly(datasetDto1, datasetDto2);
        assertThat(
                datasetDtoRepository.fetchAll(
                        Pageable.unpaged(),
                        Stream.of(
                                new DatasetFilter(DatasetFilterOption.NAME, "dataset3", false)
                        ).collect(Collectors.toSet())))
                .containsOnly(datasetDto3);
        assertThat(
                datasetDtoRepository.fetchAll(
                        Pageable.unpaged(),
                        Stream.of(
                                new DatasetFilter(DatasetFilterOption.NAME, "dataset4", false)
                        ).collect(Collectors.toSet())))
                .isEmpty();
    }

    private Map<String, Object> generateDataset(int datasetIndex, int implementationCount, int labelCount, int keyValueCount) {
        Map<String, Object> info = new HashMap<>();

        UUID datasetUUID = UUID.randomUUID();
        info.put("datasetUUID", datasetUUID);
        Dataset dataset = Dataset.builder()
                .metadataKey(new DatasetKey(datasetUUID))
                .name(String.format("dataset%d", datasetIndex))
                .datasetImplementations(
                        IntStream.range(0, implementationCount).boxed()
                                .map(implementationIndex -> {
                                    UUID datasetImplementationUUID = UUID.randomUUID();
                                    info.put(String.format("datasetImplementation%dUUID", implementationIndex), datasetImplementationUUID);
                                    DatasetImplementation datasetImplementation = InMemoryDatasetImplementation.builder()
                                            .metadataKey(new DatasetImplementationKey(datasetImplementationUUID))
                                            .datasetKey(new DatasetKey(datasetUUID))
                                            .name(String.format("dataset%d", datasetIndex))
                                            .datasetImplementationLabels(
                                                    IntStream.range(0, labelCount).boxed()
                                                            .map(labelIndex -> {
                                                                        UUID datasetImplementationLabelUUID = UUID.randomUUID();
                                                                        info.put(String.format("datasetImplementation%dLabel%dUUID", implementationIndex, labelIndex), datasetImplementationLabelUUID);
                                                                        return DatasetImplementationLabel.builder()
                                                                                .metadataKey(new DatasetImplementationLabelKey(datasetImplementationLabelUUID))
                                                                                .datasetImplementationKey(new DatasetImplementationKey(datasetImplementationUUID))
                                                                                .value(String.format("label%d%d%d", datasetIndex, implementationIndex, labelIndex))
                                                                                .build();
                                                                    }
                                                            ).collect(Collectors.toSet()))
                                            .keyValues(
                                                    IntStream.range(0, keyValueCount).boxed()
                                                            .map(keyValueIndex -> {
                                                                UUID datasetImplementationKeyValueUUID = UUID.randomUUID();
                                                                info.put(String.format("datasetImplementation%dKeyValue%dUUID", implementationIndex, keyValueIndex), datasetImplementationKeyValueUUID);

                                                                return InMemoryDatasetImplementationKeyValue.builder()
                                                                        .metadataKey(new InMemoryDatasetImplementationKeyValueKey(datasetImplementationKeyValueUUID))
                                                                        .datasetImplementationKey(new DatasetImplementationKey(datasetImplementationUUID))
                                                                        .key(String.format("key%d%d%d", datasetIndex, implementationIndex, keyValueIndex))
                                                                        .value(String.format("value%d%d%d", datasetIndex, implementationIndex, keyValueIndex))
                                                                        .build();
                                                            }).collect(Collectors.toSet())
                                            )
                                            .build();
                                    info.put(String.format("datasetImplementation%d", implementationIndex), datasetImplementation);
                                    return datasetImplementation;

                                })
                                .collect(Collectors.toSet()))
                .build();
        info.put("dataset", dataset);
        DatasetDto datasetDto = DatasetDto.builder()
                .uuid(datasetUUID)
                .name(String.format("dataset%d", datasetIndex))
                .implementations(
                        IntStream.range(0, implementationCount).boxed()
                                .map(implementationIndex -> {
                                    UUID datasetImplementationUUID = (UUID) info.get(String.format("datasetImplementation%dUUID", implementationIndex));
                                    InMemoryDatasetImplementationDto inMemoryDatasetImplementationDto = InMemoryDatasetImplementationDto.builder()
                                            .uuid(datasetImplementationUUID)
                                            .labels(
                                                    IntStream.range(0, labelCount).boxed()
                                                            .map(labelIndex -> {
                                                                        UUID datasetImplementationLabelUUID = (UUID) info.get(String.format("datasetImplementation%dLabel%dUUID", implementationIndex, labelIndex));
                                                                        return DatasetImplementationLabelDto.builder()
                                                                                .uuid(datasetImplementationLabelUUID)
                                                                                .label(String.format("label%d%d%d", datasetIndex, implementationIndex, labelIndex))
                                                                                .build();
                                                                    }
                                                            ).collect(Collectors.toSet()))
                                            .keyValues(
                                                    IntStream.range(0, keyValueCount).boxed()
                                                            .map(keyValueIndex -> {
                                                                UUID datasetImplementationKeyValueUUID = (UUID) info.get(String.format("datasetImplementation%dKeyValue%dUUID", implementationIndex, keyValueIndex));
                                                                return InMemoryDatasetImplementationKeyValueDto.builder()
                                                                        .uuid(datasetImplementationKeyValueUUID)
                                                                        .key(String.format("key%d%d%d", datasetIndex, implementationIndex, keyValueIndex))
                                                                        .value(String.format("value%d%d%d", datasetIndex, implementationIndex, keyValueIndex))
                                                                        .build();
                                                            }).collect(Collectors.toSet())
                                            )
                                            .build();
                                    info.put(String.format("datasetImplementationDto%d", implementationIndex), inMemoryDatasetImplementationDto);
                                    return inMemoryDatasetImplementationDto;
                                })
                                .map(DatasetImplementationDto::getUuid)
                                .collect(Collectors.toSet()))
                .build();
        info.put("datasetDto", datasetDto);
        return info;
    }

}
