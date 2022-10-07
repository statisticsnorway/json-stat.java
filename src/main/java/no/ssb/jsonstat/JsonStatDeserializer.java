package no.ssb.jsonstat;

import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import no.ssb.jsonstat.v2.DatasetBuildable;
import no.ssb.jsonstat.v2.Dimension;
import no.ssb.jsonstat.v2.deser.DatasetDeserializer;
import no.ssb.jsonstat.v2.deser.DimensionDeserializer;

/**
 * Main deserializer-
 */
public class JsonStatDeserializer extends SimpleDeserializers {

    public JsonStatDeserializer() {
        addDeserializer(DatasetBuildable.class, new DatasetDeserializer());
        addDeserializer(Dimension.Builder.class, new DimensionDeserializer());
    }
}
