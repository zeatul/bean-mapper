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

import glz.hawk.bean.mapper.annotation.*;
import glz.hawk.bean.mapper.support.ComponentModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for basic bean-to-bean mapping scenarios.
 * These tests compile the mapper interfaces with the annotation processor,
 * then load and execute the generated implementations to verify runtime behavior.
 * All types are defined in source code strings and loaded via reflection to avoid classloader issues.
 */
class BasicMappingIntegrationTest {

    private IntegrationTestCompiler compiler;

    @BeforeEach
    void setUp() throws Exception {
        compiler = new IntegrationTestCompiler();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (compiler != null) {
            compiler.close();
        }
    }

    // ===== Tests =====

    @Test
    void shouldMapAllMatchingProperties() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.basic.SimpleMapper", getSourceCode("SimpleMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleTarget");

        Object source = createBean(sourceClass, Map.of(
            "id", 1L,
            "name", "Test",
            "count", 42
        ));

        Object result = mapperInterface.getMethod("toTarget", sourceClass).invoke(mapper, source);

        assertThat(result).isNotNull();
        assertThat(invokeGetter(result, "getId")).isEqualTo(1L);
        assertThat(invokeGetter(result, "getName")).isEqualTo("Test");
        assertThat(invokeGetter(result, "getCount")).isEqualTo(42);
    }

    @Test
    void shouldCopyToExistingTarget() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.basic.CopyMapper", getSourceCode("CopyMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.CopyMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleTarget");

        Object source = createBean(sourceClass, Map.of(
            "id", 2L,
            "name", "Copy Test",
            "count", 100
        ));

        Object target = createBean(targetClass, Map.of(
            "id", 999L,
            "name", "Old",
            "count", -1
        ));

        mapperInterface.getMethod("copy", sourceClass, targetClass).invoke(mapper, source, target);

        assertThat(invokeGetter(target, "getId")).isEqualTo(2L);
        assertThat(invokeGetter(target, "getName")).isEqualTo("Copy Test");
        assertThat(invokeGetter(target, "getCount")).isEqualTo(100);
    }

    @Test
    void shouldHandleNullableSourceReturnNull() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.basic.NullableMapper", getSourceCode("NullableMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.NullableMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleTarget");

        Object result = mapperInterface.getMethod("toTarget", sourceClass).invoke(mapper, (Object) null);

        assertThat(result).isNull();
    }

    @Test
    void shouldHandleNullableSourceWithNonNullValue() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.basic.NullableMapper", getSourceCode("NullableMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.NullableMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleTarget");

        Object source = createBean(sourceClass, Map.of(
            "id", 3L,
            "name", "Non-null"
        ));

        Object result = mapperInterface.getMethod("toTarget", sourceClass).invoke(mapper, source);

        assertThat(result).isNotNull();
        assertThat(invokeGetter(result, "getId")).isEqualTo(3L);
        assertThat(invokeGetter(result, "getName")).isEqualTo("Non-null");
    }

    @Test
    void shouldHandleNullableTargetParameter() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.basic.NullableMapper", getSourceCode("NullableMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.NullableMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleTarget");

        Object source = createBean(sourceClass, Map.of(
            "id", 4L,
            "name", "Target Null Test"
        ));

        // Target is null but @MappingTarget(nullable=true) should allow it
        mapperInterface.getMethod("copy", sourceClass, targetClass).invoke(mapper, source, null);
        // No exception expected, just returns early
    }

    @Test
    void shouldMapExtraFieldsWhenNamesMatch() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.basic.SourceWithExtra", getSourceCode("SourceWithExtra"))
            .addBean("glz.hawk.bean.mapper.integration.basic.TargetWithExtra", getSourceCode("TargetWithExtra"))
            .addMapper("glz.hawk.bean.mapper.integration.basic.ExtraFieldsMapper", getSourceCode("ExtraFieldsMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.ExtraFieldsMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SourceWithExtra");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.TargetWithExtra");

        Object source = createBean(sourceClass, Map.of(
            "id", 5L,
            "name", "Extra",
            "extraField", "Extra Value"
        ));

        Object result = mapperInterface.getMethod("toTarget", sourceClass).invoke(mapper, source);

        assertThat(result).isNotNull();
        assertThat(invokeGetter(result, "getId")).isEqualTo(5L);
        assertThat(invokeGetter(result, "getName")).isEqualTo("Extra");
        assertThat(invokeGetter(result, "getExtraField")).isEqualTo("Extra Value");
    }

    @Test
    void shouldThrowWhenNonNullSourceIsNull() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.basic.SimpleMapper", getSourceCode("SimpleMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleSource");

        assertThatThrownBy(() -> mapperInterface.getMethod("toTarget", sourceClass).invoke(mapper, (Object) null))
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .satisfies(t -> assertThat(t.getCause().getMessage()).contains("can't be null"));
    }

    @Test
    void shouldThrowWhenNonNullTargetIsNull() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.basic.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.basic.CopyMapper", getSourceCode("CopyMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.CopyMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.basic.SimpleTarget");

        Object source = createBean(sourceClass, Map.of("id", 1L));

        assertThatThrownBy(() -> mapperInterface.getMethod("copy", sourceClass, targetClass).invoke(mapper, source, null))
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .satisfies(t -> assertThat(t.getCause().getMessage()).contains("can't be null"));
    }

    // ===== Helper methods =====

    private Object createBean(Class<?> beanClass, Map<String, Object> properties) throws Exception {
        Object bean = beanClass.getDeclaredConstructor().newInstance();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String setterName = "set" + Character.toUpperCase(entry.getKey().charAt(0)) + entry.getKey().substring(1);
            Method setter = beanClass.getMethod(setterName, entry.getValue().getClass());
            setter.invoke(bean, entry.getValue());
        }
        return bean;
    }

    private Object invokeGetter(Object bean, String getterName) throws Exception {
        return bean.getClass().getMethod(getterName).invoke(bean);
    }

    // ===== Source code definitions =====

    private String getSourceCode(String typeName) {
        return switch (typeName) {
            case "SimpleSource" -> """
                package glz.hawk.bean.mapper.integration.basic;
                public class SimpleSource {
                    private Long id;
                    private String name;
                    private Integer count;
                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                    public Integer getCount() { return count; }
                    public void setCount(Integer count) { this.count = count; }
                }
                """;
            case "SimpleTarget" -> """
                package glz.hawk.bean.mapper.integration.basic;
                public class SimpleTarget {
                    private Long id;
                    private String name;
                    private Integer count;
                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                    public Integer getCount() { return count; }
                    public void setCount(Integer count) { this.count = count; }
                }
                """;
            case "SourceWithExtra" -> """
                package glz.hawk.bean.mapper.integration.basic;
                public class SourceWithExtra {
                    private Long id;
                    private String name;
                    private String extraField;
                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                    public String getExtraField() { return extraField; }
                    public void setExtraField(String extraField) { this.extraField = extraField; }
                }
                """;
            case "TargetWithExtra" -> """
                package glz.hawk.bean.mapper.integration.basic;
                public class TargetWithExtra {
                    private Long id;
                    private String name;
                    private String extraField;
                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                    public String getExtraField() { return extraField; }
                    public void setExtraField(String extraField) { this.extraField = extraField; }
                }
                """;
            case "SimpleMapper" -> """
                package glz.hawk.bean.mapper.integration.basic;
                import glz.hawk.bean.mapper.annotation.BeanMapper;
                @BeanMapper
                public interface SimpleMapper {
                    SimpleTarget toTarget(SimpleSource source);
                }
                """;
            case "CopyMapper" -> """
                package glz.hawk.bean.mapper.integration.basic;
                import glz.hawk.bean.mapper.annotation.BeanMapper;
                import glz.hawk.bean.mapper.annotation.MappingTarget;
                @BeanMapper
                public interface CopyMapper {
                    void copy(SimpleSource source, @MappingTarget SimpleTarget target);
                }
                """;
            case "NullableMapper" -> """
                package glz.hawk.bean.mapper.integration.basic;
                import glz.hawk.bean.mapper.annotation.BeanMapper;
                import glz.hawk.bean.mapper.annotation.MappingSource;
                import glz.hawk.bean.mapper.annotation.MappingTarget;
                @BeanMapper
                public interface NullableMapper {
                    SimpleTarget toTarget(@MappingSource(nullable = true) SimpleSource source);
                    void copy(@MappingSource(nullable = true) SimpleSource source, @MappingTarget(nullable = true) SimpleTarget target);
                }
                """;
            case "ExtraFieldsMapper" -> """
                package glz.hawk.bean.mapper.integration.basic;
                import glz.hawk.bean.mapper.annotation.BeanMapper;
                import glz.hawk.bean.mapper.annotation.MappingTarget;
                @BeanMapper
                public interface ExtraFieldsMapper {
                    TargetWithExtra toTarget(SourceWithExtra source);
                    void copy(SourceWithExtra source, @MappingTarget TargetWithExtra target);
                }
                """;
            default -> throw new IllegalArgumentException("Unknown type: " + typeName);
        };
    }
}