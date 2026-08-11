package ch.admin.bit.jeap.opensearch.registry;

import tools.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public record MappingDataFieldModel(Set<String> declaredCollectionFields,
                                    Map<String, FieldContract> fieldsByPath) {

    public static MappingDataFieldModel from(JsonNode mapping) {
        Set<String> collectionFields = extractCollectionFields(mapping);
        Map<String, FieldContract> fields = new LinkedHashMap<>();
        JsonNode dataProperties = mapping.path("mappings").path("properties").path("data").path("properties");
        collectFields(dataProperties, "", collectionFields, fields);
        return new MappingDataFieldModel(
                Collections.unmodifiableSet(new LinkedHashSet<>(collectionFields)),
                Collections.unmodifiableMap(new LinkedHashMap<>(fields)));
    }

    private static Set<String> extractCollectionFields(JsonNode mapping) {
        JsonNode configuredFields = mapping.path("mappings").path("_meta").path("jeap").path("collection_fields");
        Set<String> fields = new LinkedHashSet<>();
        if (configuredFields.isArray()) {
            configuredFields.forEach(field -> fields.add(field.asString()));
        }
        return fields;
    }

    private static void collectFields(JsonNode properties, String parentPath, Set<String> collectionFields,
                                      Map<String, FieldContract> fields) {
        if (!properties.isObject()) {
            return;
        }
        properties.properties().forEach(entry -> {
            String path = parentPath.isEmpty() ? entry.getKey() : parentPath + "." + entry.getKey();
            JsonNode definition = entry.getValue();
            String type = definition.path("type").asString("object");
            boolean collection = "nested".equals(type) || collectionFields.contains(path);
            fields.put(path, new FieldContract(type, collection));
            if ("object".equals(type) || "nested".equals(type)) {
                collectFields(definition.path("properties"), path, collectionFields, fields);
            }
        });
    }

    public record FieldContract(String openSearchType, boolean collection) {
    }
}
