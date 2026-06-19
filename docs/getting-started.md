# Getting started

This page shows how to set up an OpenSearch Index Type Registry project. The registry is a Git
repository that is the single source of truth for all OpenSearch index types in a microservice
landscape. The Maven plugin validates the registry, enforces compatibility rules, and generates
typed Java artifacts for each index type.

For the registry structure see [Registry structure](registry-structure.md). For all plugin
parameters see [Plugin goals](plugin-goals.md).

## 1. Create the registry project

A registry project has `packaging: pom`. It does not produce a JAR itself — only per-type
artifacts are deployed.

```xml
<project>
    <groupId>ch.admin.bit.jme</groupId>
    <artifactId>jme-index-type-registry</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <properties>
        <maven.deploy.skip>true</maven.deploy.skip>
        <indextype.git.url>https://github.com/your-org/jme-index-type-registry.git</indextype.git.url>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>ch.admin.bit.jeap</groupId>
                <artifactId>jeap-opensearch-index-type-registry-maven-plugin</artifactId>
                <version>${jeap-opensearch-index-type-registry.version}</version>
                <configuration>
                    <descriptorDirectory>${basedir}/index-types</descriptorDirectory>
                    <basePackage>ch.admin.bit.jme.opensearch.index</basePackage>
                    <gitUrl>${indextype.git.url}</gitUrl>
                    <gitTokenEnvVariableName>GITHUB_TOKEN</gitTokenEnvVariableName>
                    <trunkBranchName>master</trunkBranchName>
                    <indexTypeVersion>${project.version}</indexTypeVersion>
                </configuration>
                <executions>
                    <execution>
                        <id>validate-registry</id>
                        <goals><goal>registry</goal></goals>
                    </execution>
                    <execution>
                        <id>deploy-index-type-artifacts</id>
                        <goals><goal>deploy-index-type-artifacts</goal></goals>
                        <configuration>
                            <groupIdPrefix>ch.admin.bit.jme.indextype</groupIdPrefix>
                            <mavenDeployGoal>deploy</mavenDeployGoal>
                            <mavenGlobalSettingsFile>${basedir}/.github/actions/settings.xml</mavenGlobalSettingsFile>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

## 2. Create the directory structure

```
jme-index-type-registry/
└── index-types/
    └── jme/
        └── jmedecreedocument/
            ├── JmeDecreeDocument.json
            └── JmeDecreeDocument_mapping_v1_0.json
```

See [Registry structure](registry-structure.md) for naming rules.

## 3. Write the descriptor

```json
{
  "system": "JME",
  "originType": "JmeDecreeDocument",
  "description": "Indexes decree documents from the JME domain service.",
  "roles": ["jme_read"],
  "mappingVersions": [
    { "major": 1, "minor": 0, "mappingDefinition": "JmeDecreeDocument_mapping_v1_0.json" }
  ]
}
```

See [Descriptor file](descriptor-file.md) for all fields.

## 4. Write the mapping

```json
{
  "mappings": {
    "dynamic": false,
    "properties": {
      "search_item": { ... },
      "origin": { ... },
      "data": {
        "type": "object",
        "properties": {
          "document_id":    { "type": "keyword" },
          "document_title": { "type": "text" }
        }
      }
    }
  }
}
```

See [Mapping file](mapping-file.md) for required sections and the full type mapping table.

## 5. Validate locally

```bash
# Validate without cloning the trunk branch
mvn verify -Dindextype.git.url=

# Install generated artifacts to the local Maven repository
mvn deploy -Dindextype.git.url= -DmavenDeployGoal=install
```

## 6. Consume the generated artifact

After deployment, service instances add the generated artifact as a dependency:

```xml
<dependency>
    <groupId>ch.admin.bit.jme.indextype.jme</groupId>
    <artifactId>jme-decree-document-v1</artifactId>
    <version>1.0</version>
</dependency>
```

The artifact provides a ready-to-use `JmeDecreeDocumentIndexTypeV1.INSTANCE` singleton and a
`JmeDecreeDocumentDataV1` data record.

## Related

- [Registry structure](registry-structure.md)
- [Descriptor file](descriptor-file.md)
- [Mapping file](mapping-file.md)
- [Validation rules](validation-rules.md)
- [Generated artifacts](generated-artifacts.md)
- [Plugin goals](plugin-goals.md)
- [Versioning](versioning.md)
- [jeap-opensearch-index-type-registry-maven-plugin](../README.md)
