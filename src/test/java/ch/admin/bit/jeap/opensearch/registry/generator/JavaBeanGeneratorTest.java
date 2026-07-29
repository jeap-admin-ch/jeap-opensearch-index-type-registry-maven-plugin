package ch.admin.bit.jeap.opensearch.registry.generator;

import ch.admin.bit.jeap.opensearch.registry.TestRegistryBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JavaBeanGeneratorTest {

    private static final String BASE_PACKAGE = "ch.admin.bit.test.index";

    @Test
    void generateReturnsIndexTypeFqn(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "JmeDecreeDocument_mapping_v1_0.json",
                TestRegistryBuilder.VALID_MAPPING_V1_0);

        JavaBeanGenerator generator = new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog());
        String fqn = generate(generator, "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        assertThat(fqn).isEqualTo(BASE_PACKAGE + ".jme.decreedocument.JmeDecreeDocumentIndexTypeV1");
    }

    @Test
    void generatesDataRecord(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping.json", TestRegistryBuilder.VALID_MAPPING_V1_0);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String source = readSource(outputDir, "JmeDecreeDocumentDataV1.java");
        assertThat(source).contains("package " + BASE_PACKAGE + ".jme.decreedocument;");
        assertThat(source).contains("public record JmeDecreeDocumentDataV1(");
    }

    @Test
    void generatesIndexTypeSingleton(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping.json", TestRegistryBuilder.VALID_MAPPING_V1_0);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String source = readSource(outputDir, "JmeDecreeDocumentIndexTypeV1.java");
        assertThat(source).contains("public final class JmeDecreeDocumentIndexTypeV1 implements IndexType<JmeDecreeDocumentDataV1>");
        assertThat(source).contains("public static final JmeDecreeDocumentIndexTypeV1 INSTANCE");
        assertThat(source).contains("return \"JME\";");
        assertThat(source).contains("return \"JmeDecreeDocument\";");
        assertThat(source).contains("return 1;"); // majorVersion
        assertThat(source).contains("return 0;"); // minorVersion
    }

    @Test
    void indexTypeSingletonContainsDescriptorMetadata(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping.json", TestRegistryBuilder.VALID_MAPPING_V1_0);

        new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog())
                .generate("JmeDecreeDocument", "JME", "Some description", "https://example.com",
                        List.of("jme_read"), 1, 0, v10, List.of(v10));

        String source = readSource(outputDir, "JmeDecreeDocumentIndexTypeV1.java");
        assertThat(source).contains("Some description");
        assertThat(source).contains("https://example.com");
        assertThat(source).contains("\"jme_read\"");
    }

    @Test
    void mappingDefinitionReferencesOpensearchResource(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "JmeDecreeDocument_mapping_v1_0.json",
                TestRegistryBuilder.VALID_MAPPING_V1_0);

        new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog())
                .generate("JmeDecreeDocument", "JME", "", "", List.of(), 1, 0, v10, List.of(v10));

        String source = readSource(outputDir, "JmeDecreeDocumentIndexTypeV1.java");
        assertThat(source).contains("/opensearch/JmeDecreeDocument_mapping_v1_0.json");
    }

    @Test
    void dataRecordHasJsonPropertyForSnakeCaseFields(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping.json", TestRegistryBuilder.VALID_MAPPING_V1_0);
        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String source = readSource(outputDir, "JmeDecreeDocumentDataV1.java");
        assertThat(source).contains("@JsonProperty(\"document_id\")");
        assertThat(source).contains("documentId");
        assertThat(source).contains("@JsonProperty(\"created_at\")");
        assertThat(source).contains("createdAt");
    }

    @Test
    void dataRecordHasNestedRecordForObjectFields(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping.json", TestRegistryBuilder.VALID_MAPPING_V1_0);
        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String source = readSource(outputDir, "JmeDecreeDocumentDataV1.java");
        assertThat(source).contains("public record DecreeReference(");
        assertThat(source).contains("DecreeReference decreeReference");
    }

    @Test
    void dataRecordUsesInstantForDateFields(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping.json", TestRegistryBuilder.VALID_MAPPING_V1_0);
        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String source = readSource(outputDir, "JmeDecreeDocumentDataV1.java");
        assertThat(source).contains("import java.time.Instant;");
        assertThat(source).contains("Instant createdAt");
    }

    @Test
    void dataRecordDoesNotMapSearchItemOrOriginFields(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping.json", TestRegistryBuilder.VALID_MAPPING_V1_0);
        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String source = readSource(outputDir, "JmeDecreeDocumentDataV1.java");
        assertThat(source).doesNotContain("bpId");
        assertThat(source).doesNotContain("minorVersion");
    }

    @Test
    void majorVersionIsIncludedInClassName(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping.json", TestRegistryBuilder.VALID_MAPPING_V1_0);
        JavaBeanGenerator generator = new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog());
        String fqn1 = generate(generator, "MyType", "SYS", 1, 0, List.of(v10));
        String fqn2 = generate(generator, "MyType", "SYS", 2, 0, List.of(v10));

        assertThat(fqn1).endsWith("MyTypeIndexTypeV1");
        assertThat(fqn2).endsWith("MyTypeIndexTypeV2");
        assertThat(sourceFile(outputDir, "SYS", "MyType", "MyTypeDataV1.java")).exists();
        assertThat(sourceFile(outputDir, "SYS", "MyType", "MyTypeDataV2.java")).exists();
        assertThat(sourceFile(outputDir, "SYS", "MyType", "MyTypeIndexTypeV1.java")).exists();
        assertThat(sourceFile(outputDir, "SYS", "MyType", "MyTypeIndexTypeV2.java")).exists();
    }

    @Test
    void generatesCompleteDataRecordFile(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "JmeDecreeDocument_mapping_v1_0.json",
                TestRegistryBuilder.VALID_MAPPING_V1_0);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String expected = """
                package ch.admin.bit.test.index.jme.decreedocument;

                import com.fasterxml.jackson.annotation.JsonProperty;
                import java.time.Instant;

                public record JmeDecreeDocumentDataV1(
                    @JsonProperty("document_id") String documentId,
                    @JsonProperty("decree_reference") DecreeReference decreeReference,
                    @JsonProperty("created_at") Instant createdAt
                ) {

                    public record DecreeReference(
                        String type,
                        String id
                    ) {}
                }
                """;
        assertThat(readSource(outputDir, "JmeDecreeDocumentDataV1.java")).isEqualTo(expected);
    }

    @Test
    void generatesCompleteIndexTypeFile(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "JmeDecreeDocument_mapping_v1_0.json",
                TestRegistryBuilder.VALID_MAPPING_V1_0);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String expected = """
                package ch.admin.bit.test.index.jme.decreedocument;

                import ch.admin.bit.jeap.opensearch.indextype.IndexType;
                import java.io.InputStream;
                import java.util.List;
                import java.util.function.Supplier;

                public final class JmeDecreeDocumentIndexTypeV1 implements IndexType<JmeDecreeDocumentDataV1> {

                    public static final JmeDecreeDocumentIndexTypeV1 INSTANCE = new JmeDecreeDocumentIndexTypeV1();
                    public JmeDecreeDocumentIndexTypeV1() {}

                    @Override public String system()           { return "JME"; }
                    @Override public String originType()       { return "JmeDecreeDocument"; }
                    @Override public int    majorVersion()     { return 1; }
                    @Override public int    minorVersion()     { return 0; }
                    @Override public String description()      { return "description"; }
                    @Override public String documentationUrl() { return "https://example.com"; }
                    @Override public List<String> roles()      { return List.of("role_read"); }
                    @Override public Class<JmeDecreeDocumentDataV1> dataClass() { return JmeDecreeDocumentDataV1.class; }

                    @Override public Supplier<InputStream> mappingDefinition() {
                        return () -> getClass().getResourceAsStream("/opensearch/JmeDecreeDocument_mapping_v1_0.json");
                    }
                }
                """;
        assertThat(readSource(outputDir, "JmeDecreeDocumentIndexTypeV1.java")).isEqualTo(expected);
    }

    @Test
    void compatConstructorGeneratedWhenFieldAddedInLaterMinor(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping_v1_0.json", TestRegistryBuilder.VALID_MAPPING_V1_0);
        File v11 = writeMappingFile(mappingDir, "mapping_v1_1.json",
                TestRegistryBuilder.VALID_MAPPING_V1_1_ADDS_FIELD);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 1, List.of(v10, v11));

        String source = readSource(outputDir, "JmeDecreeDocumentDataV1.java");
        // The latest minor added a field — expect a compat constructor
        assertThat(source).containsPattern("public JmeDecreeDocumentDataV1\\(");
        // The compat constructor should not include the newly added field
        assertThat(source).contains("null)");
    }

    @Test
    void generatesRecordsForObjectsNestedMoreThanOneLevelDeep(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping.json", DEEPLY_NESTED_MAPPING);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String expected = """
                package ch.admin.bit.test.index.jme.decreedocument;

                import com.fasterxml.jackson.annotation.JsonProperty;
                import java.time.Instant;

                public record JmeDecreeDocumentDataV1(
                    Cases cases
                ) {

                    public record Cases(
                        String status,
                        @JsonProperty("control_pattern") ControlPattern controlPattern
                    ) {

                        public record ControlPattern(
                            @JsonProperty("factual_name") String factualName,
                            Instant decided
                        ) {}
                    }
                }
                """;
        assertThat(readSource(outputDir, "JmeDecreeDocumentDataV1.java")).isEqualTo(expected);
    }

    @Test
    void generatesRecordsAtThirdLevelOfNesting(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping.json", THREE_LEVEL_MAPPING);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String expected = """
                package ch.admin.bit.test.index.jme.decreedocument;

                import com.fasterxml.jackson.annotation.JsonProperty;

                public record JmeDecreeDocumentDataV1(
                    @JsonProperty("level_one") LevelOne levelOne
                ) {

                    public record LevelOne(
                        @JsonProperty("level_two") LevelTwo levelTwo
                    ) {

                        public record LevelTwo(
                            @JsonProperty("level_three") LevelThree levelThree
                        ) {

                            public record LevelThree(
                                String value
                            ) {}
                        }
                    }
                }
                """;
        assertThat(readSource(outputDir, "JmeDecreeDocumentDataV1.java")).isEqualTo(expected);
    }

    @Test
    void importsAreDerivedFromNestedFieldsAndNotOnlyFromTopLevelFields(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        // No top-level field needs @JsonProperty, Instant or JsonNode — all three are only required
        // by fields two levels down, so an import scan limited to the top level would miss them.
        File v10 = writeMappingFile(mappingDir, "mapping.json", IMPORTS_ONLY_NEEDED_DEEP_MAPPING);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String source = readSource(outputDir, "JmeDecreeDocumentDataV1.java");
        assertThat(source).contains("import com.fasterxml.jackson.annotation.JsonProperty;");
        assertThat(source).contains("import tools.jackson.databind.JsonNode;");
        assertThat(source).contains("import java.time.Instant;");
        assertThat(source).contains("@JsonProperty(\"created_at\") Instant createdAt");
        assertThat(source).contains("JsonNode raw");
        assertThat(compile(outputDir)).isEmpty();
    }

    @Test
    void generatedSourcesCompileForMappingWithSiblingNestedObjects(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        // Mirrors the shape of DaziT TariffaProduct v2: two sibling `nested` fields, each holding a
        // sub-object. Regression test for the deploy-time compilation failure of TariffaProductDataV2.
        File v20 = writeMappingFile(mappingDir, "mapping.json", TARIFFA_SHAPED_MAPPING);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "TariffaProduct", "Tariffa", 2, 0, List.of(v20));

        String source = Files.readString(
                sourceFile(outputDir, "Tariffa", "TariffaProduct", "TariffaProductDataV2.java").toPath());
        assertThat(source).contains("public record Ingredient(");
        assertThat(source).contains("public record ControlPattern(");
        assertThat(compile(outputDir)).isEmpty();
    }

    @Test
    void generatedSourcesCompileWhenDifferentParentsHaveEquallyNamedSubObjects(
            @TempDir File outputDir, @TempDir File mappingDir) throws IOException, MojoExecutionException {
        // Both `first` and `second` contain a `detail` object. Emitting both Detail records as
        // siblings of the data record would be a duplicate class declaration.
        File v10 = writeMappingFile(mappingDir, "mapping.json", EQUAL_SUB_OBJECT_NAMES_MAPPING);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        String source = readSource(outputDir, "JmeDecreeDocumentDataV1.java");
        assertThat(source).contains("String name");
        assertThat(source).contains("String code");
        assertThat(compile(outputDir)).isEmpty();
    }

    @Test
    void generatedSourcesCompileForMappingWithoutNestedObjects(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "JmeDecreeDocument_mapping_v1_0.json",
                TestRegistryBuilder.VALID_MAPPING_V1_0);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 0, List.of(v10));

        assertThat(compile(outputDir)).isEmpty();
    }

    @Test
    void compatConstructorDelegatesUsingNestedRecordTypes(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        File v10 = writeMappingFile(mappingDir, "mapping_v1_0.json", DEEPLY_NESTED_MAPPING);
        File v11 = writeMappingFile(mappingDir, "mapping_v1_1.json", DEEPLY_NESTED_MAPPING_V1_1_ADDS_FIELD);

        generate(new JavaBeanGenerator(outputDir, BASE_PACKAGE, new SystemStreamLog()),
                "JmeDecreeDocument", "JME", 1, 1, List.of(v10, v11));

        String source = readSource(outputDir, "JmeDecreeDocumentDataV1.java");
        // The compat constructor takes the v1.0 field set and passes null for the field added in v1.1
        assertThat(source).contains("""
                    public JmeDecreeDocumentDataV1(
                        Cases cases
                    ) {
                        this(cases, null);
                    }
                """);
        assertThat(compile(outputDir)).isEmpty();
    }

    private static final String THREE_LEVEL_MAPPING = """
            {
              "mappings": {
                "dynamic": false,
                "properties": {
                  "data": {
                    "type": "object",
                    "properties": {
                      "level_one": {
                        "type": "object",
                        "properties": {
                          "level_two": {
                            "type": "object",
                            "properties": {
                              "level_three": {
                                "type": "object",
                                "properties": {
                                  "value": { "type": "keyword" }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            """;

    private static final String IMPORTS_ONLY_NEEDED_DEEP_MAPPING = """
            {
              "mappings": {
                "dynamic": false,
                "properties": {
                  "data": {
                    "type": "object",
                    "properties": {
                      "wrapper": {
                        "type": "object",
                        "properties": {
                          "inner": {
                            "type": "object",
                            "properties": {
                              "created_at": { "type": "date" },
                              "raw": { "type": "object", "enabled": false }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            """;

    private static final String TARIFFA_SHAPED_MAPPING = """
            {
              "mappings": {
                "dynamic": false,
                "properties": {
                  "data": {
                    "type": "object",
                    "properties": {
                      "case_reference": { "type": "keyword" },
                      "compositions": {
                        "type": "nested",
                        "properties": {
                          "ingredient": {
                            "type": "object",
                            "properties": {
                              "name": { "type": "text" }
                            }
                          },
                          "proportion": { "type": "double" },
                          "unit": { "type": "keyword" }
                        }
                      },
                      "cases": {
                        "type": "nested",
                        "properties": {
                          "case_reference": { "type": "keyword" },
                          "control_pattern": {
                            "type": "object",
                            "properties": {
                              "factual_name": { "type": "text" },
                              "decided": { "type": "date" }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            """;

    private static final String EQUAL_SUB_OBJECT_NAMES_MAPPING = """
            {
              "mappings": {
                "dynamic": false,
                "properties": {
                  "data": {
                    "type": "object",
                    "properties": {
                      "first": {
                        "type": "object",
                        "properties": {
                          "detail": {
                            "type": "object",
                            "properties": {
                              "name": { "type": "text" }
                            }
                          }
                        }
                      },
                      "second": {
                        "type": "nested",
                        "properties": {
                          "detail": {
                            "type": "object",
                            "properties": {
                              "code": { "type": "keyword" }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            """;

    private static final String DEEPLY_NESTED_MAPPING_V1_1_ADDS_FIELD = """
            {
              "mappings": {
                "dynamic": false,
                "properties": {
                  "data": {
                    "type": "object",
                    "properties": {
                      "cases": {
                        "type": "nested",
                        "properties": {
                          "status": { "type": "keyword" },
                          "control_pattern": {
                            "type": "object",
                            "properties": {
                              "factual_name": { "type": "text" },
                              "decided": { "type": "date" }
                            }
                          }
                        }
                      },
                      "remark": { "type": "text" }
                    }
                  }
                }
              }
            }
            """;

    private static final String DEEPLY_NESTED_MAPPING = """
            {
              "mappings": {
                "dynamic": false,
                "properties": {
                  "data": {
                    "type": "object",
                    "properties": {
                      "cases": {
                        "type": "nested",
                        "properties": {
                          "status": { "type": "keyword" },
                          "control_pattern": {
                            "type": "object",
                            "properties": {
                              "factual_name": { "type": "text" },
                              "decided": { "type": "date" }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            """;

    // ── helpers ──────────────────────────────────────────────────────────────

    private String generate(JavaBeanGenerator generator, String typeName, String system,
                             int major, int latestMinor, List<File> allMinors)
            throws MojoExecutionException {
        return generator.generate(typeName, system, "description", "https://example.com",
                List.of("role_read"), major, latestMinor, allMinors.getLast(), allMinors);
    }

    private File writeMappingFile(File dir, String name, String content) throws IOException {
        File f = new File(dir, name);
        Files.writeString(f.toPath(), content);
        return f;
    }

    private File sourceFile(File outputDir, String system, String indexTypeName, String filename) {
        String pkg = JavaBeanGenerator.packageFor(BASE_PACKAGE, system, indexTypeName);
        return new File(new File(outputDir, pkg.replace('.', '/')), filename);
    }

    private File sourceFile(File outputDir, String filename) {
        return sourceFile(outputDir, "JME", "JmeDecreeDocument", filename);
    }

    private String readSource(File outputDir, String filename) throws IOException {
        return Files.readString(sourceFile(outputDir, filename).toPath());
    }

    private List<String> compile(File outputDir) {
        return GeneratedSourceCompiler.compile(outputDir);
    }
}
