package ch.admin.bit.jeap.opensearch.registry.deploy;

import com.sun.net.httpserver.HttpServer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.*;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IndexTypeArtifactDeployerTest {

    @Test
    void pomContainsKebabCaseArtifactIdAndSystemGroupId(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit.jme.indextype", "1.0.0", outputDir);
        File mapping = writeMappingFile(mappingDir, "JmeDecreeDocument_mapping_v1_0.json");

        deployer.deploy(info("JmeDecreeDocument", "JME", 1, "1.0", List.of(mapping)));

        assertThat(deployer.capturedPomContent)
                .contains("<artifactId>jme-decree-document-v1</artifactId>")
                .contains("<groupId>ch.admin.bit.jme.indextype.jme</groupId>")
                .contains("<version>1.0</version>");
    }

    @Test
    void pomContainsIndexTypeVersionDependency(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "2.3.4", outputDir);
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping)));

        assertThat(deployer.capturedPomContent)
                .contains("<artifactId>jeap-opensearch-index-type</artifactId>")
                .contains("<version>2.3.4</version>");
    }

    @Test
    void multiWordTypeNameConvertsToKebabCase(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        CapturingDeployer deployer = new CapturingDeployer("ch.admin", "1.0", outputDir);
        File mapping = writeMappingFile(mappingDir, "JmeOrderValidatedEvent_mapping_v1_0.json");

        deployer.deploy(info("JmeOrderValidatedEvent", "JME", 1, "1.0", List.of(mapping)));

        assertThat(deployer.capturedPomContent)
                .contains("<artifactId>jme-order-validated-event-v1</artifactId>");
    }

    @Test
    void alreadyExistsErrorIsWarnedButNotThrown(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException {
        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir);
        deployer.exitCode = 1;
        deployer.outputToFeed = "status code: 409";
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        assertThatNoException().isThrownBy(() ->
                deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping))));
    }

    @Test
    void existingReleaseArtifactSkipsMavenDeploy(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir);
        deployer.releaseArtifactExists = true;
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping)));

        assertThat(deployer.capturedRequest.getProperties())
                .containsEntry("maven.deploy.skip", "true");
        assertThat(deployer.releaseArtifactChecks).isEqualTo(1);
    }

    @Test
    void missingReleaseArtifactIsDeployed(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir);
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping)));

        assertThat(deployer.capturedRequest.getProperties())
                .doesNotContainKey("maven.deploy.skip");
        assertThat(deployer.releaseArtifactChecks).isEqualTo(1);
    }

    @Test
    void snapshotArtifactDoesNotCheckReleaseRepository(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir);
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        deployer.deploy(info("MyType", "SYS", 1, "1.0-SNAPSHOT", List.of(mapping)));

        assertThat(deployer.releaseArtifactChecks).isZero();
        assertThat(deployer.capturedRequest.getProperties())
                .doesNotContainKey("maven.deploy.skip");
    }

    @Test
    void installDoesNotCheckReleaseRepository(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        CapturingDeployer deployer = new CapturingDeployer("install", "ch.admin.bit", "1.0", outputDir);
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping)));

        assertThat(deployer.releaseArtifactChecks).isZero();
        assertThat(deployer.capturedRequest.getProperties())
                .doesNotContainKey("maven.deploy.skip");
    }

    @Test
    void artifactPomUriUsesMavenRepositoryLayout() {
        URI uri = IndexTypeArtifactDeployer.artifactPomUri(
                "https://repo.example/releases", "ch.admin.bit.test", "my-artifact", "1.2");

        assertThat(uri).hasToString(
                "https://repo.example/releases/ch/admin/bit/test/my-artifact/1.2/my-artifact-1.2.pom");
    }

    @Test
    void releaseRepositoryStatusDeterminesWhetherArtifactExists(@TempDir File outputDir)
            throws IOException, MojoExecutionException {
        AtomicInteger status = new AtomicInteger(200);
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(status.get(), -1);
            exchange.close();
        });
        server.start();
        try {
            String repositoryUrl = "http://localhost:" + server.getAddress().getPort() + "/releases";
            RepositoryCheckingDeployer deployer = new RepositoryCheckingDeployer(repositoryUrl, outputDir);

            assertThat(deployer.artifactExists()).isTrue();

            status.set(404);
            assertThat(deployer.artifactExists()).isFalse();

            status.set(302);
            assertThat(deployer.artifactExists()).isFalse();

            status.set(500);
            assertThat(deployer.artifactExists()).isFalse();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void mavenBuildFailureThrows(@TempDir File outputDir, @TempDir File mappingDir) throws IOException {
        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir);
        deployer.exitCode = 1;
        deployer.outputToFeed = "BUILD FAILURE";
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        assertThatThrownBy(() -> deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping))))
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("Maven command failed");
    }

    @Test
    void mappingFilesCopiedToOpensearchResourceDir(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir);
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping)));

        // capturedMappingExisted is set during the invoke — before the temp dir is cleaned up
        assertThat(deployer.capturedMappingExisted).isTrue();
    }

    @Test
    void servicesFileRegistersIndexTypeFqn(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir);
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping)));

        assertThat(deployer.capturedServicesContent)
                .contains("ch.admin.bit.test.MyTypeIndexTypeV1");
    }

    @Test
    void customPomTemplateIsUsedWhenProvided(@TempDir File outputDir, @TempDir File mappingDir,
                                             @TempDir File templateDir)
            throws IOException, MojoExecutionException {
        File templateFile = new File(templateDir, "custom.pom.xml");
        Files.writeString(templateFile.toPath(), "<project><groupId>${groupId}</groupId>"
                + "<artifactId>${artifactId}</artifactId><version>${version}</version>"
                + "<extra>custom</extra></project>");

        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir, templateFile);
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping)));

        assertThat(deployer.capturedPomContent)
                .contains("<extra>custom</extra>")
                .contains("<artifactId>my-type-v1</artifactId>");
    }

    @Test
    void missingCustomPomTemplateThrows(@TempDir File outputDir, @TempDir File mappingDir,
                                        @TempDir File templateDir)
            throws IOException {
        File missingTemplate = new File(templateDir, "nonexistent.xml");
        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir, missingTemplate);
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        assertThatThrownBy(() -> deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping))))
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("Custom POM template not found");
    }

    @Test
    void fallsBackToBasedirSettingsFileWhenNotConfigured(@TempDir File outputDir, @TempDir File mappingDir,
                                                          @TempDir File basedir)
            throws IOException, MojoExecutionException {
        File settingsFile = new File(basedir, "settings.xml");
        Files.writeString(settingsFile.toPath(), "<settings/>");
        MavenProject project = new MavenProject();
        project.setFile(new File(basedir, "pom.xml"));

        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir, null,
                project);
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping)));

        assertThat(deployer.capturedRequest.getGlobalSettingsFile()).isEqualTo(settingsFile);
    }

    @Test
    void noGlobalSettingsFileSetWhenNoneConfiguredAndNoneFoundInBasedir(@TempDir File outputDir,
                                                                         @TempDir File mappingDir,
                                                                         @TempDir File basedir)
            throws IOException, MojoExecutionException {
        MavenProject project = new MavenProject();
        project.setFile(new File(basedir, "pom.xml"));

        CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir, null,
                project);
        File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

        deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping)));

        assertThat(deployer.capturedRequest.getGlobalSettingsFile()).isNull();
    }

    @Test
    void proxyPropertiesForwardedToNestedMavenInvocation(@TempDir File outputDir, @TempDir File mappingDir)
            throws IOException, MojoExecutionException {
        System.setProperty("https.proxyHost", "proxy.example.com");
        System.setProperty("https.proxyPort", "8080");
        try {
            CapturingDeployer deployer = new CapturingDeployer("ch.admin.bit", "1.0", outputDir);
            File mapping = writeMappingFile(mappingDir, "MyType_mapping_v1_0.json");

            deployer.deploy(info("MyType", "SYS", 1, "1.0", List.of(mapping)));

            assertThat(deployer.capturedRequest.getProperties())
                    .containsEntry("https.proxyHost", "proxy.example.com")
                    .containsEntry("https.proxyPort", "8080");
        } finally {
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static IndexTypeArtifactDeployer.IndexTypeInfo info(String typeName, String system,
                                                                  int major, String version,
                                                                  List<File> mappingFiles) {
        String fqn = "ch.admin.bit.test." + typeName + "IndexTypeV" + major;
        return new IndexTypeArtifactDeployer.IndexTypeInfo(typeName, system, major, version,
                fqn, List.of(), mappingFiles, null);
    }

    private static File writeMappingFile(File dir, String name) throws IOException {
        File f = new File(dir, name);
        Files.writeString(f.toPath(), "{\"mappings\":{\"dynamic\":false,\"properties\":{}}}");
        return f;
    }

    // ── test subclass that captures what Maven sees ───────────────────────────

    static class CapturingDeployer extends IndexTypeArtifactDeployer {
        InvocationRequest capturedRequest;
        String capturedPomContent;
        boolean capturedMappingExisted;
        String capturedServicesContent;
        int exitCode = 0;
        String outputToFeed = "";
        boolean releaseArtifactExists;
        int releaseArtifactChecks;

        CapturingDeployer(String groupIdPrefix, String indexTypeVersion, File outputDir) {
            this("deploy", groupIdPrefix, indexTypeVersion, outputDir);
        }

        CapturingDeployer(String mavenDeployGoal, String groupIdPrefix, String indexTypeVersion, File outputDir) {
            super(mavenDeployGoal, null, null, null,
                    groupIdPrefix, indexTypeVersion, outputDir, null, "https://repo.example/releases",
                    new MavenProject(), new SystemStreamLog());
        }

        CapturingDeployer(String groupIdPrefix, String indexTypeVersion, File outputDir, File pomTemplateFile) {
            super("deploy", null, null, null,
                    groupIdPrefix, indexTypeVersion, outputDir, pomTemplateFile, "https://repo.example/releases",
                    new MavenProject(), new SystemStreamLog());
        }

        CapturingDeployer(String groupIdPrefix, String indexTypeVersion, File outputDir, File pomTemplateFile,
                           MavenProject project) {
            super("deploy", null, null, null,
                    groupIdPrefix, indexTypeVersion, outputDir, pomTemplateFile, "https://repo.example/releases",
                    project, new SystemStreamLog());
        }

        @Override
        protected boolean artifactExistsInReleaseRepository(String groupId, String artifactId, String version) {
            releaseArtifactChecks++;
            return releaseArtifactExists;
        }

        @Override
        protected Invoker createInvoker() {
            return new DefaultInvoker() {
                @Override
                public InvocationResult execute(InvocationRequest request) {
                    capturedRequest = request;
                    try {
                        capturedPomContent = Files.readString(request.getPomFile().toPath());
                        File tempDir = request.getPomFile().getParentFile();
                        File mappingInTempDir = new File(tempDir,
                                "src/main/resources/opensearch/MyType_mapping_v1_0.json");
                        capturedMappingExisted = mappingInTempDir.exists();
                        File servicesFile = new File(tempDir,
                                "src/main/resources/META-INF/services/ch.admin.bit.jeap.opensearch.indextype.IndexType");
                        if (servicesFile.exists()) {
                            capturedServicesContent = Files.readString(servicesFile.toPath());
                        }
                        if (!outputToFeed.isBlank()) {
                            InvocationOutputHandler handler = request.getOutputHandler(null);
                            if (handler != null) {
                                for (String line : outputToFeed.split("\n")) {
                                    handler.consumeLine(line);
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    final int code = exitCode;
                    return new InvocationResult() {
                        @Override
                        public int getExitCode() { return code; }
                        @Override
                        public CommandLineException getExecutionException() { return null; }
                    };
                }
            };
        }
    }

    static class RepositoryCheckingDeployer extends IndexTypeArtifactDeployer {
        RepositoryCheckingDeployer(String releaseRepositoryUrl, File outputDir) {
            super("deploy", null, null, null, "ch.admin.bit", "1.0", outputDir, null,
                    releaseRepositoryUrl, new MavenProject(), new SystemStreamLog());
        }

        boolean artifactExists() throws MojoExecutionException {
            return artifactExistsInReleaseRepository("ch.admin.bit.test", "my-artifact", "1.0");
        }
    }
}
