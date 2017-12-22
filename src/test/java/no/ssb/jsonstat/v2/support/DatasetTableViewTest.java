/**
 * Copyright (C) 2016 Hadrien Kohl (hadrien.kohl@gmail.com) and contributors
 *
 *     DatasetTableViewTest.java
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
package no.ssb.jsonstat.v2.support;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import no.ssb.jsonstat.v2.Dataset;
import no.ssb.jsonstat.v2.Dimension;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;


public class DatasetTableViewTest {

    private BiMap<String, Integer> hashCodes;
    private Dataset dataset;

    @Before
    public void setUp() throws Exception {

        hashCodes = HashBiMap.create(
                Lists.cartesianProduct(
                    asList("A1", "A2", "A3"),
                    asList("B1", "B2"),
                    asList("C1", "C2", "C3", "C4")
                ).stream()
                        .map(s -> String.join("", s))
                        .collect(Collectors.toMap(
                Function.identity(),
                String::hashCode
        )));

        dataset = Dataset.create("test")
                .withDimensions(
                        Dimension.create("A")
                                .withCategories("A1", "A2", "A3"),
                        Dimension.create("B")
                                .withCategories("B1", "B2"),
                        Dimension.create("C")
                                .withCategories("C1", "C2", "C3", "C4")
                )
                .withMapper(strings -> String.join("", strings).hashCode()).build();

    }



    @Test
    public void testRowBCAndColumnA() throws Exception {

        DatasetTableView table = new DatasetTableView(
                dataset, ImmutableSet.of("B", "C"), ImmutableSet.of("A"));

        assertThat(table.cellSet()).contains(
                // Row 1
                cell(asList("B1", "C1"), asList("A1"), hashCodes.get("A1B1C1")),
                cell(asList("B1", "C1"), asList("A2"), hashCodes.get("A2B1C1")),
                cell(asList("B1", "C1"), asList("A3"), hashCodes.get("A3B1C1")),
                // Row 2
                cell(asList("B1", "C2"), asList("A1"), hashCodes.get("A1B1C2")),
                cell(asList("B1", "C2"), asList("A2"), hashCodes.get("A2B1C2")),
                cell(asList("B1", "C2"), asList("A3"), hashCodes.get("A3B1C2")),
                // Row 3
                cell(asList("B1", "C3"), asList("A1"), hashCodes.get("A1B1C3")),
                cell(asList("B1", "C3"), asList("A2"), hashCodes.get("A2B1C3")),
                cell(asList("B1", "C3"), asList("A3"), hashCodes.get("A3B1C3")),
                // Row 4
                cell(asList("B1", "C4"), asList("A1"), hashCodes.get("A1B1C4")),
                cell(asList("B1", "C4"), asList("A2"), hashCodes.get("A2B1C4")),
                cell(asList("B1", "C4"), asList("A3"), hashCodes.get("A3B1C4")),
                // Row 5
                cell(asList("B2", "C1"), asList("A1"), hashCodes.get("A1B2C1")),
                cell(asList("B2", "C1"), asList("A2"), hashCodes.get("A2B2C1")),
                cell(asList("B2", "C1"), asList("A3"), hashCodes.get("A3B2C1")),
                // Row 6
                cell(asList("B2", "C2"), asList("A1"), hashCodes.get("A1B2C2")),
                cell(asList("B2", "C2"), asList("A2"), hashCodes.get("A2B2C2")),
                cell(asList("B2", "C2"), asList("A3"), hashCodes.get("A3B2C2")),
                // Row 7
                cell(asList("B2", "C3"), asList("A1"), hashCodes.get("A1B2C3")),
                cell(asList("B2", "C3"), asList("A2"), hashCodes.get("A2B2C3")),
                cell(asList("B2", "C3"), asList("A3"), hashCodes.get("A3B2C3")),
                // Row 8
                cell(asList("B2", "C4"), asList("A1"), hashCodes.get("A1B2C4")),
                cell(asList("B2", "C4"), asList("A2"), hashCodes.get("A2B2C4")),
                cell(asList("B2", "C4"), asList("A3"), hashCodes.get("A3B2C4"))
        );

        assertThat(table.rowMap().get(asList("B1", "C1"))).containsExactly(
                entry(asList("A1"), hashCodes.get("A1B1C1")),
                entry(asList("A2"), hashCodes.get("A2B1C1")),
                entry(asList("A3"), hashCodes.get("A3B1C1"))
        );
        assertThat(table.rowMap().get(asList("B1", "C2"))).containsExactly(
                entry(asList("A1"), hashCodes.get("A1B1C2")),
                entry(asList("A2"), hashCodes.get("A2B1C2")),
                entry(asList("A3"), hashCodes.get("A3B1C2"))
        );
        assertThat(table.rowMap().get(asList("B1", "C3"))).containsExactly(
                entry(asList("A1"), hashCodes.get("A1B1C3")),
                entry(asList("A2"), hashCodes.get("A2B1C3")),
                entry(asList("A3"), hashCodes.get("A3B1C3"))
        );
        assertThat(table.rowMap().get(asList("B1", "C4"))).containsExactly(
                entry(asList("A1"), hashCodes.get("A1B1C4")),
                entry(asList("A2"), hashCodes.get("A2B1C4")),
                entry(asList("A3"), hashCodes.get("A3B1C4"))
        );
        assertThat(table.rowMap().get(asList("B2", "C1"))).containsExactly(
                entry(asList("A1"), hashCodes.get("A1B2C1")),
                entry(asList("A2"), hashCodes.get("A2B2C1")),
                entry(asList("A3"), hashCodes.get("A3B2C1"))
        );
        assertThat(table.rowMap().get(asList("B2", "C2"))).containsExactly(
                entry(asList("A1"), hashCodes.get("A1B2C2")),
                entry(asList("A2"), hashCodes.get("A2B2C2")),
                entry(asList("A3"), hashCodes.get("A3B2C2"))
        );
        assertThat(table.rowMap().get(asList("B2", "C3"))).containsExactly(
                entry(asList("A1"), hashCodes.get("A1B2C3")),
                entry(asList("A2"), hashCodes.get("A2B2C3")),
                entry(asList("A3"), hashCodes.get("A3B2C3"))
        );
        assertThat(table.rowMap().get(asList("B2", "C4"))).containsExactly(
                entry(asList("A1"), hashCodes.get("A1B2C4")),
                entry(asList("A2"), hashCodes.get("A2B2C4")),
                entry(asList("A3"), hashCodes.get("A3B2C4"))
        );

        assertThat(table.columnMap().get(asList("A1"))).containsExactly(
                entry(asList("B1", "C1"), hashCodes.get("A1B1C1")),
                entry(asList("B1", "C2"), hashCodes.get("A1B1C2")),
                entry(asList("B1", "C3"), hashCodes.get("A1B1C3")),
                entry(asList("B1", "C4"), hashCodes.get("A1B1C4")),
                entry(asList("B2", "C1"), hashCodes.get("A1B2C1")),
                entry(asList("B2", "C2"), hashCodes.get("A1B2C2")),
                entry(asList("B2", "C3"), hashCodes.get("A1B2C3")),
                entry(asList("B2", "C4"), hashCodes.get("A1B2C4"))
        );

        assertThat(table.columnMap().get(asList("A2"))).containsExactly(
                entry(asList("B1", "C1"), hashCodes.get("A2B1C1")),
                entry(asList("B1", "C2"), hashCodes.get("A2B1C2")),
                entry(asList("B1", "C3"), hashCodes.get("A2B1C3")),
                entry(asList("B1", "C4"), hashCodes.get("A2B1C4")),
                entry(asList("B2", "C1"), hashCodes.get("A2B2C1")),
                entry(asList("B2", "C2"), hashCodes.get("A2B2C2")),
                entry(asList("B2", "C3"), hashCodes.get("A2B2C3")),
                entry(asList("B2", "C4"), hashCodes.get("A2B2C4"))
        );

        assertThat(table.columnMap().get(asList("A3"))).containsExactly(
                entry(asList("B1", "C1"), hashCodes.get("A3B1C1")),
                entry(asList("B1", "C2"), hashCodes.get("A3B1C2")),
                entry(asList("B1", "C3"), hashCodes.get("A3B1C3")),
                entry(asList("B1", "C4"), hashCodes.get("A3B1C4")),
                entry(asList("B2", "C1"), hashCodes.get("A3B2C1")),
                entry(asList("B2", "C2"), hashCodes.get("A3B2C2")),
                entry(asList("B2", "C3"), hashCodes.get("A3B2C3")),
                entry(asList("B2", "C4"), hashCodes.get("A3B2C4"))
        );

    }

    @Test
    public void testRowABAndColumnC() throws Exception {

        DatasetTableView table = new DatasetTableView(
                dataset, ImmutableSet.of("A", "B"), ImmutableSet.of("C"));

        assertThat(table.cellSet()).contains(
                // Row 1
                cell(asList("A1", "B1"), asList("C1"), hashCodes.get("A1B1C1")),
                cell(asList("A1", "B1"), asList("C2"), hashCodes.get("A1B1C2")),
                cell(asList("A1", "B1"), asList("C3"), hashCodes.get("A1B1C3")),
                cell(asList("A1", "B1"), asList("C4"), hashCodes.get("A1B1C4")),
                // Row 2
                cell(asList("A1", "B2"), asList("C1"), hashCodes.get("A1B2C1")),
                cell(asList("A1", "B2"), asList("C2"), hashCodes.get("A1B2C2")),
                cell(asList("A1", "B2"), asList("C3"), hashCodes.get("A1B2C3")),
                cell(asList("A1", "B2"), asList("C4"), hashCodes.get("A1B2C4")),
                // Row 3
                cell(asList("A2", "B1"), asList("C1"), hashCodes.get("A2B1C1")),
                cell(asList("A2", "B1"), asList("C2"), hashCodes.get("A2B1C2")),
                cell(asList("A2", "B1"), asList("C3"), hashCodes.get("A2B1C3")),
                cell(asList("A2", "B1"), asList("C4"), hashCodes.get("A2B1C4")),
                // Row 4
                cell(asList("A2", "B2"), asList("C1"), hashCodes.get("A2B2C1")),
                cell(asList("A2", "B2"), asList("C2"), hashCodes.get("A2B2C2")),
                cell(asList("A2", "B2"), asList("C3"), hashCodes.get("A2B2C3")),
                cell(asList("A2", "B2"), asList("C4"), hashCodes.get("A2B2C4")),
                // Row 5
                cell(asList("A3", "B1"), asList("C1"), hashCodes.get("A3B1C1")),
                cell(asList("A3", "B1"), asList("C2"), hashCodes.get("A3B1C2")),
                cell(asList("A3", "B1"), asList("C3"), hashCodes.get("A3B1C3")),
                cell(asList("A3", "B1"), asList("C4"), hashCodes.get("A3B1C4")),
                // Row 6
                cell(asList("A3", "B2"), asList("C1"), hashCodes.get("A3B2C1")),
                cell(asList("A3", "B2"), asList("C2"), hashCodes.get("A3B2C2")),
                cell(asList("A3", "B2"), asList("C3"), hashCodes.get("A3B2C3")),
                cell(asList("A3", "B2"), asList("C4"), hashCodes.get("A3B2C4"))
        );

        assertThat(table.rowMap().get(asList("A1", "B1"))).containsExactly(
                entry(asList("C1"), hashCodes.get("A1B1C1")),
                entry(asList("C2"), hashCodes.get("A1B1C2")),
                entry(asList("C3"), hashCodes.get("A1B1C3")),
                entry(asList("C4"), hashCodes.get("A1B1C4"))
        );
        assertThat(table.rowMap().get(asList("A1", "B2"))).containsExactly(
                entry(asList("C1"), hashCodes.get("A1B2C1")),
                entry(asList("C2"), hashCodes.get("A1B2C2")),
                entry(asList("C3"), hashCodes.get("A1B2C3")),
                entry(asList("C4"), hashCodes.get("A1B2C4"))
        );
        assertThat(table.rowMap().get(asList("A2", "B1"))).containsExactly(
                entry(asList("C1"), hashCodes.get("A2B1C1")),
                entry(asList("C2"), hashCodes.get("A2B1C2")),
                entry(asList("C3"), hashCodes.get("A2B1C3")),
                entry(asList("C4"), hashCodes.get("A2B1C4"))
        );
        assertThat(table.rowMap().get(asList("A2", "B2"))).containsExactly(
                entry(asList("C1"), hashCodes.get("A2B2C1")),
                entry(asList("C2"), hashCodes.get("A2B2C2")),
                entry(asList("C3"), hashCodes.get("A2B2C3")),
                entry(asList("C4"), hashCodes.get("A2B2C4"))
        );
        assertThat(table.rowMap().get(asList("A3", "B1"))).containsExactly(
                entry(asList("C1"), hashCodes.get("A3B1C1")),
                entry(asList("C2"), hashCodes.get("A3B1C2")),
                entry(asList("C3"), hashCodes.get("A3B1C3")),
                entry(asList("C4"), hashCodes.get("A3B1C4"))
        );
        assertThat(table.rowMap().get(asList("A3", "B2"))).containsExactly(
                entry(asList("C1"), hashCodes.get("A3B2C1")),
                entry(asList("C2"), hashCodes.get("A3B2C2")),
                entry(asList("C3"), hashCodes.get("A3B2C3")),
                entry(asList("C4"), hashCodes.get("A3B2C4"))
        );

        assertThat(table.columnMap().get(asList("C1"))).containsExactly(
                entry(asList("A1", "B1"), hashCodes.get("A1B1C1")),
                entry(asList("A1", "B2"), hashCodes.get("A1B2C1")),
                entry(asList("A2", "B1"), hashCodes.get("A2B1C1")),
                entry(asList("A2", "B2"), hashCodes.get("A2B2C1")),
                entry(asList("A3", "B1"), hashCodes.get("A3B1C1")),
                entry(asList("A3", "B2"), hashCodes.get("A3B2C1"))
        );
        assertThat(table.columnMap().get(asList("C2"))).containsExactly(
                entry(asList("A1", "B1"), hashCodes.get("A1B1C2")),
                entry(asList("A1", "B2"), hashCodes.get("A1B2C2")),
                entry(asList("A2", "B1"), hashCodes.get("A2B1C2")),
                entry(asList("A2", "B2"), hashCodes.get("A2B2C2")),
                entry(asList("A3", "B1"), hashCodes.get("A3B1C2")),
                entry(asList("A3", "B2"), hashCodes.get("A3B2C2"))
        );
        assertThat(table.columnMap().get(asList("C3"))).containsExactly(
                entry(asList("A1", "B1"), hashCodes.get("A1B1C3")),
                entry(asList("A1", "B2"), hashCodes.get("A1B2C3")),
                entry(asList("A2", "B1"), hashCodes.get("A2B1C3")),
                entry(asList("A2", "B2"), hashCodes.get("A2B2C3")),
                entry(asList("A3", "B1"), hashCodes.get("A3B1C3")),
                entry(asList("A3", "B2"), hashCodes.get("A3B2C3"))
        );
        assertThat(table.columnMap().get(asList("C4"))).containsExactly(
                entry(asList("A1", "B1"), hashCodes.get("A1B1C4")),
                entry(asList("A1", "B2"), hashCodes.get("A1B2C4")),
                entry(asList("A2", "B1"), hashCodes.get("A2B1C4")),
                entry(asList("A2", "B2"), hashCodes.get("A2B2C4")),
                entry(asList("A3", "B1"), hashCodes.get("A3B1C4")),
                entry(asList("A3", "B2"), hashCodes.get("A3B2C4"))
        );


    }

    @Test
    public void testRowACAndColumnB() throws Exception {

        DatasetTableView table = new DatasetTableView(
                dataset, ImmutableSet.of("A", "C"), ImmutableSet.of("B"));

        assertThat(table.cellSet()).contains(
                // Row 1
                cell(asList("A1", "C1"), asList("B1"), hashCodes.get("A1B1C1")),
                cell(asList("A1", "C1"), asList("B2"), hashCodes.get("A1B2C1")),
                // Row 2
                cell(asList("A1", "C2"), asList("B1"), hashCodes.get("A1B1C2")),
                cell(asList("A1", "C2"), asList("B2"), hashCodes.get("A1B2C2")),
                // Row 3
                cell(asList("A1", "C3"), asList("B1"), hashCodes.get("A1B1C3")),
                cell(asList("A1", "C3"), asList("B2"), hashCodes.get("A1B2C3")),
                // Row 4
                cell(asList("A1", "C4"), asList("B1"), hashCodes.get("A1B1C4")),
                cell(asList("A1", "C4"), asList("B2"), hashCodes.get("A1B2C4")),
                // Row 5
                cell(asList("A2", "C1"), asList("B1"), hashCodes.get("A2B1C1")),
                cell(asList("A2", "C1"), asList("B2"), hashCodes.get("A2B2C1")),
                // Row 6
                cell(asList("A2", "C2"), asList("B1"), hashCodes.get("A2B1C2")),
                cell(asList("A2", "C2"), asList("B2"), hashCodes.get("A2B2C2")),
                // Row 7
                cell(asList("A2", "C3"), asList("B1"), hashCodes.get("A2B1C3")),
                cell(asList("A2", "C3"), asList("B2"), hashCodes.get("A2B2C3")),
                // Row 8
                cell(asList("A2", "C4"), asList("B1"), hashCodes.get("A2B1C4")),
                cell(asList("A2", "C4"), asList("B2"), hashCodes.get("A2B2C4"))
        );

        assertThat(table.rowMap().get(asList("A1", "C1"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A1B1C1")),
                entry(asList("B2"), hashCodes.get("A1B2C1"))
        );
        assertThat(table.rowMap().get(asList("A1", "C2"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A1B1C2")),
                entry(asList("B2"), hashCodes.get("A1B2C2"))
        );
        assertThat(table.rowMap().get(asList("A1", "C3"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A1B1C3")),
                entry(asList("B2"), hashCodes.get("A1B2C3"))
        );
        assertThat(table.rowMap().get(asList("A1", "C4"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A1B1C4")),
                entry(asList("B2"), hashCodes.get("A1B2C4"))
        );
        assertThat(table.rowMap().get(asList("A2", "C1"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A2B1C1")),
                entry(asList("B2"), hashCodes.get("A2B2C1"))
        );
        assertThat(table.rowMap().get(asList("A2", "C2"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A2B1C2")),
                entry(asList("B2"), hashCodes.get("A2B2C2"))
        );
        assertThat(table.rowMap().get(asList("A2", "C3"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A2B1C3")),
                entry(asList("B2"), hashCodes.get("A2B2C3"))
        );
        assertThat(table.rowMap().get(asList("A2", "C4"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A2B1C4")),
                entry(asList("B2"), hashCodes.get("A2B2C4"))
        );
        assertThat(table.rowMap().get(asList("A3", "C1"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A3B1C1")),
                entry(asList("B2"), hashCodes.get("A3B2C1"))
        );
        assertThat(table.rowMap().get(asList("A3", "C2"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A3B1C2")),
                entry(asList("B2"), hashCodes.get("A3B2C2"))
        );
        assertThat(table.rowMap().get(asList("A3", "C3"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A3B1C3")),
                entry(asList("B2"), hashCodes.get("A3B2C3"))
        );
        assertThat(table.rowMap().get(asList("A3", "C4"))).containsExactly(
                entry(asList("B1"), hashCodes.get("A3B1C4")),
                entry(asList("B2"), hashCodes.get("A3B2C4"))
        );

        assertThat(table.columnMap().get(asList("B1"))).containsExactly(
                entry(asList("A1", "C1"), hashCodes.get("A1B1C1")),
                entry(asList("A1", "C2"), hashCodes.get("A1B1C2")),
                entry(asList("A1", "C3"), hashCodes.get("A1B1C3")),
                entry(asList("A1", "C4"), hashCodes.get("A1B1C4")),

                entry(asList("A2", "C1"), hashCodes.get("A2B1C1")),
                entry(asList("A2", "C2"), hashCodes.get("A2B1C2")),
                entry(asList("A2", "C3"), hashCodes.get("A2B1C3")),
                entry(asList("A2", "C4"), hashCodes.get("A2B1C4")),

                entry(asList("A3", "C1"), hashCodes.get("A3B1C1")),
                entry(asList("A3", "C2"), hashCodes.get("A3B1C2")),
                entry(asList("A3", "C3"), hashCodes.get("A3B1C3")),
                entry(asList("A3", "C4"), hashCodes.get("A3B1C4"))
        );
        assertThat(table.columnMap().get(asList("B2"))).containsExactly(
                entry(asList("A1", "C1"), hashCodes.get("A1B2C1")),
                entry(asList("A1", "C2"), hashCodes.get("A1B2C2")),
                entry(asList("A1", "C3"), hashCodes.get("A1B2C3")),
                entry(asList("A1", "C4"), hashCodes.get("A1B2C4")),

                entry(asList("A2", "C1"), hashCodes.get("A2B2C1")),
                entry(asList("A2", "C2"), hashCodes.get("A2B2C2")),
                entry(asList("A2", "C3"), hashCodes.get("A2B2C3")),
                entry(asList("A2", "C4"), hashCodes.get("A2B2C4")),

                entry(asList("A3", "C1"), hashCodes.get("A3B2C1")),
                entry(asList("A3", "C2"), hashCodes.get("A3B2C2")),
                entry(asList("A3", "C3"), hashCodes.get("A3B2C3")),
                entry(asList("A3", "C4"), hashCodes.get("A3B2C4"))
        );
    }

    private Table.Cell<List<String>, List<String>, Number> cell(List<String> row, List<String> column, Number value) {
        return Tables.immutableCell(row, column, value);
    }


}
