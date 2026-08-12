package ch.admin.bit.jeap.opensearch.registry.verifier.indextype;

import ch.admin.bit.jeap.opensearch.registry.verifier.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

class MappingCollectionFieldsValidatorTest {

    @Test
    void objectWithPropertiesDeclaredAsCollectionProducesWarning(@TempDir File dir) throws IOException {
        File mappingFile = new File(dir, "mapping.json");
        Files.writeString(mappingFile.toPath(), """
                {
                  "mappings": {
                    "_meta": { "jeap": { "collection_fields": ["items"] } },
                    "properties": {
                      "data": {
                        "properties": {
                          "items": {
                            "type": "object",
                            "properties": { "name": { "type": "keyword" } }
                          }
                        }
                      }
                    }
                  }
                }
                """);

        ValidationResult result = MappingCollectionFieldsValidator.validate(mappingFile);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getWarnings()).singleElement().asString()
                .contains("items", "object", "nested", "correlation");
    }

    @Test
    void nestedFieldDeclaredAsCollectionDoesNotProduceWarning(@TempDir File dir) throws IOException {
        File mappingFile = new File(dir, "mapping.json");
        Files.writeString(mappingFile.toPath(), """
                {
                  "mappings": {
                    "_meta": { "jeap": { "collection_fields": ["items"] } },
                    "properties": {
                      "data": {
                        "properties": {
                          "items": {
                            "type": "nested",
                            "properties": { "name": { "type": "keyword" } }
                          }
                        }
                      }
                    }
                  }
                }
                """);

        ValidationResult result = MappingCollectionFieldsValidator.validate(mappingFile);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getWarnings()).isEmpty();
    }
}
