package ch.admin.bit.jeap.opensearch.registry;

import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@MojoTest
class IndexTypeRegistryMojoTest {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    @Test
    void generatedIndexTypeJarPreservesNativeAnalysis(@TempDir File tempDir) throws Exception {
        File descriptorDir = TestRegistryBuilder.mkdirs(tempDir, "index-types");
        new TestRegistryBuilder(descriptorDir).buildValidIndexType();
        File mappingFile = new File(descriptorDir, "jme/jmedecreedocument/JmeDecreeDocument_mapping_v1_0.json");
        String definition = """
                {"settings":{"analysis":{
                  "analyzer":{"folding_analyzer":{"type":"custom","tokenizer":"standard","filter":["lowercase","asciifolding"]}},
                  "normalizer":{"folding_normalizer":{"type":"custom","filter":["lowercase","asciifolding"]}}
                }},
                """ + TestRegistryBuilder.VALID_MAPPING_V1_0.substring(1);
        Files.writeString(mappingFile.toPath(), definition);
        File sources = new File(tempDir, "generated-sources");
        File classes = new File(tempDir, "classes");
        IndexTypeRegistryMojo mojo = new IndexTypeRegistryMojo();
        setField(mojo, "descriptorDirectory", descriptorDir);
        setField(mojo, "outputDirectory", sources);
        setField(mojo, "outputResourcesDirectory", classes);
        setField(mojo, "basePackage", "ch.admin.bit.test.index");
        setField(mojo, "project", new MavenProject());
        setField(mojo, "skipGeneration", false);
        mojo.execute();

        java.util.List<String> arguments = new java.util.ArrayList<>(java.util.List.of(
                "-classpath", System.getProperty("java.class.path"), "-d", classes.getAbsolutePath()));
        try (var files = Files.walk(sources.toPath())) {
            files.filter(p -> p.toString().endsWith(".java")).map(Object::toString).forEach(arguments::add);
        }
        assertThat(javax.tools.ToolProvider.getSystemJavaCompiler().run(null, null, null, arguments.toArray(String[]::new))).isZero();
        File jar = new File(tempDir, "analysis-index-type.jar");
        try (var out = new java.util.jar.JarOutputStream(Files.newOutputStream(jar.toPath()));
             var files = Files.walk(classes.toPath())) {
            for (var file : files.filter(Files::isRegularFile).toList()) {
                out.putNextEntry(new java.util.jar.JarEntry(classes.toPath().relativize(file).toString()));
                Files.copy(file, out);
                out.closeEntry();
            }
        }
        try (var loader = new java.net.URLClassLoader(new java.net.URL[]{jar.toURI().toURL()}, getClass().getClassLoader())) {
            var indexType = java.util.ServiceLoader.load(ch.admin.bit.jeap.opensearch.indextype.IndexType.class, loader).findFirst().orElseThrow();
            try (var stream = indexType.mappingDefinition().get()) {
                assertThat(JSON_MAPPER.readTree(stream)).isEqualTo(JSON_MAPPER.readTree(definition));
            }
        }
    }

    @Test
    @Basedir("src/test/resources/valid")
    @InjectMojo(goal = "registry")
    void validRegistry(IndexTypeRegistryMojo mojo) {
        assertDoesNotThrow(mojo::execute);
    }

    @Test
    @Basedir("src/test/resources/dirMissing")
    @InjectMojo(goal = "registry")
    void missingDescriptorDirectory(IndexTypeRegistryMojo mojo) {
        assertThatThrownBy(mojo::execute).hasMessageContaining("does not exist");
    }

    @Test
    @Basedir("src/test/resources/invalidDescriptor")
    @InjectMojo(goal = "registry")
    void invalidDescriptor(IndexTypeRegistryMojo mojo) {
        assertThatThrownBy(mojo::execute).hasMessageContaining("does not conform to schema");
    }

    @Test
    @Basedir("src/test/resources/incompatibleMinorVersion")
    @InjectMojo(goal = "registry")
    void incompatibleMinorVersion(IndexTypeRegistryMojo mojo) {
        assertThatThrownBy(mojo::execute).hasMessageContaining("not backward compatible");
    }

    @Test
    void executeGeneratesSourcesAndMetaInf(@TempDir File tempDir) throws Exception {
        File descriptorDir = TestRegistryBuilder.mkdirs(tempDir, "index-types");
        File outputDir = new File(tempDir, "generated-sources");
        File outputResourcesDir = new File(tempDir, "classes");
        new TestRegistryBuilder(descriptorDir).buildValidIndexType();

        MavenProject project = new MavenProject();
        IndexTypeRegistryMojo mojo = new IndexTypeRegistryMojo();
        setField(mojo, "descriptorDirectory", descriptorDir);
        setField(mojo, "outputDirectory", outputDir);
        setField(mojo, "outputResourcesDirectory", outputResourcesDir);
        setField(mojo, "basePackage", "ch.admin.bit.test.index");
        setField(mojo, "project", project);
        setField(mojo, "skipGeneration", false);

        assertDoesNotThrow(mojo::execute);

        File packageDir = new File(outputDir, "ch/admin/bit/test/index/jme/decreedocument");
        assertThat(new File(packageDir, "JmeDecreeDocumentDataV1.java")).exists();
        assertThat(new File(packageDir, "JmeDecreeDocumentIndexTypeV1.java")).exists();
        assertThat(new File(outputResourcesDir, "opensearch/JmeDecreeDocument_mapping_v1_0.json")).exists();

        File indexTypesFile = new File(outputResourcesDir, "META-INF/index-types.json");
        assertThat(indexTypesFile).exists();
        JsonNode root = JSON_MAPPER.readTree(indexTypesFile);
        JsonNode indexTypes = root.path("indexTypes");
        assertThat(indexTypes.isArray()).isTrue();
        assertThat(indexTypes).hasSize(1);
        assertThat(indexTypes.get(0).path("indexTypeName").asString()).isEqualTo("JmeDecreeDocument");

        assertThat(project.getCompileSourceRoots()).contains(outputDir.getAbsolutePath());
    }

    @Test
    void executeSkipsGenerationWhenConfigured(@TempDir File tempDir) throws Exception {
        File descriptorDir = TestRegistryBuilder.mkdirs(tempDir, "index-types");
        File outputDir = new File(tempDir, "generated-sources");
        File outputResourcesDir = new File(tempDir, "classes");
        new TestRegistryBuilder(descriptorDir).buildValidIndexType();

        MavenProject project = new MavenProject();
        IndexTypeRegistryMojo mojo = new IndexTypeRegistryMojo();
        setField(mojo, "descriptorDirectory", descriptorDir);
        setField(mojo, "outputDirectory", outputDir);
        setField(mojo, "outputResourcesDirectory", outputResourcesDir);
        setField(mojo, "basePackage", "ch.admin.bit.test.index");
        setField(mojo, "project", project);
        setField(mojo, "skipGeneration", true);

        assertDoesNotThrow(mojo::execute);

        assertThat(Files.exists(outputDir.toPath())).isFalse();
        assertThat(Files.exists(new File(outputResourcesDir, "META-INF/index-types.json").toPath())).isFalse();
        assertThat(project.getCompileSourceRoots()).isEmpty();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
