# Plugin goals

The plugin provides two Maven goals: `registry` for validation and code generation, and
`deploy-index-type-artifacts` for packaging and deploying the generated artifacts.

## Goal: registry

**Default phase:** `verify`

Validates the registry structure and generates Java sources and mapping files for all index types.

### Parameters

| Parameter                  | Default                                                            | Description                                                                                                                                                |
|----------------------------|--------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `descriptorDirectory`      | `${basedir}/index-types`                                           | Root directory of the index type descriptors.                                                                                                              |
| `basePackage`              | *(required)*                                                       | Java package for generated classes (e.g. `ch.admin.bit.jme.opensearch.index`).                                                                             |
| `gitUrl`                   | —                                                                  | Git URL of the registry repository. Used to clone the trunk branch for immutability comparison. Set to empty to skip trunk comparison (local development). |
| `gitTokenEnvVariableName`  | `INDEX_TYPE_REPO_GIT_TOKEN`                                        | Name of the environment variable holding the Git authentication token. Falls back to unauthenticated access if not set.                                    |
| `trunkBranchName`          | `master`                                                           | Branch to compare against for immutability checks.                                                                                                         |
| `indexTypeVersion`         | —                                                                  | Version of `jeap-opensearch-index-type` declared as a dependency in each generated artifact's POM.                                                         |
| `outputDirectory`          | `${project.build.directory}/generated-sources/index-type-registry` | Directory for generated Java sources.                                                                                                                      |
| `outputResourcesDirectory` | `${project.build.outputDirectory}`                                 | Directory for `META-INF/` resources and `opensearch/` mapping files.                                                                                       |
| `skipGeneration`           | `false`                                                            | When `true`, validation runs but no Java sources are generated.                                                                                            |

### Local development

```bash
# Validate and generate without cloning the trunk branch
mvn verify -Dindextype.git.url=

# Validation only, skip code generation
mvn verify -Dindextype.git.url= -DskipGeneration=true
```

## Goal: deploy-index-type-artifacts

**Default phase:** `deploy`

Packages each `(index type, major version)` pair into an individual Maven JAR and deploys it.

The goal is **idempotent**: HTTP 409 Conflict responses (artifact already exists) are logged as
warnings rather than failing the build, so the goal is safe to re-run.

### Parameters

| Parameter                 | Default                     | Description                                                                                                                                                         |
|---------------------------|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `groupIdPrefix`           | *(required)*                | GroupId prefix for generated artifacts. The lowercase system name is appended automatically.                                                                        |
| `indexTypeVersion`        | *(required)*                | Version string for the generated artifacts (e.g. `1.2` on trunk, `1.2-feature-branch-SNAPSHOT` on feature branches).                                                |
| `basePackage`             | *(required)*                | Must match the `registry` goal configuration.                                                                                                                       |
| `mavenDeployGoal`         | `deploy`                    | `deploy` to push to a remote repository, `install` to install to the local Maven repository.                                                                        |
| `mavenGlobalSettingsFile` | —                           | Path to a Maven `settings.xml` for repository authentication.                                                                                                       |
| `deployAllIndexTypes`     | `false`                     | When `true`, deploys all index types unconditionally. On trunk the plugin always deploys all types; on feature branches only changed types are deployed by default. |
| `gitUrl`                  | —                           | Git URL for change detection. Set to empty to deploy all unconditionally.                                                                                           |
| `gitTokenEnvVariableName` | `INDEX_TYPE_REPO_GIT_TOKEN` | Environment variable holding the Git token.                                                                                                                         |
| `trunkBranchName`         | `master`                    | Branch used for change detection.                                                                                                                                   |
| `currentBranch`           | `${git.branch}`             | Current branch name, injected by `git-commit-id-maven-plugin`.                                                                                                      |
| `trunkMavenProfile`       | —                           | Maven profile activated only when building on the trunk branch.                                                                                                     |
| `pomTemplateFile`         | —                           | Custom POM template for generated artifacts. Falls back to the built-in default.                                                                                    |
| `skip`                    | `false`                     | Skip this goal entirely.                                                                                                                                            |

### Local deployment

```bash
# Install generated artifacts to the local Maven repository
mvn deploy -Dindextype.git.url= -DmavenDeployGoal=install
```

## Related

- [Getting started](getting-started.md)
- [Versioning](versioning.md)
- [Validation rules](validation-rules.md)
- [jeap-opensearch-index-type-registry-maven-plugin](../README.md)
