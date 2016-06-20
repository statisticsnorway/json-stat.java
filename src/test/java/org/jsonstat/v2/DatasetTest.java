package org.jsonstat.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.jsonstat.JsonStatModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.cartesianProduct;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DatasetTest {

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeMethod
    public void setUp() throws Exception {

        mapper.registerModule(new JsonStatModule());
        mapper.registerModule(new Jdk8Module().configureAbsentsAsNulls(true));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new GuavaModule().configureAbsentsAsNulls(false));
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "size and property sizes do not match"
    )
    public void testInvalidSize() throws Exception {

        new Dataset(ImmutableSet.of(
                "a", "b", "c"
        ), ImmutableList.of(
                1, 2, 3, 4
        ));

    }

    @Test(enabled = false)
    public void testNeedAtLeastOneDimension() throws Exception {
        fail("TODO");
    }

    @Test(enabled = false)
    public void testNeedAtLeastOneMetric() throws Exception {
        fail("TODO");
    }

    @Test()
    public void testConstructor() throws Exception {

        new Dataset(ImmutableSet.of(
                "a", "b", "c"
        ), ImmutableList.of(
                1, 2, 3
        ));

    }

    @Test(
            expectedExceptions = DuplicateDimensionException.class,
            expectedExceptionsMessageRegExp = ".*duplicatedimension.*"
    )
    public void testFailIfDuplicateDimension() throws Exception {

        Dataset.create("Test dataset")
                .withDimension(Dimension.create("duplicatedimension"))
                .withDimension(Dimension.create("duplicatedimension"));

    }

    @Test(
            expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*dimension builder.*"
    )
    public void testFailIfDimensionIsNull() throws Exception {
        Dataset.create().withDimension(null);
    }

    @Test(
            expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*label.*"
    )
    public void testFailIfLabelIsNull() throws Exception {
        Dataset.create().withLabel(null);
    }

    @Test(
            expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*source.*"
    )
    public void testFailIfSourceIsNull() throws Exception {
        Dataset.create().withSource(null);
    }

    @Test(
            expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = ".*update.*"
    )
    public void testFailIfUpdateIsNull() throws Exception {
        Dataset.create().updatedAt(null);
    }

    @Test
    public void testBuilder() throws Exception {

        Dataset.Builder builder = Dataset.create().withLabel("");
        builder.withSource("");
        builder.updatedAt(Instant.now());

        Dimension.Builder dimension = Dimension.create("year")
                .withRole(Dimension.Roles.TIME)
                .withCategories(ImmutableSet.of("2003", "2004", "2005"));
        builder.withDimension(dimension);


        builder.withDimension(Dimension.create("month").withRole(Dimension.Roles.TIME)
                .withCategories(ImmutableSet.of("may", "june", "july")));

        builder.withDimension(Dimension.create("week").withTimeRole()
                .withLabels(ImmutableList.of("30", "31", "32")));

        builder.withDimension(Dimension.create("population")
                .withIndexedLabels(ImmutableMap.of(
                        "A", "active population",
                        "E", "employment",
                        "U", "unemployment",
                        "I", "inactive population",
                        "T", "population 15 years old and over"
                )));

        // TODO: addDimension("name") returning Dimension.Builder? Super fluent?
        // TODO: How to ensure valid data with the geo builder? Add the type first and extend builders?
        // TODO: express hierarchy with the builder? Check how ES did that with the query builders.
        // example: builder.withDimension(Dimension.create("location")
        //        .withGeoRole());

        builder.withDimension(Dimension.create("arrival").withMetricRole());
        builder.withDimension(Dimension.create("departure").withRole(Dimension.Roles.METRIC));

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

        Dataset build = builder.withValues(collect);

        assertThat(build).isNotNull();

    }

    @Test
    public void testGetRows() throws Exception {

        Dataset dataset = Dataset.create("test")
                .withDimension(
                        Dimension.create("A")
                                .withCategories("A1", "A2", "A3"))
                .withDimension(Dimension.create("B")
                        .withCategories("B1", "B2"))
                .withDimension(Dimension.create("C")
                        .withCategories("C1", "C2", "C3", "C4")
                ).withMapper(strings -> {
                    return newArrayList(String.join("", strings).hashCode());
                });

        List<Object> result = StreamSupport.stream(dataset.getRows().spliterator(), false)
                .flatMap(Collection::stream)
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
    public void testGetRowsEmtpy() throws Exception {
        Dataset dataset = Dataset.create("test")
                .withDimension(
                        Dimension.create("A")
                                .withCategories("A1", "A2", "A3"))
                .withDimension(Dimension.create("B")
                        .withCategories("B1", "B2"))
                .withDimension(Dimension.create("C")
                        .withCategories("C1", "C2", "C3", "C4")
                ).withMapper(strings -> {
                    return newArrayList(String.join("", strings).hashCode());
                });

        assertThat(dataset.getRows(Collections.emptyList())).isEmpty();
    }

    @Test
    public void testGetRowsAllDimensions() throws Exception {
        Dataset dataset = Dataset.create("test")
                .withDimension(
                        Dimension.create("A")
                                .withCategories("A1", "A2", "A3"))
                .withDimension(Dimension.create("B")
                        .withCategories("B1", "B2"))
                .withDimension(Dimension.create("C")
                        .withCategories("C1", "C2", "C3", "C4")
                ).withMapper(strings -> {
                    return newArrayList(String.join("", strings).hashCode());
                });

        List<Object> result = StreamSupport.stream(dataset.getRows(Arrays.asList("A", "B", "C")).spliterator(), false)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        assertThat(result).hasSize(3 * 2 * 4);
    }

    @Test
    public void testValueMapper() throws Exception {

        Dataset.Builder builder = Dataset.create().withLabel("")
                .withDimension(Dimension.create("year")
                        .withRole(Dimension.Roles.TIME)
                        .withIndexedLabels(ImmutableMap.of("2003", "2003", "2004", "2004", "2005", "2005")))

                .withDimension(Dimension.create("month").withRole(Dimension.Roles.TIME)
                        .withIndexedLabels(ImmutableMap.of("may", "may", "june", "june", "july", "july")))

                .withDimension(Dimension.create("week").withTimeRole()
                        .withIndexedLabels(ImmutableMap.of("30", "30", "31", "31", "32", "32")))

                .withDimension(Dimension.create("population")
                        .withIndexedLabels(ImmutableMap.of(
                                "A", "active population",
                                "E", "employment",
                                "U", "unemployment",
                                "I", "inactive population",
                                "T", "population 15 years old and over"
                        )))
                .withDimension(Dimension.create("amount").withMetricRole()
                        .withIndexedLabels(ImmutableMap.of("millions", "millions")))

                .withDimension(Dimension.create("percent").withMetricRole()
                        .withIndexedLabels(ImmutableMap.of("%", "percent")));


        Dataset dataset = builder.withMapper(
                dimensions -> newArrayList(
                        dimensions.hashCode(),
                        dimensions.hashCode())
        );

        // Supplier.
        List<Number> collect = cartesianProduct(
                ImmutableList.of("2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010"),
                ImmutableList.of("may", "june", "july"),
                ImmutableList.of("30", "31", "32"),
                ImmutableMap.of(
                        "A", "active population",
                        "E", "employment",
                        "U", "unemployment",
                        "I", "inactive population",
                        "T", "population 15 years old and over"
                ).keySet().asList()
        ).stream().map(List::hashCode).collect(Collectors.toList());

        String value = mapper.writeValueAsString(dataset);

        assertThat(value).isNotNull();

    }

    @Test(enabled = false)
    public void testLessMetricsInTheMapper() throws Exception {
        fail("TODO");
    }

    @Test(enabled = false)
    public void testNullsInValuesIsOk() throws Exception {
        fail("TODO");
    }

    @Test
    public void testSerialize() throws Exception {

        // TODO: Find a way to implement the child that is fluent.

        Dataset dataset = new Dataset(ImmutableSet.of(
                "a", "b"
        ), ImmutableList.of(
                1, 1
        ));

        String value = mapper.writeValueAsString(dataset);

        assertThat(value).isNotNull();

    }

    @Test(enabled = false)
    public void testDeserialize() throws Exception {

    }

}