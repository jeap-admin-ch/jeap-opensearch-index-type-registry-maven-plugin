package ch.admin.bit.jeap.opensearch.registry.verifier.indextype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class MappingAnalysisCompatibilityTest {
    private static final JsonMapper MAPPER = new JsonMapper();

    @Test
    void absentAndEmptyAnalysisAreCompatible() {
        assertCompatible("{}", "{\"settings\":{\"analysis\":{\"analyzer\":{}}}}");
    }

    @Test
    void addingAnalysisRequiresNewIndexEvenIfUnused() {
        assertThat(MappingAnalysisCompatibility.differences(MAPPER.readTree("{}"), MAPPER.readTree("""
                {"settings":{"analysis":{"analyzer":{"fold":{"type":"custom","tokenizer":"standard"}}}}}
                """))).anyMatch(s -> s.contains("open indices"));
    }

    @Test
    void objectOrderIsIgnoredButFilterOrderMatters() {
        String original = """
                {"settings":{"analysis":{"analyzer":{"fold":{"type":"custom","filter":["lowercase","asciifolding"]}}}}}
                """;
        assertCompatible(original, original.replace("\"type\":\"custom\",\"filter\":[\"lowercase\",\"asciifolding\"]",
                "\"filter\":[\"lowercase\",\"asciifolding\"],\"type\":\"custom\""));
        assertThat(MappingAnalysisCompatibility.differences(MAPPER.readTree(original),
                MAPPER.readTree(original.replace("\"lowercase\",\"asciifolding\"", "\"asciifolding\",\"lowercase\"")))).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"analyzer", "normalizer", "search_analyzer", "search_quote_analyzer"})
    void existingNestedMultiFieldAnalysisCannotChange(String parameter) {
        String original = """
                {"mappings":{"properties":{"data":{"properties":{"name":{"type":"text","fields":{
                  "folded":{"type":"text","%s":"original"}
                }}}}}}}
                """.formatted(parameter);
        assertThat(MappingAnalysisCompatibility.differences(MAPPER.readTree(original),
                MAPPER.readTree(original.replace("original", "changed"))))
                .containsExactly("mappings.properties.data.properties.name.fields.folded." + parameter + " changed");
    }

    @Test
    void newFieldUsingExistingAnalysisIsCompatible() {
        assertCompatible("{\"mappings\":{\"properties\":{}}}",
                "{\"mappings\":{\"properties\":{\"name\":{\"type\":\"text\",\"analyzer\":\"standard\"}}}}");
    }

    private void assertCompatible(String previous, String current) {
        assertThat(MappingAnalysisCompatibility.differences(MAPPER.readTree(previous), MAPPER.readTree(current))).isEmpty();
    }
}
