/**
 * Copyright (C) 2016 Hadrien Kohl (hadrien.kohl@gmail.com) and contributors
 *
 *     DatasetTest.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.ssb.jsonstat.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import no.ssb.jsonstat.JsonStatModule;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.cartesianProduct;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

public class DatasetTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {

        mapper.registerModule(new JsonStatModule());
        mapper.registerModule(new Jdk8Module().configureAbsentsAsNulls(true));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new GuavaModule().configureAbsentsAsNulls(false));
    }

    public void testNeedAtLeastOneDimension() throws Exception {
        fail("TODO");
    }

    public void testNeedAtLeastOneMetric() throws Exception {
        fail("TODO");
    }

    @Test()
    public void testFailIfDuplicateDimension() throws Exception {
        assertThatThrownBy(() -> {
            Dataset.create("Test dataset")
                    .withDimensions(
                            Dimension.create("duplicatedimension"),
                            Dimension.create("duplicatedimension")
                    );
        }).isInstanceOf(DuplicateDimensionException.class)
                .hasMessageContaining("duplicatedimension");
    }

    @Test()
    public void testFailIfDimensionIsNull() throws Exception {
        assertThatThrownBy(() -> Dataset.create().withDimensions((Dimension.Builder[]) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("dimension builder");
    }

    @Test()
    public void testFailIfLabelIsNull() throws Exception {
        assertThatThrownBy(() -> Dataset.create().withLabel(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("label");
    }

    @Test()
    public void testFailIfSourceIsNull() throws Exception {
        assertThatThrownBy(() -> Dataset.create().withSource(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("source");
    }

    @Test()
    public void testFailIfUpdateIsNull() throws Exception {
        assertThatThrownBy(() -> Dataset.create().updatedAt(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("update");
    }

    @Test
    public void testBuilder() throws Exception {

        DatasetBuilder builder = Dataset.create().withLabel("");
        builder.withSource("");
        builder.updatedAt(Instant.now());

        Dimension.Builder dimension = Dimension.create("year")
                .withRole(Dimension.Roles.TIME)
                .withCategories(ImmutableSet.of("2003", "2004", "2005"));

        DatasetValueBuilder valueBuilder = builder.withDimensions(
                dimension,
                Dimension.create("month").withRole(Dimension.Roles.TIME)
                        .withCategories(ImmutableSet.of("may", "june", "july")),
                Dimension.create("week").withTimeRole()
                        .withLabels(ImmutableList.of("30", "31", "32")),
                Dimension.create("population")
                        .withIndexedLabels(ImmutableMap.of(
                                "A", "active population",
                                "E", "employment",
                                "U", "unemployment",
                                "I", "inactive population",
                                "T", "population 15 years old and over"
                        )),
                Dimension.create("arrival").withMetricRole(),
                Dimension.create("departure").withRole(Dimension.Roles.METRIC)
        );

        builder.withExtension(ImmutableMap.<String, String>of("arbitrary_field", "arbitrary_value"));

        // TODO: addDimension("name") returning Dimension.Builder? Super fluent?
        // TODO: How to ensure valid data with the geo builder? Add the type first and extend builders?
        // TODO: express hierarchy with the builder? Check how ES did that with the query builders.
        // example: builder.withDimension(Dimension.create("location")
        //        .withGeoRole());

        //Dataset build = valueBuilder.build();

        //assertThat(build).isNotNull();

    }

    @Test
    public void testGetRows() throws Exception {

        Dataset dataset = Dataset.create("test")
                .withDimensions(
                        Dimension.create("A")
                                .withCategories("A1", "A2", "A3"),
                        Dimension.create("B")
                                .withCategories("B1", "B2"),
                        Dimension.create("C")
                                .withCategories("C1", "C2", "C3", "C4"))
                .withMapper(strings -> String.join("", strings).hashCode()).build();

        List<Object> result = StreamSupport.stream(dataset.getRows().spliterator(), false)
                .collect(Collectors.toList());

        List<Integer> expected = Lists.transform(
                newArrayList(
                        "A1B1C1", "A1B1C2", "A1B1C3", "A1B1C4",
                        "A1B2C1", "A1B2C2", "A1B2C3", "A1B2C4",

                        "A2B1C1", "A2B1C2", "A2B1C3", "A2B1C4",
                        "A2B2C1", "A2B2C2", "A2B2C3", "A2B2C4",

                        "A3B1C1", "A3B1C2", "A3B1C3", "A3B1C4",
                        "A3B2C1", "A3B2C2", "A3B2C3", "A3B2C4"),
                String::hashCode);

        assertThat(result).containsExactlyElementsOf(expected);

    }

    @Test
    public void checkAddTuple() throws Exception {

        DatasetValueBuilder dataset = Dataset.create("test")
                .withDimensions(
                        Dimension.create("A")
                                .withCategories("A1", "A2", "A3"),

                        Dimension.create("B")
                                .withCategories("B1", "B2"),

                        Dimension.create("C")
                                .withMetricRole()
                );
        // TODO: Separate metrics from dimensions...
        // à la withMetricDimension(Dimension.createMetric(name).withUnit()...

        // TODO: Make this less verbose?
//        Dataset ds = dataset.addTuple(
//                list("A", "A1", "B", "B1"),
//                list(1)
//        ).addTuple(
//                list("A1", "B2", "C1"),
//                list(2)
//        ).build();

        // A1B1 1
        // A1B2 3
        // A2B1 5
        // A2B2 6
        // A3B1 4
        // A3B2 2

    }

    private <T> List<T> list(T... e) {
        return Arrays.asList(e);
    }

    @Test
    public void testTuple() {

        DatasetValueBuilder builder = Dataset.create("test")
                .withDimensions(
                        Dimension.create("A")
                                .withCategories("A1", "A2", "A3"),
                        Dimension.create("B")
                                .withCategories("B1", "B2"),
                        Dimension.create("C")
                                .withMetricRole()
                );

        // A1B1 1
        // A1B2 3
        // A2B1 5
        // A2B2 6
        // A3B1 4
        // A3B2 2

//        builder.addTuple(list("A1", "B1"), list(1));
//        builder.addTuple(list("A1", "B2"), list(3));
//        builder.addTuple(list("A2", "B1"), list(5));
//        builder.addTuple(list("A2", "B2"), list(6));
//        builder.addTuple(list("A3", "B1"), list(4));
//        builder.addTuple(list("A3", "B2"), list(2));
//
//        assertThat(builder.build().getValue().values()).containsExactly(
//                1, 3, 5, 6, 4, 2
//        );


    }

    @Test
    public void testExtension() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JsonStatModule());
        mapper.registerModule(new Jdk8Module().configureAbsentsAsNulls(true));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new GuavaModule().configureAbsentsAsNulls(false));

        DatasetBuilder builder = Dataset.create()
                .withLabel("Testing")
                .withSource("This is the source");

        Dataset dataset = builder
                .withExtension(ImmutableMap.<String, String>of("extensionKey", "extensionValue"))
                .withDimensions(
                        Dimension.create("Measures")
                                .withLabel("Measures")
                                .withMetricRole()
                                .withCategories("Count", "Sum"),
                        Dimension.create("Time").withLabel("Year").withTimeRole().withCategories("2001", "2002", "2003"),
                        Dimension.create("Geography").withLabel("Geography").withCategories("Chile", "Argentina", "Uruguay", "Brasil")
                )
                .withValues(Arrays.asList(new Number[] {1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12}))

                .build();

        String value = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataset);

        assertThat(value).contains("extensionKey");
        assertThat(value).contains("extensionValue");

    }

    @Test
    public void testSerialize() throws Exception {

        DatasetBuilder builder = Dataset.create().withLabel("");
        builder.withSource("");
        builder.updatedAt(Instant.now());

        Dimension.Builder dimension = Dimension.create("year")
                .withRole(Dimension.Roles.TIME)
                .withCategories(ImmutableSet.of("2003", "2004", "2005"));

        builder.withExtension(ImmutableMap.of("arbitrary_field", "arbitrary_value"));

        // TODO: addDimension("name") returning Dimension.Builder? Super fluent?
        // TODO: How to ensure valid data with the geo builder? Add the type first and extend builders?
        // TODO: express hierarchy with the builder? Check how ES did that with the query builders.
        // example: builder.withDimension(Dimension.create("location")
        //        .withGeoRole());

        // Supplier.
        List<Number> collect = cartesianProduct(
                ImmutableList.of("2003", "2004", "2005"),
                ImmutableList.of("may", "june", "july"),
                ImmutableList.of("30", "31", "32"),
                ImmutableMap.of(
                        "A", "active population",
                        "E", "employment",
                        "U", "unemployment",
                        "I", "inactive population",
                        "T", "population 15 years old and over"
                ).keySet().asList()
        ).stream().map(dimensions -> dimensions.hashCode()).collect(Collectors.toList());

        // Some extension.
        List<Map<String, List<Instant>>> extension = Collections.singletonList(
                ImmutableMap.of(
                        "now", Collections.singletonList(Instant.now())
                )
        );

        Dataset dataset = builder.withExtension(extension).withDimensions(
                dimension,
                Dimension.create("month").withRole(Dimension.Roles.TIME)
                        .withCategories(ImmutableSet.of("may", "june", "july")),
                Dimension.create("week").withTimeRole()
                        .withLabels(ImmutableList.of("30", "31", "32")),
                Dimension.create("population")
                        .withIndexedLabels(ImmutableMap.of(
                                "A", "active population",
                                "E", "employment",
                                "U", "unemployment",
                                "I", "inactive population",
                                "T", "population 15 years old and over"
                        ))
        ).withValues(collect).build();

        String value = mapper.writeValueAsString(dataset);

        assertThat(value).isNotNull();

    }

}
