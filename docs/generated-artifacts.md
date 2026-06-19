# Generated artifacts

The `registry` goal generates one Maven artifact per `(index type, major version)` pair. Each
artifact is a self-contained JAR that can be consumed as a normal Maven dependency.

## Artifact coordinates

The groupId is formed by appending the lowercase system name to `groupIdPrefix`. The artifactId
includes the major version suffix `-v<major>`. For system `JME`, type `JmeDecreeDocument`, major
version `1`, minor version `2`, and `groupIdPrefix=ch.admin.bit.jme.indextype`:

```
ch.admin.bit.jme.indextype.jme:jme-decree-document-v1:1.2
```

The `-v<major>` suffix in the artifactId allows a service to depend on multiple major versions of
the same index type simultaneously.

On feature branches the version becomes `<major>.<minor>-<sanitized-branch>-SNAPSHOT`.

## JAR contents

| Path                                                                 | Description                                                                     |
|----------------------------------------------------------------------|---------------------------------------------------------------------------------|
| `ch/admin/.../jme/decreedocument/JmeDecreeDocumentDataV1.class`      | Immutable data record for the `data` section.                                   |
| `ch/admin/.../jme/decreedocument/JmeDecreeDocumentIndexTypeV1.class` | `IndexType<T>` singleton with all metadata and a reference to the mapping.      |
| `opensearch/JmeDecreeDocument_mapping_v1_0.json`                     | Mapping JSON for each minor version of this major version.                      |
| `META-INF/index-types.json`                                          | Runtime metadata: system, roles, mapping versions, fully qualified class names. |
| `META-INF/services/ch.admin.bit.jeap.opensearch.indextype.IndexType` | ServiceLoader registration for auto-discovery.                                  |

## Generated Java code

The plugin generates two Java files per `(index type, major version)` pair in the package
`<basePackage>.<system-lowercase>.<typename-without-system-prefix-lowercase>`.

For `JmeDecreeDocument` v1 with `basePackage=ch.admin.bit.jme.opensearch.index` the package is
`ch.admin.bit.jme.opensearch.index.jme.decreedocument`.

### Data record

```java
public record JmeDecreeDocumentDataV1(
    @JsonProperty("document_id")    String documentId,
    @JsonProperty("document_title") String documentTitle,
    @JsonProperty("created_at")     Instant createdAt
) {}
```

Nested `object` fields with sub-`properties` become inner records:

```java
public record JmeDecreeDocumentDataV1(
    @JsonProperty("decree_reference") DecreeReference decreeReference
) {
    public record DecreeReference(String type, String id) {}
}
```

### IndexType singleton

```java
public final class JmeDecreeDocumentIndexTypeV1 implements IndexType<JmeDecreeDocumentDataV1> {

    public static final JmeDecreeDocumentIndexTypeV1 INSTANCE = new JmeDecreeDocumentIndexTypeV1();

    @Override public String system()           { return "JME"; }
    @Override public String originType()       { return "JmeDecreeDocument"; }
    @Override public int    majorVersion()     { return 1; }
    @Override public int    minorVersion()     { return 2; }
    @Override public List<String> roles()      { return List.of("jme_read"); }
    @Override public String indexWriteAlias()  { return "jme_decree_document_v1_write"; }
    @Override public String indexReadAlias()   { return "jme_decree_document_read"; }
    @Override public Class<JmeDecreeDocumentDataV1> dataClass() {
        return JmeDecreeDocumentDataV1.class;
    }
    @Override public Supplier<InputStream> mappingDefinition() {
        return () -> getClass().getResourceAsStream(
            "/opensearch/JmeDecreeDocument_mapping_v1_2.json");
    }
}
```

## Consuming the artifact

```xml
<dependency>
    <groupId>ch.admin.bit.jme.indextype.jme</groupId>
    <artifactId>jme-decree-document-v1</artifactId>
    <version>1.2</version>
</dependency>
```

Use the singleton directly:

```java
// In the index writer service
IndexType<JmeDecreeDocumentDataV1> indexType = JmeDecreeDocumentIndexTypeV1.INSTANCE;

// In the search client
searchItemClient.searchMultiVersionWithUserAuth(
    List.of(JmeDecreeDocumentIndexTypeV1.INSTANCE),
    query
);
```

## Related

- [Getting started](getting-started.md)
- [Mapping file](mapping-file.md)
- [Plugin goals](plugin-goals.md)
- [Versioning](versioning.md)
- [jeap-opensearch-index-type-registry-maven-plugin](../README.md)
