# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.2.0] - 2026-08-18

### Changed

- Update parent from 8.11.0 to 8.12.0

## [3.1.0] - 2026-08-17

### Changed
- Update parent from 8.10.0 to 8.11.0
- update jeap-opensearch-index-type from 1.19.0 to 1.20.0

## [3.0.1] - 2026-08-14

### Fixed
- `deploy-index-type-artifacts`: skip internal deployment of release artifacts that already exist instead of partially uploading files before an immutable repository rejects the existing POM.

## [3.0.0] - 2026-08-13

### Added
- `registry`: support `mappings._meta.jeap.collection_fields` for generating scalar and object fields as `List<T>`, including fields at arbitrary nesting depth.
- `registry`: validate collection field paths and reject generated API changes between minor mapping versions.

### Changed
- `registry`: require `nested` fields to be declared in `collection_fields` to generate `List<T>`.
- `registry`: warn when structured `object` fields are declared as collections and report reordered field positions.

## [2.7.0] - 2026-08-13

### Changed
- Update parent from 8.9.1 to 8.10.0
- update jeap-opensearch-index-type from 1.18.0 to 1.19.0

## [2.6.0] - 2026-08-12

### Changed
- Update parent from 8.8.0 to 8.9.1
- update jeap-opensearch-index-type from 1.17.0 to 1.18.0

## [2.5.0] - 2026-08-11

### Changed
- Update parent from 8.7.1 to 8.8.0
- update jeap-opensearch-index-type from 1.16.0 to 1.17.0

## [2.4.0] - 2026-08-10

### Changed
- Update parent from 8.7.0 to 8.7.1
- update maven-plugin-testing-harness from 3.5.0 to 3.5.1
- update org.eclipse.jgit from 7.3.0.202506031305-r to 7.7.1.202607240634-r
- update json-schema-validator from 3.0.3 to 3.0.6
- update jeap-opensearch-index-type from 1.15.0 to 1.16.0

## [2.3.0] - 2026-08-08

### Changed
- Update parent from 8.6.1 to 8.7.0
- update jeap-opensearch-index-type from 1.14.0 to 1.15.0

## [2.2.0] - 2026-08-04

### Changed
- Update parent from 8.6.0 to 8.6.1
- update jeap-opensearch-index-type from 1.13.0 to 1.14.0

## [2.1.0] - 2026-08-01

### Changed
- Update parent from 8.5.6 to 8.6.0
- update jeap-opensearch-index-type from 1.12.0 to 1.13.0

## [2.0.0] - 2026-07-29

### Changed
- **BREAKING** `registry`: a mapping field declared `"type": "nested"` is now generated as `List<X>` instead of `X`. In OpenSearch, `nested` means *array of objects*; the generated single-valued component could not represent more than one element and made the index writer service fail with `MismatchedInputException` as soon as a producer sent the array the mapping asks for. Fields declared `"type": "object"` are unchanged. Registries using `nested` must expect the component type of those fields to change.

### Fixed
- `registry`: objects nested more than one level deep below `data` now get a generated record. Previously only top-level object/nested fields were declared, so a mapping such as `data.cases.control_pattern` generated a field referencing a record that was never emitted, and the generated data class failed to compile.
- `registry`: the `Instant`, `JsonNode` and `@JsonProperty` imports of the generated data class are now derived from fields at any nesting depth instead of only from top-level fields.

## [1.14.0] - 2026-07-28

### Changed
- Update parent from 8.5.5 to 8.5.6
- update jeap-opensearch-index-type from 1.11.0 to 1.12.0

## [1.13.0] - 2026-07-25

### Changed
- Update parent from 8.5.4 to 8.5.5
- update jeap-opensearch-index-type from 1.10.0 to 1.11.0

## [1.12.0] - 2026-07-24

### Fixed
- `deploy-index-type-artifacts`: proxy system properties (e.g. `https.proxyHost`, `https.proxyPort`) from the outer Maven process are now forwarded to the nested per-index-type `mvn deploy` invocation, matching the convention used by `jeap-messaging-avro-maven-plugin` and `jeap-process-archive-avro-maven-plugin`. Previously, the nested invocation lost proxy configuration, which could cause connection timeouts when publishing to Maven Central through a proxy.

### Changed
- `deploy-index-type-artifacts`: nested Maven invocations now also set `style.color=always` and `jansi.force=true` for colored console output, matching the convention used by `jeap-messaging-avro-maven-plugin` and `jeap-process-archive-avro-maven-plugin`.

## [1.11.0] - 2026-07-23

### Changed
- `deploy-index-type-artifacts`: `mavenGlobalSettingsFile` now falls back to `settings.xml` in the project's basedir when not configured (or when the configured file does not exist), matching the convention used by `jeap-messaging-avro-maven-plugin`. Previously, if unset, no global settings file was passed to the nested Maven invocation at all.

## [1.10.0] - 2026-07-23

### Changed
- Update parent from 8.5.3 to 8.5.4
- update jeap-opensearch-index-type from 1.9.0 to 1.10.0

## [1.9.0] - 2026-07-23

### Changed
- Update parent from 8.5.2 to 8.5.3
- update jeap-opensearch-index-type from 1.8.0 to 1.9.0

## [1.8.0] - 2026-07-22

### Changed
- Update parent from 8.5.0 to 8.5.2
- update jeap-opensearch-index-type from 1.7.0 to 1.8.0

## [1.7.0] - 2026-07-15

### Changed
- Update parent from 8.4.0 to 8.5.0
- update jeap-opensearch-index-type from 1.6.0 to 1.7.0

## [1.6.0] - 2026-07-13

### Changed
- Update parent from 8.3.4 to 8.4.0
- update jeap-opensearch-index-type from 1.5.0 to 1.6.0

## [1.5.0] - 2026-06-30

### Changed
- Update parent from 8.3.3 to 8.3.4
- update jeap-opensearch-index-type from 1.4.0 to 1.5.0

## [1.4.0] - 2026-06-23

### Changed
- Update parent from 8.3.2 to 8.3.3
- update jeap-opensearch-index-type from 1.3.0 to 1.4.0

## [1.3.0] - 2026-06-22

### Changed
- Update parent from 8.3.1 to 8.3.2
- update jeap-opensearch-index-type from 1.2.0 to 1.3.0

## [1.2.0] - 2026-06-18

### Changed
- Update parent from 8.3.0 to 8.3.1
- update jeap-opensearch-index-type from 1.1.0 to 1.2.0

## [1.1.0] - 2026-06-17

### Changed
- Update parent from 8.2.0 to 8.3.0
- update jeap-opensearch-index-type from 1.0.0 to 1.1.0

## [1.0.0] - 2026-06-15

### Changed
- initial release
