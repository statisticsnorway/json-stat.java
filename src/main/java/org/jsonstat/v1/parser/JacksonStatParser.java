package org.jsonstat.v1.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.jsonstat.v1.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;

public class JacksonStatParser {
    private ObjectMapper mapper;

    public JacksonStatParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JacksonStatParser() {
        this(new ObjectMapper());
    }

    public Stat parse(InputStream stream) throws IOException {
        try(InputStream is = stream) {
            JsonNode tree = mapper.readTree(is);
            return parse((ObjectNode)tree);
        }
    }

    private Stat parse(ObjectNode tree) {
        Iterator<Map.Entry<String,JsonNode>> fields = tree.fields();
        List<Dataset> datasets = new ArrayList<>();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> next = fields.next();
            datasets.add(parseDataset(next));
        }
        return new Stat(datasets);
    }

    private Dataset parseDataset(Map.Entry<String, JsonNode> entry) {
        JsonNode node = entry.getValue();
        Optional<String> label = Optional.empty();
        Optional<Instant> updated = Optional.empty();
        List<Data> values = Lists.newArrayList();
        if (node.has("label")) {
            label = Optional.ofNullable(node.get("label").asText());
        }
        if (node.hasNonNull("updated")) {
            updated = Optional.ofNullable(Instant.parse(node.get("updated").asText()));
        }

        if (node.hasNonNull("value")) {
            for(JsonNode v : node.get("value")) {
                Object value;
                if (v.isNumber()) {
                    value = v.decimalValue();
                } else {
                    value = v.asText();
                }
                if (value != null) {
                    values.add(new Data(value, Optional.empty())); //Handle status...
                }
            }
        }
        Map<String, Dimension> dimensions = new LinkedHashMap<>();

        if (node.hasNonNull("dimension")) {
            JsonNode dims = node.get("dimension");
            JsonNode ids = dims.get("id");
            JsonNode sizes = dims.get("size");
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i).asText();
                int currentSize = sizes.get(i).intValue();
                if (dims.hasNonNull(id)) {
                    JsonNode dimension = dims.get(id);
                    dimensions.put(id, parseDimension(i, id, currentSize, dimension));
                }
            }
        }

        return new Dataset(entry.getKey(), label, values, updated, dimensions);
    }

    private Dimension parseDimension(int index, String id, int currentSize, JsonNode dimension) {
        Optional<String> label = Optional.empty();

        if (dimension.has("label")) {
            label = Optional.ofNullable(dimension.get("label").asText());
        }
        JsonNode category = dimension.get("category");


        return new Dimension(index, id, currentSize, label, parseCategory(category), Optional.<Role>empty()); //handle roles
    }

    private Category parseCategory(JsonNode category) {
        Map<String, Integer> indices = new LinkedHashMap<>();
        Map<String, String> labels = new LinkedHashMap<>();
        Map<String, List<String>> children = new LinkedHashMap<>();
        if (category != null) {
            if (category.has("index")) {
                JsonNode index = category.get("index");
                if (index.isArray()) {
                    int i = 0;
                    for (JsonNode id : index) {
                        indices.put(id.asText(), i);
                        i++;
                    }
                }
                Iterator<Map.Entry<String, JsonNode>> fields = index.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    indices.put(entry.getKey(), entry.getValue().intValue());
                }
            }
            if (category.has("label")) {
                JsonNode label = category.get("label");
                Iterator<Map.Entry<String, JsonNode>> fields = label.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    labels.put(entry.getKey(), entry.getValue().asText());
                }
            }
            if (category.has("child")) {
                JsonNode child = category.get("child");
                Iterator<Map.Entry<String, JsonNode>> fields = child.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    List<String> c = new ArrayList<>();
                    for (JsonNode node : entry.getValue()) {
                        c.add(node.asText());
                    }
                    children.put(entry.getKey(), c);
                }
            }
        }
        return new Category(indices, labels, children);
    }
}
