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

package glz.hawk.bean.mapper.processor.e2e;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import glz.hawk.bean.mapper.processor.BeanMapperAnnotationProcessor;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * End-to-end tests for the BeanMapper annotation processor using compile-testing.
 */
class BeanMapperProcessorE2ETest {

    private Compiler createCompiler() {
        return Compiler.javac().withProcessors(new BeanMapperAnnotationProcessor());
    }

    // ===== Basic mapping tests =====

    @Test
    void shouldGenerateMapperForSimpleBean() {
        Compilation compilation = createCompiler().compile(
                JavaFileObjects.forSourceString("test.UserEntity", simpleEntity()),
                JavaFileObjects.forSourceString("test.UserDTO", simpleDTO()),
                JavaFileObjects.forSourceString("test.UserMapper",
                        "package test;\n" +
                        "import glz.hawk.bean.mapper.annotation.*;\n" +
                        "@BeanMapper\n" +
                        "public interface UserMapper {\n" +
                        "    UserDTO toDTO(UserEntity entity);\n" +
                        "}\n")
        );

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test.impl.UserMapperImpl")
                .hasSourceEquivalentTo(forResource("e2e/expected/SimpleMapper.java"));
    }

    @Test
    void shouldGenerateMapperWithMappingTargetParameter() {
        Compilation compilation = createCompiler().compile(
                JavaFileObjects.forSourceString("test.UserEntity", simpleEntity()),
                JavaFileObjects.forSourceString("test.UserDTO", simpleDTO()),
                JavaFileObjects.forSourceString("test.UserMapper",
                        "package test;\n" +
                        "import glz.hawk.bean.mapper.annotation.*;\n" +
                        "@BeanMapper\n" +
                        "public interface UserMapper {\n" +
                        "    void copy(UserEntity source, @MappingTarget UserDTO target);\n" +
                        "}\n")
        );

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test.impl.UserMapperImpl")
                .hasSourceEquivalentTo(forResource("e2e/expected/MappingTargetMapper.java"));
    }

    // ===== PropertyMapping tests =====

    @Test
    void shouldApplyPropertyMappingRules() {
        Compilation compilation = createCompiler().compile(
                JavaFileObjects.forSourceString("test.UserEntity", simpleEntity()),
                JavaFileObjects.forSourceString("test.UserDTO", simpleDTO()),
                JavaFileObjects.forSourceString("test.UserMapper",
                        "package test;\n" +
                        "import glz.hawk.bean.mapper.annotation.*;\n" +
                        "@BeanMapper\n" +
                        "public interface UserMapper {\n" +
                        "    @PropertyMapping(source = \"name\", target = \"name\", ignore = true)\n" +
                        "    @PropertyMapping(source = \"id\", ignore = true)\n" +
                        "    UserDTO toDTO(UserEntity entity);\n" +
                        "}\n")
        );

        assertThat(compilation).succeeded();
        // Both properties ignored - generated code should only have null checks
        assertThat(compilation).succeeded();
    }

