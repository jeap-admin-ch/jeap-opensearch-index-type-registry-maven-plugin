package ch.admin.bit.jeap.opensearch.registry.verifier.indextype;

import ch.admin.bit.jeap.opensearch.registry.MappingDataFieldModel;
import ch.admin.bit.jeap.opensearch.registry.verifier.ValidationResult;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.List;

class MappingCollectionFieldsValidator {
    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    static ValidationResult validate(File mappingFile) {
        if (!mappingFile.isFile()) {
            return ValidationResult.ok();
        }
        try {
            JsonNode mapping = JSON_MAPPER.readTree(mappingFile);
            MappingDataFieldModel model = MappingDataFieldModel.from(mapping);
            List<String> unresolved = model.declaredCollectionFields().stream()
                    .filter(path -> !model.fieldsByPath().containsKey(path))
                    .sorted()
                    .toList();
            if (unresolved.isEmpty()) {
                return ValidationResult.ok();
            }
            return ValidationResult.fail(
                    "Mapping file '%s' contains a collection field path that does not resolve to a data property: %s"
                            .formatted(mappingFile.getName(), unresolved));
        } catch (JacksonIOException | StreamReadException _) {
            return ValidationResult.ok();
        }
    }
}
