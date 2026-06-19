# Registry structure

The index type registry is a Git repository. The plugin reads all index type definitions from a
configurable `descriptorDirectory` (default: `index-types/`).

## Directory layout

```
index-types/
└── <system>/                        lowercase system name
    └── <indextypename>/             lowercase, no separators
        ├── <IndexTypeName>.json     descriptor (PascalCase)
        ├── <IndexTypeName>_mapping_v1_0.json
        ├── <IndexTypeName>_mapping_v1_1.json
        └── <IndexTypeName>_mapping_v2_0.json
```

## Naming conventions

| Element              | Convention                                       | Example                               |
|----------------------|--------------------------------------------------|---------------------------------------|
| System directory     | Lowercase                                        | `jme`                                 |
| Index type directory | Lowercase, no separators                         | `jmedecreedocument`                   |
| Descriptor file      | PascalCase, `.json` extension                    | `JmeDecreeDocument.json`              |
| Mapping file         | `<DescriptorName>_mapping_v<major>_<minor>.json` | `JmeDecreeDocument_mapping_v1_0.json` |

## Naming rules enforced by the plugin

- The system directory name must be all-lowercase.
- The system directory name must match the `system` field in the descriptor (case-insensitive).
- The index type directory name must equal the descriptor filename lowercased (without `.json`).
- The index type name (`originType`) must start with the system name in title case — for system `JME` the origin type must start with `Jme`.
- Only the descriptor JSON and versioned mapping files may exist inside an index type directory. No other files are allowed.

## Example

For system `JME` and business object `DecreeDocument`:

```
index-types/
└── jme/
    └── jmedecreedocument/
        ├── JmeDecreeDocument.json              ← descriptor
        ├── JmeDecreeDocument_mapping_v1_0.json ← initial version
        └── JmeDecreeDocument_mapping_v1_1.json ← compatible update
```

## Related

- [Getting started](getting-started.md)
- [Descriptor file](descriptor-file.md)
- [Mapping file](mapping-file.md)
- [Validation rules](validation-rules.md)
- [jeap-opensearch-index-type-registry-maven-plugin](../README.md)
