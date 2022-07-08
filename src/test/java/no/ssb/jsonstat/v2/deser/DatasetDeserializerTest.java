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
package no.ssb.jsonstat.v2.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import no.ssb.jsonstat.JsonStatModule;
import no.ssb.jsonstat.v2.DatasetBuildable;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static com.google.common.collect.Lists.cartesianProduct;
import static com.google.common.io.Resources.getResource;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DatasetDeserializerTest {

    DatasetDeserializer ds = new DatasetDeserializer();

    private static List<String> join(List<List<String>> list) {
        return Lists.transform(list, Joiner.on("")::join);
    }

    private static List<String> concat(List<String>... lists) {
        return Lists.newArrayList(Iterables.concat(lists));
    }

    public static Iterable<String> ecmaDates() {
        List<String> time = asList("T00:00", "T00:00:00");
        List<String> offset = asList("", "Z", "+00:00", "-00:00");
        List<String> dateTime = Lists.newArrayList(
                concat(
                        asList(""),
                        join(cartesianProduct(time, offset))
                )
        );

        return join(
                cartesianProduct(
                        asList("2000", "2000-01", "2000-01-01"),
                        Lists.newArrayList(dateTime))
        );
    }

    public void testParseUpdated(String date) throws Exception {
        // Only check that we handle for now.
        ds.parseEcmaDate(date);
    }

    @Test
    public void testParseValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonParser mapParser = mapper.getFactory().createParser("" +
                "{ " +
                "  \"0\": 10," +
                "  \"1\": 20," +
                "  \"3\": 30," +
                "  \"4\": 40}"
        );
        mapParser.nextValue();

        JsonParser arrayParser = mapper.getFactory().createParser(
                "[ 10, 20, null, 30, 40 ]"
        );
        arrayParser.nextValue();

        List<Number> fromMap = ds.parseValues(mapParser, null);
        List<Number> fromArray = ds.parseValues(arrayParser, null);
        List<Number> expected = Lists.newArrayList(
                10, 20, null, 30, 40
        );

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fromMap).as("deserialize values from map").isEqualTo(expected);
        softly.assertThat(fromArray).as("deserialize values from array").isEqualTo(expected);
        softly.assertAll();

    }

    @Test
    public void testDimensionOrder() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new JsonStatModule());

        URL resource = getResource(getClass(), "dimOrder.json");

        JsonParser jsonParser = mapper.getFactory().createParser(resource.openStream());
        jsonParser.nextValue();

        DatasetBuildable deserialize = ds.deserialize(jsonParser, mapper.getDeserializationContext());

        assertThat(deserialize.build().getDimension().keySet()).containsExactly(
                "A", "B", "C"
        );

    }
}
