package ch.admin.bit.jeap.opensearch.registry.generator;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Compiles generated sources with the JDK compiler so that tests can assert that what the
 * generator emits is actually valid Java. The generated code is otherwise only compiled during
 * {@code deploy-index-type-artifacts}, in a forked Maven build, which makes generator defects
 * surface very late.
 */
final class GeneratedSourceCompiler {

    private GeneratedSourceCompiler() {
    }

    /**
     * @return the compiler diagnostics, empty if the sources below {@code sourceRoot} compile
     */
    static List<String> compile(File sourceRoot) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No JDK compiler available — tests must run on a JDK, not a JRE");
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            Path classOutput = Files.createTempDirectory("generated-classes");
            List<File> sources = collectSources(sourceRoot);
            if (sources.isEmpty()) {
                throw new IllegalStateException("No generated sources found below " + sourceRoot);
            }
            List<String> options = List.of(
                    "-classpath", System.getProperty("java.class.path"),
                    "-d", classOutput.toString());
            compiler.getTask(null, fileManager, diagnostics, options, null,
                    fileManager.getJavaFileObjectsFromFiles(sources)).call();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return diagnostics.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                .map(d -> d.getSource().getName() + ":" + d.getLineNumber() + " " + d.getMessage(Locale.ENGLISH))
                .toList();
    }

    private static List<File> collectSources(File sourceRoot) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceRoot.toPath())) {
            return paths.filter(p -> p.toString().endsWith(".java")).map(Path::toFile).toList();
        }
    }
}
