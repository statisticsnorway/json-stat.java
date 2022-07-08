/*
 * The MIT License
 *
 * Copyright 2022 Statistisk sentralbyrå - Statistics Norway
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
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
