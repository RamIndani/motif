package com.uber.motif.compiler;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationFactory;
import com.google.testing.compile.Compiler;
import dagger.internal.codegen.ComponentProcessor;
import motif.compiler.Processor;
import org.junit.rules.TemporaryFolder;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExternalSourceCompiler extends TemporaryFolder {

    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public ClassLoader classLoader;

    private File compilerOutputDir;

    @Override
    protected void before() throws Throwable {
        super.before();
        compilerOutputDir = newFolder();
        classLoader = new URLClassLoader(new URL[]{compilerOutputDir.toURI().toURL()});
    }

    Compilation compile(File dir) throws IOException {
        List<JavaFileObject> files = TestHarness.javaFileObjects(dir);
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        CollectingFileManager fileManager = new CollectingFileManager(diagnosticCollector);
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnosticCollector,
                ImmutableList.of("-d", compilerOutputDir.getAbsolutePath()),
                Collections.emptyList(),
                files);
        task.setProcessors(getProcessors(dir));
        boolean succeeded = task.call();
        return CompilationFactory.create(
                Compiler.javac(),
                files,
                succeeded,
                diagnosticCollector.getDiagnostics(),
                fileManager.getOutputFiles());
    }

    private List<javax.annotation.processing.Processor> getProcessors(File dir) {
        ImmutableList.Builder<javax.annotation.processing.Processor> builder = ImmutableList.<javax.annotation.processing.Processor>builder()
                .add(new ComponentProcessor());

        if (shouldRunProcessor(dir)) {
            builder.add(new Processor());
        }

        return builder.build();
    }

    private boolean shouldRunProcessor(File dir) {
        return !new File(dir, "DO_NOT_PROCESS").exists();
    }

    private class CollectingFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        private final Set<JavaFileObject> outputFiles = new HashSet<>();

        public CollectingFileManager(DiagnosticCollector<JavaFileObject> diagnosticCollector) {
            super(compiler.getStandardFileManager(diagnosticCollector, null, null));
        }

        public Set<JavaFileObject> getOutputFiles() {
            return outputFiles;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            JavaFileObject javaFile = super.getJavaFileForOutput(location, className, kind, sibling);
            outputFiles.add(javaFile);
            return javaFile;
        }
    }
}