# jeap-opensearch-index-type-registry-maven-plugin

Maven plugin for managing an OpenSearch Index Type Registry — a Git repository that is the single
source of truth for all OpenSearch index types in a microservice landscape.

## Key Features

- **Validation:** Enforces naming conventions, snake_case field names, and schema correctness on every build
- **Immutability:** Existing mapping files on the trunk branch are immutable (CRC32-checked); index types and system directories cannot be deleted
- **Backward compatibility:** Minor version changes may only add fields; breaking changes require a major version bump
- **Code generation:** Generates typed Java records and `IndexType<T>` singletons from mapping definitions
- **Artifact deployment:** Publishes one Maven JAR per `(index type, major version)` pair for consumption by service instances

## Documentation

- [Getting started](docs/getting-started.md)
- [Registry structure](docs/registry-structure.md)
- [Descriptor file](docs/descriptor-file.md)
- [Mapping file](docs/mapping-file.md)
- [Validation rules](docs/validation-rules.md)
- [Generated artifacts](docs/generated-artifacts.md)
- [Plugin goals](docs/plugin-goals.md)
- [Versioning](docs/versioning.md)

## Note

This repository is part of the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap) for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
