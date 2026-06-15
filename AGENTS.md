# AGENTS.md — jeap-opensearch-index-type-registry-maven-plugin

## Project purpose
Maven plugin that manages an OpenSearch Index Type Registry: validates registry structure and mapping schemas, enforces immutability of existing mappings (CRC32 against trunk branch), and generates per-(index type, major version) Maven JARs containing typed Java records, `IndexType<T>` singletons, and mapping files.

## Key packages
- `mojo` — `RegistryMojo` (goal: `registry`, phase: `verify`) and `DeployIndexTypeArtifactsMojo` (goal: `deploy-index-type-artifacts`, phase: `deploy`)
- `registry` — reads descriptor JSON files, resolves mapping files, builds the in-memory registry model
- `validation` — naming rules, schema validation (networknt json-schema-validator), immutability checks, minor-version backward-compatibility checks
- `codegen` — generates `*DataV<n>.java` records and `*IndexTypeV<n>.java` singletons from registry model
- `deploy` — packages per-type JARs, writes `META-INF/index-types.json` and ServiceLoader registrations, invokes Maven deployer via `maven-invoker`
- `git` — clones registry trunk branch via JGit for immutability/change-detection comparisons

## Critical conventions
- Artifact IDs follow `<kebab-case-name>-v<major>` (e.g. `jme-decree-document-v1`); artifact versions follow `<major>.<minor>` on trunk and `<major>.<minor>-<branch>-SNAPSHOT` on feature branches
- All `data.properties` field names must be snake_case (enforced by validation) — the writer service serialises with `SNAKE_CASE` naming strategy
- `dynamic: false` is mandatory in every mapping file
- `deploy-index-type-artifacts` is idempotent: HTTP 409 from repository = warning, not failure

## External dependency
Depends on `jeap-opensearch-index-type`. Version controlled via the `jeap-opensearch-index-type.version` property.

## Build & test
```bash
mvn verify
```
Integration tests use maven-invoker-plugin to run plugin goals against sample registry projects.
