# Mapping file

Each mapping version has its own JSON file. The file is a standard OpenSearch mapping object with
three required top-level sections.

## Required structure

```json
{
  "mappings": {
    "dynamic": false,
    "_meta": {
      "jeap": {
        "collection_fields": ["keywords"]
      }
    },
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

## Collection fields

OpenSearch has no separate array mapping type: the same field mapping accepts either one value or
an array. Use `mappings._meta.jeap.collection_fields` to declare fields that the plugin must generate
as `java.util.List<T>`:

```json
"_meta": {
  "jeap": {
    "collection_fields": [
      "keywords",
      "details.codes",
      "cases.tags"
    ]
  }
}
```

Paths are relative to `data.properties`, use the JSON snake_case field names, and descend through
`properties`. A parent collection does not change the cardinality of its children: `cases` and
`cases.tags` describe two independent collection fields.

Fields mapped as `nested` are collections by default and do not need to be listed. All other fields
are single-valued unless listed in `collection_fields`. The declaration controls the generated Java
type only; OpenSearch mapping and query semantics remain defined by each field's `type`.

Requiredness, null checks, and non-empty collection checks are not expressed by this metadata.

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

The plugin generates Java records from the `data` section. `object` and `nested` fields with
sub-`properties` become inner records, declared inside the record of the field they belong to, at
any nesting depth.

| OpenSearch type                                   | Java type                      |
|---------------------------------------------------|--------------------------------|
| `keyword`, `text`, `wildcard`, `constant_keyword` | `String`                       |
| `integer`, `short`, `byte`                        | `Integer`                      |
| `long`                                            | `Long`                         |
| `float`, `half_float`                             | `Float`                        |
| `double`, `scaled_float`                          | `Double`                       |
| `boolean`                                         | `Boolean`                      |
| `date`                                            | `java.time.Instant`            |
| `binary`                                          | `String`                       |
| `object` with no sub-`properties`                 | `JsonNode`                     |
| `object` with sub-`properties`                    | Generated inner record         |
| `nested` with no sub-`properties`                 | `List<JsonNode>`               |
| `nested` with sub-`properties`                    | `List<generated inner record>` |

Any non-`nested` type in `collection_fields` is wrapped in `List`, for example `keyword` becomes
`List<String>` and `object` with sub-properties becomes `List<generated inner record>`.

Fields whose JSON name differs from the Java identifier convention get a `@JsonProperty` annotation.
For example, `document_id` → `@JsonProperty("document_id") String documentId`.

### `object` versus `nested`

Use `nested` when the field holds an **array** of objects whose sub-fields must stay correlated —
that is what OpenSearch `nested` is for, and it is why the generator maps it to a `List`. Use
`object` for a single object. Declaring `object` for a field that carries an array makes OpenSearch
flatten the array and lose the correlation between sibling values, and the generated record will
reject the payload.

```json
"cases": {
  "type": "nested",
  "properties": {
    "case_reference": { "type": "keyword" },
    "control_pattern": {
      "type": "object",
      "properties": {
        "factual_name": { "type": "text" }
      }
    }
  }
}
```

```java
public record MyTypeDataV1(
    List<Cases> cases
) {

    public record Cases(
        @JsonProperty("case_reference") String caseReference,
        @JsonProperty("control_pattern") ControlPattern controlPattern
    ) {

        public record ControlPattern(
            @JsonProperty("factual_name") String factualName
        ) {}
    }
}
```

## Related

- [Getting started](getting-started.md)
- [Descriptor file](descriptor-file.md)
- [Validation rules](validation-rules.md)
- [Generated artifacts](generated-artifacts.md)
- [jeap-opensearch-index-type-registry-maven-plugin](../README.md)