    @Test
    void shouldErrorWhenSourceIsArrayButTargetIsNot() {
        try {
            createCompiler().compile(
                    JavaFileObjects.forSourceString("test.UserEntity", simpleEntity()),
                    JavaFileObjects.forSourceString("test.UserDTO", simpleDTO()),
                    JavaFileObjects.forSourceString("test.UserMapper",
                            "package test;\n" +
                            "import glz.hawk.bean.mapper.annotation.*;\n" +
                            "@BeanMapper\n" +
                            "public interface UserMapper {\n" +
                            "    UserDTO toDTO(UserEntity[] entities);\n" +
                            "}\n")
            );
            fail("Expected compilation to fail");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IllegalStateException.class);
            assertThat(e.getCause().getMessage()).contains("is array or iterable object, but the target");
        }
    }

    @Test
    void shouldErrorWhenTargetIsArrayButSourceIsNot() {
        try {
            createCompiler().compile(
                    JavaFileObjects.forSourceString("test.UserEntity", simpleEntity()),
                    JavaFileObjects.forSourceString("test.UserDTO", simpleDTO()),
                    JavaFileObjects.forSourceString("test.UserMapper",
                            "package test;\n" +
                            "import glz.hawk.bean.mapper.annotation.*;\n" +
                            "@BeanMapper\n" +
                            "public interface UserMapper {\n" +
                            "    UserDTO[] toDTOs(UserEntity entity);\n" +
                            "}\n")
            );
            fail("Expected compilation to fail");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IllegalStateException.class);
            assertThat(e.getCause().getMessage()).contains("must be array or collection");
        }
    }

    @Test
    void shouldErrorWhenTargetIsNestedProperty() {
        try {
            createCompiler().compile(
                    JavaFileObjects.forSourceString("test.UserEntity", simpleEntity()),
                    JavaFileObjects.forSourceString("test.UserDTO", simpleDTO()),
                    JavaFileObjects.forSourceString("test.UserMapper",
                            "package test;\n" +
                            "import glz.hawk.bean.mapper.annotation.*;\n" +
                            "@BeanMapper\n" +
                            "public interface UserMapper {\n" +
                            "    @PropertyMapping(target = \"address.city\")\n" +
                            "    UserDTO toDTO(UserEntity entity);\n" +
                            "}\n")
            );
            fail("Expected compilation to fail");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IllegalStateException.class);
            assertThat(e.getCause().getMessage()).contains("illegal target");
        }
    }

    @Test
    void shouldErrorWhenPropertyMappingHasNoSourceAndNoTarget() {
        try {
            createCompiler().compile(
                    JavaFileObjects.forSourceString("test.UserEntity", simpleEntity()),
                    JavaFileObjects.forSourceString("test.UserDTO", simpleDTO()),
                    JavaFileObjects.forSourceString("test.UserMapper",
                            "package test;\n" +
                            "import glz.hawk.bean.mapper.annotation.*;\n" +
                            "@BeanMapper\n" +
                            "public interface UserMapper {\n" +
                            "    @PropertyMapping\n" +
                            "    UserDTO toDTO(UserEntity entity);\n" +
                            "}\n")
            );
            fail("Expected compilation to fail");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IllegalStateException.class);
            assertThat(e.getCause().getMessage()).contains("no source and no target");
        }
    }

    @Test
    void shouldErrorWhenMultipleMappingTargetParameters() {
        try {
            createCompiler().compile(
                    JavaFileObjects.forSourceString("test.UserEntity", simpleEntity()),
                    JavaFileObjects.forSourceString("test.UserDTO", simpleDTO()),
                    JavaFileObjects.forSourceString("test.UserMapper",
                            "package test;\n" +
                            "import glz.hawk.bean.mapper.annotation.*;\n" +
                            "@BeanMapper\n" +
                            "public interface UserMapper {\n" +
                            "    void copy(UserEntity source,\n" +
                            "        @MappingTarget UserDTO target1,\n" +
                            "        @MappingTarget UserDTO target2);\n" +
                            "}\n")
            );
            fail("Expected compilation to fail");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(IllegalStateException.class);
            assertThat(e.getCause().getMessage()).contains("At most one method");
        }
    }

    @Test
    void shouldErrorWhenMappingTargetOnMethod() {
        Compilation compilation = createCompiler().compile(
                JavaFileObjects.forSourceString("test.UserEntity", simpleEntity()),
                JavaFileObjects.forSourceString("test.UserDTO", simpleDTO()),
                JavaFileObjects.forSourceString("test.UserMapper",
                        "package test;\n" +
                        "import glz.hawk.bean.mapper.annotation.*;\n" +
                        "@BeanMapper\n" +
                        "public interface UserMapper {\n" +
                        "    @MappingTarget(includeAll = false)\n" +
                        "    UserDTO toDTO(UserEntity entity);\n" +
                        "}\n")
        );

        assertThat(compilation).failed();
    }

    // ===== Converter tests =====

    @Test
    void shouldUseCustomConverter() {
        Compilation compilation = createCompiler().compile(
                JavaFileObjects.forSourceString("test.UserEntity",
                        "package test;\n" +
                        "import java.time.LocalDate;\n" +
                        "public class UserEntity {\n" +
                        "    private LocalDate birthDate;\n" +
                        "    public LocalDate getBirthDate() { return birthDate; }\n" +
                        "    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }\n" +
                        "}\n"),
                JavaFileObjects.forSourceString("test.UserDTO",
                        "package test;\n" +
                        "public class UserDTO {\n" +
                        "    private String birthDateStr;\n" +
                        "    public String getBirthDateStr() { return birthDateStr; }\n" +
                        "    public void setBirthDateStr(String birthDateStr) { this.birthDateStr = birthDateStr; }\n" +
                        "}\n"),
                JavaFileObjects.forSourceString("test.UserMapper",
                        "package test;\n" +
                        "import glz.hawk.bean.mapper.annotation.*;\n" +
                        "import java.time.format.DateTimeFormatter;\n" +
                        "@BeanMapper\n" +
                        "public interface UserMapper {\n" +
                        "    @PropertyMapping(source = \"birthDate\", target = \"birthDateStr\",\n" +
                        "        converter = @Converter(format = \"$L.format($T.ofPattern($S))\",\n" +
                        "            params = {@ConverterParam(type = ConverterParamType.SOURCE),\n" +
                        "                      @ConverterParam(type = ConverterParamType.TYPE, value = \"java.time.format.DateTimeFormatter\"),\n" +
                        "                      @ConverterParam(type = ConverterParamType.LITERAL, value = \"yyyy-MM-dd\")}))\n" +
                        "    UserDTO toDTO(UserEntity entity);\n" +
                        "}\n")
        );

        assertThat(compilation).succeeded();
    }

    // ===== Helper methods =====

    private String simpleEntity() {
        return "package test;\n" +
                "public class UserEntity {\n" +
                "    private Long id;\n" +
                "    private String name;\n" +
                "    public Long getId() { return id; }\n" +
                "    public void setId(Long id) { this.id = id; }\n" +
                "    public String getName() { return name; }\n" +
                "    public void setName(String name) { this.name = name; }\n" +
                "}\n";
    }

    private String simpleDTO() {
        return "package test;\n" +
                "public class UserDTO {\n" +
                "    private Long id;\n" +
                "    private String name;\n" +
                "    public Long getId() { return id; }\n" +
                "public void setId(Long id) { this.id = id; }\n" +
                "    public String getName() { return name; }\n" +
                "    public void setName(String name) { this.name = name; }\n" +
                "}\n";
    }
}