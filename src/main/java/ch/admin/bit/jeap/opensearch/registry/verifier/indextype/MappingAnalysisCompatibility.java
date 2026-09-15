package ch.admin.bit.jeap.opensearch.registry.verifier.indextype;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.ArrayList;
import java.util.List;

final class MappingAnalysisCompatibility {

    private static final List<String> ANALYSIS_PARAMETERS =
            List.of("analyzer", "normalizer", "search_analyzer", "search_quote_analyzer");

    private MappingAnalysisCompatibility() {
    }

    static List<String> differences(JsonNode previous, JsonNode current) {
        List<String> differences = new ArrayList<>();
        JsonNode previousAnalysis = normalizedAnalysis(previous.path("settings").path("analysis"));
        JsonNode currentAnalysis = normalizedAnalysis(current.path("settings").path("analysis"));
        if (!previousAnalysis.equals(currentAnalysis)) {
            differences.add("settings.analysis changed; analysis settings cannot be updated on open indices");
        }
        compareFields(previous.path("mappings"), current.path("mappings"), "mappings", differences);
        return differences;
    }

    private static JsonNode normalizedAnalysis(JsonNode analysis) {
        var normalized = JsonNodeFactory.instance.objectNode();
        analysis.properties().forEach(entry -> {
            if (!entry.getValue().isEmpty()) {
                normalized.set(entry.getKey(), entry.getValue());
            }
        });
        return normalized;
    }

    private static void compareFields(JsonNode previous, JsonNode current, String path, List<String> differences) {
        for (String parameter : ANALYSIS_PARAMETERS) {
            if (!previous.path(parameter).equals(current.path(parameter))) {
                differences.add(path + "." + parameter + " changed");
            }
        }
        for (String section : List.of("properties", "fields")) {
            previous.path(section).properties().forEach(field -> {
                JsonNode next = current.path(section).path(field.getKey());
                if (!next.isMissingNode()) {
                    compareFields(field.getValue(), next, path + "." + section + "." + field.getKey(), differences);
                }
            });
        }
    }
}
