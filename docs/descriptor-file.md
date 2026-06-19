# Descriptor file

Each index type has exactly one descriptor JSON file. It declares the system, roles, and the list
of all mapping versions that have ever been published for this index type.

## Format

```json
{
  "system": "JME",
  "originType": "JmeDecreeDocument",
  "description": "Indexes decree documents from the JME domain service.",
  "documentationUrl": "https://confluence.example.com/display/JME/DecreeDocument",
  "roles": [
    "jme_read"
  ],
  "mappingVersions": [
    { "major": 1, "minor": 0, "mappingDefinition": "JmeDecreeDocument_mapping_v1_0.json" },
    { "major": 1, "minor": 1, "mappingDefinition": "JmeDecreeDocument_mapping_v1_1.json" },
    { "major": 2, "minor": 0, "mappingDefinition": "JmeDecreeDocument_mapping_v2_0.json" }
  ]
}
```

## Fields

| Field              | Required  | Description                                                                                                                                                 |
|--------------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `system`           | Yes       | System identifier (free text, e.g. `"JME"`). Must match the system directory name (case-insensitive).                                                       |
| `originType`       | Yes       | Name of the originating business type (PascalCase). Must start with the system name in title case. Used as the `originType()` of the generated `IndexType`. |
| `description`      | Yes       | Human-readable description of what is indexed.                                                                                                              |
| `roles`            | Yes (≥ 1) | jEAP roles required to read documents of this index type. Declared on the generated `IndexType` and enforced by the search client.                          |
| `mappingVersions`  | Yes (≥ 1) | All mapping versions ever published. Each entry must reference a mapping file in the same directory.                                                        |
| `documentationUrl` | No        | Link to further documentation (e.g. Confluence).                                                                                                            |

## mappingVersions entries

| Field               | Required | Description                                                                           |
|---------------------|----------|---------------------------------------------------------------------------------------|
| `major`             | Yes      | Major version number. Increment for breaking changes (removed or renamed fields).     |
| `minor`             | Yes      | Minor version number. Increment for backwards-compatible changes (added fields only). |
| `mappingDefinition` | Yes      | Filename of the corresponding mapping JSON file (relative, in the same directory).    |

## Role changes

The `roles` field may only be changed when at least one new mapping version (major or minor) is
introduced at the same time. Changing roles without adding a new mapping version fails the build.

## Related

- [Getting started](getting-started.md)
- [Registry structure](registry-structure.md)
- [Mapping file](mapping-file.md)
- [Validation rules](validation-rules.md)
- [jeap-opensearch-index-type-registry-maven-plugin](../README.md)
