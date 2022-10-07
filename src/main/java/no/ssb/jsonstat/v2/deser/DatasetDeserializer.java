package no.ssb.jsonstat.v2.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import no.ssb.jsonstat.v2.Dataset;
import no.ssb.jsonstat.v2.DatasetBuildable;
import no.ssb.jsonstat.v2.DatasetBuilder;
import no.ssb.jsonstat.v2.Dimension;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Deserializer for Dataset.
 * <p>
 * TODO: Use builder instead.
 * TODO: Check {@link com.fasterxml.jackson.databind.deser.ResolvableDeserializer}
 */
public class DatasetDeserializer extends StdDeserializer<DatasetBuildable> {

    static final TypeReference<List<Number>> VALUES_LIST = new TypeReference<List<Number>>() {
    };
    static final TypeReference<Map<String, Dimension.Builder>> DIMENSION_MAP = new TypeReference<Map<String, Dimension.Builder>>() {
    };
    static final TypeReference<ImmutableSet<String>> ID_SET = new TypeReference<ImmutableSet<String>>() {
    };
    static final TypeReference<List<Integer>> SIZE_LIST = new TypeReference<List<Integer>>() {
    };
    static final TypeReference<ArrayListMultimap<String, String>> ROLE_MULTIMAP = new TypeReference<ArrayListMultimap<String, String>>() {
    };
    static final TypeReference<?> VALUES_MAP = new TypeReference<TreeMap<Integer, Number>>() {
    };

    static final DateTimeFormatter ECMA_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("uuuu").optionalStart().appendPattern("-MM").optionalStart().appendPattern("-dd")
            .optionalEnd()
            .optionalEnd()
            .optionalStart().appendLiteral("T").appendPattern("HH:mm").optionalStart().appendPattern(":ss")
            .optionalStart().appendPattern(".SSS").optionalEnd().optionalEnd().optionalStart()
            .appendPattern("z").optionalEnd().optionalEnd()
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 1)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
            .parseDefaulting(ChronoField.OFFSET_SECONDS, 0).toFormatter();

    public DatasetDeserializer() {
        super(DatasetBuildable.class);
    }

    Instant parseEcmaDate(String value) {
        return Instant.from(ECMA_FORMATTER.parse(value));
    }

    @Override
    public Collection<Object> getKnownPropertyNames() {
        return Arrays.asList(
                "class", "version", "label",
                "source", "updated", "id",
                "size", "dimension", "value",
                "link", "status", "extension"
        );
    }

    @Override
    public DatasetBuildable deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.getCurrentToken() == JsonToken.START_OBJECT) {
            p.nextToken();
        }

        Set<String> ids = Collections.emptySet();
        List<Integer> sizes = Collections.emptyList();
        Multimap<String, String> roles = ArrayListMultimap.create();
        Map<String, Dimension.Builder> dims = Collections.emptyMap();
        List<Number> values = Collections.emptyList();


        DatasetBuilder builder = Dataset.create();
        Optional<String> version = Optional.empty();
        Optional<String> clazz = Optional.empty();
        Optional<ObjectNode> extension = Optional.empty();

        while (p.nextValue() != JsonToken.END_OBJECT) {
            switch (p.getCurrentName()) {
                case "label":
                    builder.withLabel(_parseString(p, ctxt));
                    break;
                case "source":
                    builder.withSource(_parseString(p, ctxt));
                    break;
                case "href":
                    break;
                case "updated":
                    Instant updated = parseEcmaDate(_parseString(p, ctxt));
                    builder.updatedAt(updated);
                    break;
                case "value":
                    values = parseValues(p, ctxt);
                    break;
                case "dimension":
                    if (!version.orElse("1.x").equals("2.0")) {
                        dims = Maps.newHashMap();
                        // Deal with the id, size and role inside dimension.
                        while (p.nextValue() != JsonToken.END_OBJECT) {
                            switch (p.getCurrentName()) {
                                case "id":
                                    ids = p.readValueAs(ID_SET);
                                    break;
                                case "size":
                                    sizes = p.readValueAs(SIZE_LIST);
                                    break;
                                case "role":
                                    roles = p.readValueAs(ROLE_MULTIMAP);
                                    break;
                                default:
                                    dims.put(
                                            p.getCurrentName(),
                                            ctxt.readValue(p, Dimension.Builder.class)
                                    );
                            }
                        }
                    } else {
                        dims = p.readValueAs(DIMENSION_MAP);
                    }
                    break;
                case "id":
                    ids = p.readValueAs(ID_SET);
                    break;
                case "size":
                    sizes = p.readValueAs(SIZE_LIST);
                    break;
                case "role":
                    roles = p.readValueAs(ROLE_MULTIMAP);
                    break;
                case "extension":
                    extension = Optional.of(ctxt.readValue(
                            p, ObjectNode.class
                    ));
                    break;
                case "link":
                case "status":
                    // TODO
                    p.skipChildren();
                    break;
                case "version":
                    version = Optional.of(_parseString(p, ctxt));
                    break;
                case "class":
                    // TODO
                    clazz = Optional.of(_parseString(p, ctxt));
                    break;
                default:
                    boolean handled = ctxt.handleUnknownProperty(
                            p, this, Dimension.Builder.class, p.getCurrentName()
                    );
                    if (!handled)
                        p.skipChildren();
                    break;
            }
        }


        // Setup roles
        for (Map.Entry<String, String> dimRole : roles.entries()) {
            Dimension.Roles role = Dimension.Roles.valueOf(
                    dimRole.getKey().toUpperCase()
            );
            Dimension.Builder dimension = checkNotNull(
                    dims.get(dimRole.getValue()),
                    "could not assign the role {} to the dimension {}. The dimension did not exist",
                    role, dimRole.getValue()

            );
            dimension.withRole(role);
        }

        List<Dimension.Builder> orderedDimensions = Lists.newArrayList();
        for (String dimensionName : ids) {
            orderedDimensions.add(dims.get(dimensionName));
        }

        // TODO: Check size?

        // Check ids and add to the data set.
        checkArgument(ids.size() == dims.size(),
                "dimension and size did not match"
        );

        if (extension.isPresent()) {
            builder.withExtension(extension.get());
        }

        return builder.withDimensions(orderedDimensions).withValues(values);
    }

    List<Number> parseValues(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<Number> result = Collections.emptyList();
        switch (p.getCurrentToken()) {
            case START_OBJECT:
                SortedMap<Integer, Number> map = p.readValueAs(VALUES_MAP);
                result = new AbstractList<Number>() {

                    @Override
                    public int size() {
                        return map.lastKey() + 1;
                    }

                    @Override
                    public Number get(int index) {
                        return map.get(index);
                    }
                };
                break;
            case START_ARRAY:
                result = p.readValueAs(VALUES_LIST);
                break;
            default:
                ctxt.handleUnexpectedToken(
                        this._valueClass, p.getCurrentToken(), p, "msg"
                );
        }
        return result;
    }

}
