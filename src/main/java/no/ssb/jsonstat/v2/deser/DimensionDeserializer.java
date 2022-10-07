package no.ssb.jsonstat.v2.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Functions;
import com.google.common.collect.*;
import no.ssb.jsonstat.v2.Dimension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static me.yanaga.guava.stream.MoreCollectors.toImmutableMap;

/**
 * Deserialize dimensions to {@link no.ssb.jsonstat.v2.Dimension.Builder}.
 */
public class DimensionDeserializer extends StdDeserializer<Dimension.Builder> {

    static final TypeReference<Map<String, String>> LABEL_MAP = new TypeReference<Map<String, String>>() {
    };
    static final TypeReference<List<String>> INDEX_LIST = new TypeReference<List<String>>() {
    };
    static final TypeReference<Map<String, Integer>> INDEX_MAP = new TypeReference<Map<String, Integer>>() {
    };

    public DimensionDeserializer() {
        super(Dimension.Builder.class);
    }

    @Override
    public Dimension.Builder deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        // Get the name first.
        String name = parseName(p, ctxt);

        if (p.getCurrentToken() == JsonToken.START_OBJECT) {
            p.nextToken();
        }

        Dimension.Builder dimension;
        dimension = Dimension.create(name);

        while (p.nextValue() != JsonToken.END_OBJECT) {
            switch (p.getCurrentName()) {
                case "category":
                    parseCategory(dimension, p, ctxt);
                    break;
                case "label":
                    dimension.withLabel(parseLabel(p, ctxt));
                    break;
                case "link":
                    p.skipChildren();
                    break;
                default:
                    ctxt.handleUnknownProperty(
                            p,
                            this,
                            Dimension.Builder.class,
                            p.getCurrentName()
                    );
                    break;
            }
        }

        return dimension;
    }

    private void parseCategory(Dimension.Builder dimension, JsonParser p, DeserializationContext ctxt) throws IOException {
        Map<String, String> index = null;
        Map<String, String> label = null;
        while (p.nextValue() != JsonToken.END_OBJECT) {
            switch (p.getCurrentName()) {
                case "index":
                    index = parseIndex(p, ctxt);
                    break;
                case "label":
                    label = parseCategoryLabel(p, ctxt);
                    break;
                case "unit":
                    // TODO: Support units.
                    parseUnit(p, ctxt);
                    break;
                default:
                    ctxt.handleUnknownProperty(
                            p,
                            this,
                            Dimension.Builder.class,
                            p.getCurrentName()
                    );
                    break;
            }
        }
        checkArgument(!(index == null && label == null), "either label or index is required");

        // Once we have everything, we can build the dimension.

        if (index == null) {
            checkArgument(label.size() >= 1, "category label must contain a least one element if " +
                    "no index is provided");
            dimension.withIndexedLabels(ImmutableMap.copyOf(label));
            return;
        }
        if (label == null) {
            dimension.withCategories(
                    ImmutableSet.copyOf(
                            index.keySet()
                    )
            );
            return;
        }

        // TODO: Maybe the checks should reside inside the builder?
        checkArgument(
                label.size() == index.size(),
                "label and index's sizes were inconsistent"
        );

        ImmutableMap<String, String> withIndex = index.keySet().stream()
                .collect(
                        toImmutableMap(
                                Function.identity(),
                                label::get
                        )
                );
        dimension.withIndexedLabels(withIndex);

    }

    private Map<String, String> parseCategoryLabel(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() != JsonToken.START_OBJECT)
            ctxt.reportWrongTokenException(
                    p, JsonToken.START_OBJECT,
                    "label was not an object", (Object) null
            );

        Map<String, String> label = p.readValueAs(LABEL_MAP);

        return checkNotNull(label, "label object was null");
    }

    private void parseUnit(JsonParser p, DeserializationContext ctxt) throws IOException {
        p.skipChildren();
    }

    /**
     * Extract the dimension name.
     */
    private String parseName(JsonParser p, DeserializationContext ctxt) throws IOException {
        // TODO: Investigate this. The current name is the key value and token is object.
        //if (p.currentToken() != JsonToken.FIELD_NAME)
        //    ctxt.reportWrongTokenException(p, JsonToken.FIELD_NAME,
        //            "could not determine dimension name", p.getCurrentToken());

        String name = checkNotNull(
                p.getCurrentName(),
                "dimension name cannot be null"
        );

        checkArgument(!name.isEmpty(), "dimension name cannot be empty");
        return name;
    }

    private String parseLabel(JsonParser p, DeserializationContext ctxt) throws IOException {
        String label = _parseString(p, ctxt);
        checkArgument(!label.trim().isEmpty(), "label cannot be empty");
        return label;
    }

    private ImmutableMap<String, String> parseIndex(JsonParser p, DeserializationContext ctxt) throws IOException {
        // Index can either be an array or object with id values. Here we transform
        // both to array.
        ImmutableMap<String, String> index = null;
        JsonToken token = p.currentToken();
        if (token == JsonToken.START_ARRAY)
            index = parseIndexAsArray(p);
        else if (token == JsonToken.START_OBJECT)
            index = parseIndexAsMap(p);
        else
            ctxt.reportMappingException("could not deserialize category index, need either an array " +
                    "or an object, got %s", token);

        return checkNotNull(index, "could not parse index");

    }

    private ImmutableMap<String, String> parseIndexAsMap(JsonParser p) throws IOException {
        ImmutableMap<String, String> index;

        Map<String, Integer> mapIndex = p.readValueAs(
                INDEX_MAP
        );

        // Even though the type is String, the sorting actually uses the
        // integer value thanks to the forMap function.
        Ordering<String> byValue = Ordering.natural().onResultOf(
                Functions.forMap(mapIndex)
        );


        index = ImmutableSortedMap.copyOf(
                Maps.transformValues(mapIndex, Object::toString),
                byValue
        );
        return index;
    }

    private ImmutableMap<String, String> parseIndexAsArray(JsonParser p) throws IOException {
        ImmutableMap<String, String> index;

        List<String> listIndex = p.readValueAs(
                INDEX_LIST);
        index = IntStream.range(0, listIndex.size())
                .boxed()
                .collect(toImmutableMap(
                        listIndex::get, Object::toString
                ));
        return index;
    }
}
