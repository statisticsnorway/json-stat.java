/**
 * Copyright (C) 2016 Hadrien Kohl (hadrien.kohl@gmail.com) and contributors
 *
 *     DimensionTest.java
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
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import no.ssb.jsonstat.JsonStatModule;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DimensionTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        // TODO
        mapper.registerModule(new JsonStatModule());
        mapper.registerModule(new Jdk8Module().configureAbsentsAsNulls(true));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void testSerialize() throws Exception {

        Dimension dimension = Dimension.create("test")
                .withIndex(ImmutableSet.of("index 0", "index 1")).build();
        String value = mapper.writeValueAsString(dimension);

        assertThat(value).isNotNull();

        dimension = Dimension.create("test")
                .withIndexedLabels(ImmutableMap.of(
                        "test", "test label",
                        "test2", "test label2"
                )).build();
        value = mapper.writeValueAsString(dimension);

        assertThat(value).isNotNull();
    }
}
