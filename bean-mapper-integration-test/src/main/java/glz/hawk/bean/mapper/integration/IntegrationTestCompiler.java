/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package glz.hawk.bean.mapper.integration;

import glz.hawk.bean.mapper.annotation.BeanMapper;
import glz.hawk.bean.mapper.annotation.MappingTarget;
import glz.hawk.bean.mapper.processor.BeanMapperAnnotationProcessor;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility for integration tests: compiles mapper interfaces with the annotation processor,
 * then compiles and loads the generated implementation classes for runtime verification.
 */
public class IntegrationTestCompiler {

    private final JavaCompiler compiler;
    private final StandardJavaFileManager fileManager;
    private final Path tempDir;
    private final List<String> processorOptions;
    
    private final List<Path> sourceRoots = new ArrayList<>();
    private final List<String> classNames = new ArrayList<>();

    public IntegrationTestCompiler() throws IOException {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        this.tempDir = Files.createTempDirectory("bean-mapper-integration-test-");
        this.processorOptions = new ArrayList<>(Arrays.asList("-processor", BeanMapperAnnotationProcessor.class.getName()));
        
        // Add current module's classpath to processor path
        String classpath = System.getProperty("java.class.path");
        this.processorOptions.addAll(Arrays.asList("--class-path", classpath));
    }

    /**
     * Add a source root directory containing test mapper interfaces and beans.
     */
    public IntegrationTestCompiler addSourceRoot(Path sourceRoot) {
        sourceRoots.add(sourceRoot);
        return this;
    }

    /**
     * Add a Java source file from a string.
     */
    public IntegrationTestCompiler addSourceString(String className, String sourceCode) throws IOException {
        Path file = tempDir.resolve(className.replace('.', '/') + ".java");
        Files.createDirectories(file.getParent());
        Files.writeString(file, sourceCode, StandardCharsets.UTF_8);
        classNames.add(className);
        return this;
    }

    /**
     * Compile all added sources with the annotation processor.
     * Returns a class loader that can load both the source classes and generated implementations.
     */
    public URLClassLoader compile() throws Exception {
        // Collect all source files
        List<Path> allSources = new ArrayList<>();
        for (Path root : sourceRoots) {
            Files.walk(root)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(allSources::add);
        }
        // Add string-based sources
        allSources.addAll(classNames.stream()
            .map(name -> tempDir.resolve(name.replace('.', '/') + ".java"))
            .collect(Collectors.toList()));

        if (allSources.isEmpty()) {
            throw new IllegalStateException("No source files to compile");
        }

        // Create compilation task
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromPaths(allSources);
        
        // Output to temp directory
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(tempDir.toFile()));
        
        JavaCompiler.CompilationTask task = compiler.getTask(
            null, fileManager, null,
            processorOptions, null, compilationUnits
        );

        Boolean success = task.call();
        if (!success) {
            throw new IllegalStateException("Compilation failed");
        }

        // Return class loader for the compiled classes
        return new URLClassLoader(new URL[]{tempDir.toUri().toURL()}, getClass().getClassLoader());
    }

    /**
     * Load a mapper implementation from the compiled classes.
     * The implementation class is expected to be in the same package with "Impl" suffix,
     * or in the ".impl" subpackage.
     */
    @SuppressWarnings("unchecked")
    public <T> T loadMapper(Class<T> mapperInterface, URLClassLoader classLoader) throws Exception {
        // Load the mapper interface from the same classloader to avoid classloader mismatch
        Class<?> mapperInterfaceFromLoader = classLoader.loadClass(mapperInterface.getName());
        
        String implPackageName = mapperInterface.getPackageName() + ".impl";
        String fullImplName = implPackageName + "." + mapperInterface.getSimpleName() + "Impl";
        
        Class<?> implClass = null;
        try {
            implClass = classLoader.loadClass(fullImplName);
        } catch (ClassNotFoundException e) {
            // Try without .impl package
            try {
                implClass = classLoader.loadClass(mapperInterface.getPackageName() + "." + mapperInterface.getSimpleName() + "Impl");
            } catch (ClassNotFoundException e2) {
                throw new IllegalStateException("Generated implementation not found for " + mapperInterface.getName(), e2);
            }
        }
        
        // Verify the implementation implements the interface (from the same classloader)
        if (!mapperInterfaceFromLoader.isAssignableFrom(implClass)) {
            throw new IllegalStateException("Generated class " + implClass.getName() + " does not implement " + mapperInterfaceFromLoader.getName());
        }
        
        return (T) implClass.getDeclaredConstructor().newInstance();
    }

    /**
     * Clean up temporary files.
     */
    public void close() throws IOException {
        fileManager.close();
        deleteRecursively(tempDir);
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }

    /**
     * Builder for creating test scenarios.
     */
    public static class ScenarioBuilder {
        private final IntegrationTestCompiler compiler;
        private final Map<String, String> sources = new LinkedHashMap<>();

        public ScenarioBuilder(IntegrationTestCompiler compiler) {
            this.compiler = compiler;
        }

        public ScenarioBuilder addBean(String className, String sourceCode) {
            sources.put(className, sourceCode);
            return this;
        }

        public ScenarioBuilder addMapper(String className, String sourceCode) {
            sources.put(className, sourceCode);
            return this;
        }

        public CompiledScenario build() throws IOException {
            for (Map.Entry<String, String> entry : sources.entrySet()) {
                compiler.addSourceString(entry.getKey(), entry.getValue());
            }
            return new CompiledScenario(compiler);
        }
    }

    public static class CompiledScenario {
        private final IntegrationTestCompiler compiler;
        private URLClassLoader classLoader;

        public CompiledScenario(IntegrationTestCompiler compiler) {
            this.compiler = compiler;
        }

        public URLClassLoader getClassLoader() throws Exception {
            if (classLoader == null) {
                classLoader = compiler.compile();
            }
            return classLoader;
        }

        @SuppressWarnings("unchecked")
        public <T> T getMapper(Class<T> mapperInterface) throws Exception {
            return compiler.loadMapper(mapperInterface, getClassLoader());
        }

        public Class<?> loadClass(String className) throws Exception {
            return getClassLoader().loadClass(className);
        }
    }

    public ScenarioBuilder scenario() {
        return new ScenarioBuilder(this);
    }
}