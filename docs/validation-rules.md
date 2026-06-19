# Validation rules

The `registry` goal enforces the following rules on every build. Violations fail the build with a
descriptive error message.

## Naming and structure

| Rule                                                                                       | Details                                             |
|--------------------------------------------------------------------------------------------|-----------------------------------------------------|
| System directory must be lowercase                                                         | `jme` ✓  `JME` ✗                                    |
| System directory name must match the `system` field in the descriptor                      | Case-insensitive comparison.                        |
| Index type directory name must equal the descriptor filename (lowercased, without `.json`) | `jmedecreedocument/JmeDecreeDocument.json` ✓        |
| Index type name (`originType`) must start with the system name in title case               | System `JME` → origin type must start with `Jme`.   |
| Only descriptor and versioned mapping files may exist in an index type directory           | No extra files, READMEs, or subdirectories allowed. |

## Schema validation

| Rule                                                                             | Details                                                                 |
|----------------------------------------------------------------------------------|-------------------------------------------------------------------------|
| Every descriptor file is validated against `IndexTypeDescriptor.schema.json`     | Missing required fields or wrong types fail the build.                  |
| Every mapping file is validated against `IndexTypeMappingDescriptor.schema.json` | Incorrect `search_item` or `origin` sections fail the build.            |
| All field names in `data.properties` (recursively) must be snake_case            | Pattern: `[a-z][a-z0-9]*(_[a-z0-9]+)*`. camelCase names fail the build. |
| `dynamic: false` must be set                                                     | Required in all mapping files.                                          |

The snake_case rule is strictly enforced because the index writer service serialises documents
using `PropertyNamingStrategies.SNAKE_CASE`. A camelCase field in the mapping would never match
the data actually written to OpenSearch.

## Immutability (compared to trunk branch)

The plugin clones the configured `gitUrl` at `trunkBranchName` and compares the current working
tree against it. Three immutability rules apply:

| Rule                                    | Details                                 |
|-----------------------------------------|-----------------------------------------|
| No system directory may be deleted      | Removing a system fails the build.      |
| No index type directory may be deleted  | Removing an index type fails the build. |
| No existing mapping file may be changed | Detected by CRC32 checksum comparison.  |

To bypass trunk comparison during local development, set `gitUrl` to empty:
```bash
mvn verify -Dindextype.git.url=
```

## Minor version backward compatibility

Within the same major version, each successive minor version may only **add** properties to the
`data` section — never remove them. This is checked for every adjacent minor version pair
(e.g. v1.0→v1.1, v1.1→v1.2):

| Change                     | Allowed in minor version | Requires major version bump |
|----------------------------|--------------------------|-----------------------------|
| Add a new field to `data`  | Yes                      | No                          |
| Remove a field from `data` | No                       | Yes                         |
| Rename a field in `data`   | No                       | Yes                         |
| Change a field type        | No                       | Yes                         |

## Role changes

The `roles` field in the descriptor may only be changed when at least one new mapping version
(major or minor) is introduced at the same time. Changing roles without adding a new version fails
the build.

## Related

- [Getting started](getting-started.md)
- [Descriptor file](descriptor-file.md)
- [Mapping file](mapping-file.md)
- [Versioning](versioning.md)
- [jeap-opensearch-index-type-registry-maven-plugin](../README.md)
