# Mapping file

Each mapping version has its own JSON file. The file is a standard OpenSearch mapping object with
three required top-level sections.

## Required structure

```json
{
  "mappings": {
    "dynamic": false,
    "_meta": { "schema_version": 0 },
    "properties": {
      "search_item": { ... },
      "origin":      { ... },
      "data":        { ... }
    }
  }
}
```

`dynamic: false` is required — the plugin validates its presence. The three sections map to the
responsibilities of the index writer service and the domain service:

| Section       | Written by        | Purpose                                                               |
|---------------|-------------------|-----------------------------------------------------------------------|
| `search_item` | jEAP Index Writer | Indexing metadata: write timestamp, major and minor version.          |
| `origin`      | jEAP Index Writer | Reference back to the source business object.                         |
| `data`        | Domain service    | Application-defined business fields — this section is mapped to Java. |

## search_item section (fixed)

```json
"search_item": {
  "type": "object",
  "properties": {
    "upserted_at":   { "type": "date", "format": "strict_date_optional_time||epoch_millis" },
    "major_version": { "type": "integer" },
    "minor_version": { "type": "integer" }
  }
}
```

This section must conform to the bundled `IndexTypeMappingDescriptor.schema.json` validated by the
plugin.

## origin section (fixed)

```json
"origin": {
  "type": "object",
  "properties": {
    "id":        { "type": "keyword" },
    "version":   { "type": "keyword" },
    "bp_id":     { "type": "keyword" },
    "tenant":    { "type": "keyword" },
    "created":   { "type": "date", "format": "strict_date_optional_time||epoch_millis" },
    "modified":  { "type": "date", "format": "strict_date_optional_time||epoch_millis" },
    "reference": { "type": "object", "enabled": false }
  }
}
```

## data section (application-defined)

The `data` section defines the business fields specific to the index type. Field names must be
`snake_case` — the plugin rejects `camelCase` names at build time.

```json
"data": {
  "type": "object",
  "properties": {
    "document_id":    { "type": "keyword" },
    "document_title": { "type": "text" },
    "issued_by": {
      "type": "object",
      "properties": {
        "name":   { "type": "keyword" },
        "office": { "type": "keyword" }
      }
    },
    "created_at": { "type": "date", "format": "strict_date_optional_time||epoch_millis" }
  }
}
```

## OpenSearch to Java type mapping

The plugin generates Java records from the `data` section. Nested `object` fields with
sub-`properties` become inner records.

| OpenSearch type                                   | Java type              |
|---------------------------------------------------|------------------------|
| `keyword`, `text`, `wildcard`, `constant_keyword` | `String`               |
| `integer`, `short`, `byte`                        | `Integer`              |
| `long`                                            | `Long`                 |
| `float`, `half_float`                             | `Float`                |
| `double`, `scaled_float`                          | `Double`               |
| `boolean`                                         | `Boolean`              |
| `date`                                            | `java.time.Instant`    |
| `binary`                                          | `String`               |
| `object` / `nested` with no sub-`properties`      | `JsonNode`             |
| `object` / `nested` with sub-`properties`         | Generated inner record |

Fields whose JSON name differs from the Java identifier convention get a `@JsonProperty` annotation.
For example, `document_id` → `@JsonProperty("document_id") String documentId`.

## Related

- [Getting started](getting-started.md)
- [Descriptor file](descriptor-file.md)
- [Validation rules](validation-rules.md)
- [Generated artifacts](generated-artifacts.md)
- [jeap-opensearch-index-type-registry-maven-plugin](../README.md)
