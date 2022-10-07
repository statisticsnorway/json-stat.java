package no.ssb.jsonstat.v2.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.ssb.jsonstat.v2.Dimension;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class DimensionDeserializerTest {

    private ObjectMapper mapper;
    private DimensionDeserializer deserializer;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        deserializer = spy(new DimensionDeserializer());

        mapper.registerModule(new SimpleModule() {{
            addDeserializer(
                    Dimension.Builder.class,
                    deserializer
            );
        }});
    }

    private JsonParser createSpyParser(String content, String name) throws IOException {
        JsonParser parser = spy(mapper.getFactory().createParser(content));
        doReturn(name).doCallRealMethod().doCallRealMethod().when(parser).getCurrentName();
        //parser.nextValue();
        return parser;
    }

    private DeserializationContext createSpyContext() {
        return mapper.getDeserializationContext();
    }

    @Test
    public void testComplete() throws Exception {

        String json = "" +
                "{" +
                "  \"link\" : { \"describedby\": [ { \"extension\": { \"Region\": \"urn:ssb:classification:klass:104\" } } ] }," +
                "  \"label\" : \"place of birth\"," +
                "  \"category\" : {" +
                "    \"index\" : [\"T\", \"C\", \"P\", \"G\", \"A\", \"F\"]," +
                "    \"label\" : {" +
                "      \"T\" : \"total\"," +
                "      \"C\" : \"county of residence\"," +
                "      \"P\" : \"another county in the same province\"," +
                "      \"G\" : \"another province of Galicia\"," +
                "      \"A\" : \"in another autonomous community\"," +
                "      \"F\" : \"abroad\"" +
                "    }" +
                "  }" +
                "}";

        Dimension.Builder builder = mapper.readValue(
                createSpyParser(json, "birth"),
                Dimension.Builder.class
        );

        assertThat(builder).isNotNull();
        Dimension build = builder.build();
        assertThat(build.getLabel()).contains("place of birth");
        Dimension.Category category = build.getCategory();
        assertThat(category.getIndex()).containsExactly(
                "T", "C", "P", "G", "A", "F"
        );
        assertThat(category.getLabel()).containsOnly(
                entry("T", "total"),
                entry("C", "county of residence"),
                entry("P", "another county in the same province"),
                entry("G", "another province of Galicia"),
                entry("A", "in another autonomous community"),
                entry("F", "abroad")
        );

    }

    @Test
    public void testNoLabel() throws Exception {

        String json = "" +
                "{" +
                "  \"category\" : {" +
                "    \"index\" : {" +
                "      \"2001\" : 0," +
                "      \"2011\" : 1" +
                "    }" +
                "  }" +
                "}";

        Dimension.Builder builder = mapper.readValue(
                createSpyParser(json, "time"),
                Dimension.Builder.class
        );

        assertThat(builder).isNotNull();

        Dimension build = builder.build();
        assertThat(build.getLabel()).isNotPresent();
        assertThat(build.getCategory().getIndex()).containsExactly(
                "2001", "2011");

    }

    @Test
    public void testNoCategoryLabel() throws Exception {

        String json = "" +
                "{" +
                "  \"label\" : \"year\"," +
                "  \"category\" : {" +
                "    \"index\" : {" +
                "      \"2001\" : 0," +
                "      \"2011\" : 1" +
                "    }" +
                "  }" +
                "}";

        Dimension.Builder builder = mapper.readValue(
                createSpyParser(json, "time"),
                Dimension.Builder.class
        );

        assertThat(builder).isNotNull();

        Dimension build = builder.build();
        assertThat(build.getLabel()).contains("year");
        assertThat(build.getCategory().getIndex()).containsExactly(
                "2001", "2011");

    }

    @Test
    public void testNoCategoryIndex() throws Exception {

        String json = "" +
                "{" +
                "    \"label\" : \"gender\"," +
                "    \"category\" : {" +
                "      \"label\" : {" +
                "        \"T\" : \"total\"," +
                "        \"M\" : \"male\"," +
                "        \"F\" : \"female\"" +
                "      }" +
                "    }" +
                "}";

        Dimension.Builder builder = mapper.readValue(
                createSpyParser(json, "gender"),
                Dimension.Builder.class
        );

        assertThat(builder).isNotNull();
        Dimension build = builder.build();
        assertThat(build.getLabel()).contains("gender");
        Dimension.Category category = build.getCategory();
        assertThat(category.getLabel()).containsExactly(
                entry("T", "total"),
                entry("M", "male"),
                entry("F", "female")
        );
    }
}
