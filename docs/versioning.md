# Versioning

Index type versions follow a two-level scheme: a **major version** for breaking changes and a
**minor version** for backwards-compatible additions. Both are independently tracked and enforced
by the plugin.

## Version semantics

| Change                               | Version action                   | Notes                                                                                              |
|--------------------------------------|----------------------------------|----------------------------------------------------------------------------------------------------|
| Add a new field to `data`            | Increment **minor** version      | New mapping file. Existing data records remain compatible.                                         |
| Remove a field from `data`           | Increment **major** version      | New mapping file, new data record class (`DataV<n>`), new `IndexType` singleton (`IndexTypeV<n>`). |
| Rename a field in `data`             | Increment **major** version      | Treated as remove + add.                                                                           |
| Change a field type                  | Increment **major** version      | Incompatible with existing documents.                                                              |
| Add a new collection field           | Increment **minor** version      | Add the field and its path in `collection_fields` in the new mapping.                              |
| Change an existing field cardinality | Increment **major** version      | Changing between `T` and `List<T>` changes the generated Java API.                                |
| Reorder existing fields              | Increment **major** version      | Record constructor parameter order is part of the generated Java API.                              |
| Add or remove object `properties`    | Increment **major** version      | Changes the generated Java type between `JsonNode` and a record.                                   |
| Add a new index type                 | New descriptor + initial mapping | New per-type artifact.                                                                             |
| Delete an index type                 | **Not allowed**                  | Build fails — index types on trunk are immutable.                                                  |
| Modify an existing mapping file      | **Not allowed**                  | Build fails — detected by CRC32 checksum.                                                          |
| Change `roles` without a new mapping | **Not allowed**                  | Role changes require a new mapping version.                                                        |

## Artifact versioning

Generated artifact versions follow:

| Branch         | Version format                                | Example                   |
|----------------|-----------------------------------------------|---------------------------|
| Trunk          | `<major>.<minor>`                             | `1.2`                     |
| Feature branch | `<major>.<minor>-<sanitized-branch>-SNAPSHOT` | `1.2-my-feature-SNAPSHOT` |

The `-v<major>` suffix in the artifactId allows a service to depend on multiple major versions of
the same index type simultaneously:

```xml
<!-- Search across both versions at runtime -->
<dependency>
    <groupId>ch.admin.bit.jme.indextype.jme</groupId>
    <artifactId>jme-decree-document-v1</artifactId>
    <version>1.2</version>
</dependency>
<dependency>
    <groupId>ch.admin.bit.jme.indextype.jme</groupId>
    <artifactId>jme-decree-document-v2</artifactId>
    <version>2.0</version>
</dependency>
```

## Major version workflow

1. Add a new mapping file with an incremented major version (e.g. `JmeDecreeDocument_mapping_v2_0.json`).
2. Add the new version entry to the descriptor's `mappingVersions`.
3. The plugin generates a new data record (`JmeDecreeDocumentDataV2`) and a new singleton (`JmeDecreeDocumentIndexTypeV2`), packaged in a new artifact (`jme-decree-document-v2`).
4. The v1 artifact continues to be published unchanged — services can migrate at their own pace.
5. Once all consumers have migrated to v2, the v1 index can be dropped from OpenSearch (this is an operational step outside the registry).

## Minor version workflow

1. Add a new field to the `data` section of the latest minor version's mapping file — but remember that existing mapping files are immutable. Create a new mapping file instead (e.g. `JmeDecreeDocument_mapping_v1_1.json`).
2. Add the new version entry to the descriptor.
3. The plugin regenerates the data record for that major version with the new field, and updates the artifact version to `<major>.<new-minor>`.
4. On startup, the index writer service detects the version change and pushes the updated mapping to the current write index automatically.

Changing only `collection_fields` for an existing property is not a compatible minor change. Create
a new major mapping instead. Existing mapping files, including their `_meta` section, remain
immutable after publication.

This rule also applies to `nested` fields: adding or removing their path changes the generated Java
type between a single record and `List<Record>`.

## Related

- [Getting started](getting-started.md)
- [Validation rules](validation-rules.md)
- [Generated artifacts](generated-artifacts.md)
- [jeap-opensearch-index-type-registry-maven-plugin](../README.md)
