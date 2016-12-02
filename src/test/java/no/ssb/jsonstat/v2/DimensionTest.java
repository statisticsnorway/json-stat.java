package no.ssb.jsonstat.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import no.ssb.jsonstat.JsonStatModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DimensionTest {

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeMethod
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

    @Test(enabled = false)
    public void testDeserialize() throws Exception {

    }
}
