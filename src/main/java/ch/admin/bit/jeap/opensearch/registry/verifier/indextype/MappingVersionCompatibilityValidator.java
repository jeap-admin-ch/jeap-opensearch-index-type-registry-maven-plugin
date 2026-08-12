package ch.admin.bit.jeap.opensearch.registry.verifier.indextype;

import ch.admin.bit.jeap.opensearch.registry.MappingDataFieldModel;
import ch.admin.bit.jeap.opensearch.registry.MappingDataFieldModel.FieldContract;
import ch.admin.bit.jeap.opensearch.registry.verifier.ValidationResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates minor version compatibility: within the same major version,
 * newer minor versions must only ADD new optional properties to the data section
 * (backward compatible changes). Existing properties must not be removed or modified.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class MappingVersionCompatibilityValidator {
    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    private final File indexTypeDir;
    private final String indexTypeName;

    static ValidationResult validate(File indexTypeDir, String indexTypeName,
                                     List<MappingVersionRef> versions) {
        return new MappingVersionCompatibilityValidator(indexTypeDir, indexTypeName)
                .validateCompatibility(versions);
    }

    private ValidationResult validateCompatibility(List<MappingVersionRef> versions) {
        // Group by major version
        Map<Integer, List<MappingVersionRef>> byMajor = new java.util.TreeMap<>();
        for (MappingVersionRef v : versions) {
            byMajor.computeIfAbsent(v.major(), k -> new ArrayList<>()).add(v);
        }

        // For each major version, check minor version compatibility
        return byMajor.values().stream()
                .map(this::validateMajorGroup)
                .reduce(ValidationResult.ok(), ValidationResult::merge);
    }

    private ValidationResult validateMajorGroup(List<MappingVersionRef> versionGroup) {
        // Sort by minor version
        List<MappingVersionRef> sorted = versionGroup.stream()
                .sorted(java.util.Comparator.comparingInt(MappingVersionRef::minor))
                .toList();

        ValidationResult result = ValidationResult.ok();
        for (int i = 1; i < sorted.size(); i++) {
            MappingVersionRef previous = sorted.get(i - 1);
            MappingVersionRef current = sorted.get(i);
            result = ValidationResult.merge(result, checkMinorCompatibility(previous, current));
        }
        return result;
    }

    private ValidationResult checkMinorCompatibility(MappingVersionRef previous, MappingVersionRef current) {
        File prevFile = new File(indexTypeDir, previous.mappingDefinition());
        File currFile = new File(indexTypeDir, current.mappingDefinition());

        MappingDataFieldModel previousModel;
        MappingDataFieldModel currentModel;
        try {
            previousModel = MappingDataFieldModel.from(JSON_MAPPER.readTree(prevFile));
            currentModel = MappingDataFieldModel.from(JSON_MAPPER.readTree(currFile));
        } catch (JacksonIOException e) {
            return ValidationResult.fail("Cannot read mapping file for compatibility check: " + e.getMessage());
        }

        return validateFieldContracts(previousModel, currentModel, previous, current);
    }

    private ValidationResult validateFieldContracts(MappingDataFieldModel previousModel,
                                                    MappingDataFieldModel currentModel,
                                                    MappingVersionRef previous, MappingVersionRef current) {
        List<String> removedProperties = new ArrayList<>();
        List<String> changedTypes = new ArrayList<>();
        List<String> changedCardinalities = new ArrayList<>();
        List<String> changedRepresentations = new ArrayList<>();

        previousModel.fieldsByPath().forEach((path, previousField) -> {
            FieldContract currentField = currentModel.fieldsByPath().get(path);
            if (currentField == null) {
                removedProperties.add(path);
            } else {
                if (!previousField.openSearchType().equals(currentField.openSearchType())) {
                    changedTypes.add("%s (%s -> %s)".formatted(
                            path, previousField.openSearchType(), currentField.openSearchType()));
                }
                if (previousField.collection() != currentField.collection()) {
                    changedCardinalities.add("%s (%s -> %s)".formatted(path,
                            cardinality(previousField), cardinality(currentField)));
                }
                if (previousField.structured() != currentField.structured()) {
                    changedRepresentations.add("%s (%s -> %s)".formatted(path,
                            representation(previousField), representation(currentField)));
                }
            }
        });

        List<String> incompatibilities = new ArrayList<>();
        if (!removedProperties.isEmpty()) {
            incompatibilities.add("data properties were removed: " + String.join(", ", removedProperties));
        }
        if (!changedTypes.isEmpty()) {
            incompatibilities.add("data property types changed: " + String.join(", ", changedTypes));
        }
        if (!changedCardinalities.isEmpty()) {
            incompatibilities.add("data property cardinality changed: " + String.join(", ", changedCardinalities));
        }
        if (!changedRepresentations.isEmpty()) {
            incompatibilities.add(
                    "generated data property representation changed: " + String.join(", ", changedRepresentations));
        }
        List<String> previousPaths = previousModel.fieldsByPath().keySet().stream()
                .filter(currentModel.fieldsByPath()::containsKey)
                .toList();
        List<String> currentPaths = currentModel.fieldsByPath().keySet().stream()
                .filter(previousModel.fieldsByPath()::containsKey)
                .toList();
        if (!previousPaths.equals(currentPaths)) {
            List<String> movedFields = previousPaths.stream()
                    .filter(path -> previousPaths.indexOf(path) != currentPaths.indexOf(path))
                    .map(path -> "%s (%d -> %d)".formatted(
                            path, previousPaths.indexOf(path) + 1, currentPaths.indexOf(path) + 1))
                    .toList();
            incompatibilities.add("existing data properties were reordered: " + String.join(", ", movedFields));
        }
        if (incompatibilities.isEmpty()) {
            return ValidationResult.ok();
        }
        return ValidationResult.fail(
                "Minor version %s is not backward compatible with v%d.%d for index type '%s': %s. "
                        .formatted(current.versionLabel(), previous.major(), previous.minor(), indexTypeName,
                                String.join("; ", incompatibilities)));
    }

    private String cardinality(FieldContract field) {
        return field.collection() ? "collection" : "single value";
    }

    private String representation(FieldContract field) {
        return field.structured() ? "generated record" : "JsonNode";
    }

    record MappingVersionRef(int major, int minor, String mappingDefinition) {
        String versionLabel() {
            return "v%d.%d".formatted(major, minor);
        }
    }
}
