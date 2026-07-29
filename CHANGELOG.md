# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
