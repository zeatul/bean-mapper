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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for component model annotations (Spring, CDI, Jakarta).
 * These tests verify that generated implementations have the correct annotations.
 * All types are defined in source code strings and loaded via reflection to avoid classloader issues.
 */
class ComponentModelIntegrationTest {

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
    void defaultMapperShouldHaveNoAnnotations() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.componentmodel.DefaultMapper", getSourceCode("DefaultMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.componentmodel.DefaultMapper");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Should have no component annotations
        assertThat(implClass.getAnnotations()).isEmpty();

        // Should be instantiable without DI container
        assertThat(mapper).isNotNull();

        // Verify it works
        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.componentmodel.SimpleSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.componentmodel.SimpleTarget");

        Object source = createBean(sourceClass, Map.of(
            "id", 1L,
            "name", "Test"
        ));

        Object result = mapperInterface.getMethod("toTarget", sourceClass).invoke(mapper, source);
        assertThat(invokeGetter(result, "getId")).isEqualTo(1L);
    }

    @Test
    void springMapperShouldHaveComponentAnnotation() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.componentmodel.SpringMapper", getSourceCode("SpringMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.componentmodel.SpringMapper");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Check for @Component annotation
        Annotation componentAnn = findAnnotation(implClass, "org.springframework.stereotype.Component");
        assertThat(componentAnn).isNotNull();
    }

    @Test
    void springMapperWithAutowiredShouldHaveAutowiredFields() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.componentmodel.DefaultMapper", getSourceCode("DefaultMapper"))
            .addMapper("glz.hawk.bean.mapper.integration.componentmodel.SpringWithAutowiredMapper", getSourceCode("SpringWithAutowiredMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.componentmodel.SpringWithAutowiredMapper");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Check for @Component annotation
        Annotation componentAnn = findAnnotation(implClass, "org.springframework.stereotype.Component");
        assertThat(componentAnn).isNotNull();

        // Check for @Autowired field
        Field[] fields = implClass.getDeclaredFields();
        boolean foundAutowired = false;
        for (Field field : fields) {
            Annotation autowiredAnn = findAnnotation(field, "org.springframework.beans.factory.annotation.Autowired");
            if (autowiredAnn != null) {
                foundAutowired = true;
                assertThat(field.getName()).isEqualTo("otherMapper");
                assertThat(field.getType().getSimpleName()).isEqualTo("DefaultMapper");
                break;
            }
        }
        assertThat(foundAutowired).isTrue();
    }

    @Test
    void jakartaCdiMapperShouldHaveApplicationScoped() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.componentmodel.JakartaCdiMapper", getSourceCode("JakartaCdiMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.componentmodel.JakartaCdiMapper");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Check for @ApplicationScoped annotation (jakarta)
        Annotation scopedAnn = findAnnotation(implClass, "jakarta.enterprise.context.ApplicationScoped");
        assertThat(scopedAnn).isNotNull();
    }

    @Test
    void jsr330MapperShouldHaveNamedAndSingleton() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.componentmodel.Jsr330Mapper", getSourceCode("Jsr330Mapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.componentmodel.Jsr330Mapper");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Check for @Named and @Singleton annotations (javax)
        Annotation namedAnn = findAnnotation(implClass, "javax.inject.Named");
        assertThat(namedAnn).isNotNull();

        Annotation singletonAnn = findAnnotation(implClass, "javax.inject.Singleton");
        assertThat(singletonAnn).isNotNull();
    }

    @Test
    void jakartaMapperShouldHaveNamedAndSingleton() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleSource", getSourceCode("SimpleSource"))
            .addBean("glz.hawk.bean.mapper.integration.componentmodel.SimpleTarget", getSourceCode("SimpleTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.componentmodel.JakartaMapper", getSourceCode("JakartaMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.componentmodel.JakartaMapper");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Check for @Named and @Singleton annotations (jakarta)
        Annotation namedAnn = findAnnotation(implClass, "jakarta.inject.Named");
        assertThat(namedAnn).isNotNull();

        Annotation singletonAnn = findAnnotation(implClass, "jakarta.inject.Singleton");
        assertThat(singletonAnn).isNotNull();
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

    private Annotation findAnnotation(Class<?> clazz, String annotationName) {
        try {
            Class<? extends Annotation> annClass = Class.forName(annotationName).asSubclass(Annotation.class);
            return clazz.getAnnotation(annClass);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private Annotation findAnnotation(Field field, String annotationName) {
        try {
            Class<? extends Annotation> annClass = Class.forName(annotationName).asSubclass(Annotation.class);
            return field.getAnnotation(annClass);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // ===== Source code definitions =====

    private String getSourceCode(String typeName) {
        return switch (typeName) {
            case "SimpleSource" -> """
                package glz.hawk.bean.mapper.integration.componentmodel;
                public class SimpleSource {
                    private Long id;
                    private String name;
                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                }
                """;
            case "SimpleTarget" -> """
                package glz.hawk.bean.mapper.integration.componentmodel;
                public class SimpleTarget {
                    private Long id;
                    private String name;
                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                }
                """;
            case "DefaultMapper" -> """
                package glz.hawk.bean.mapper.integration.componentmodel;
                import glz.hawk.bean.mapper.annotation.*;
                import glz.hawk.bean.mapper.support.ComponentModel;
                @BeanMapper(componentModel = ComponentModel.DEFAULT)
                public interface DefaultMapper {
                    SimpleTarget toTarget(SimpleSource source);
                }
                """;
            case "SpringMapper" -> """
                package glz.hawk.bean.mapper.integration.componentmodel;
                import glz.hawk.bean.mapper.annotation.*;
                import glz.hawk.bean.mapper.support.ComponentModel;
                @BeanMapper(componentModel = ComponentModel.SPRING)
                public interface SpringMapper {
                    SimpleTarget toTarget(SimpleSource source);
                }
                """;
            case "SpringWithAutowiredMapper" -> """
                package glz.hawk.bean.mapper.integration.componentmodel;
                import glz.hawk.bean.mapper.annotation.*;
                import glz.hawk.bean.mapper.support.ComponentModel;
                @BeanMapper(componentModel = ComponentModel.SPRING,
                    autowiredBeans = {
                        @AutowiredBean(type = "glz.hawk.bean.mapper.integration.componentmodel.DefaultMapper", fieldName = "otherMapper")
                    }
                )
                public interface SpringWithAutowiredMapper {
                    SimpleTarget toTarget(SimpleSource source);
                }
                """;
            case "JakartaCdiMapper" -> """
                package glz.hawk.bean.mapper.integration.componentmodel;
                import glz.hawk.bean.mapper.annotation.*;
                import glz.hawk.bean.mapper.support.ComponentModel;
                @BeanMapper(componentModel = ComponentModel.JAKARTA_CDI)
                public interface JakartaCdiMapper {
                    SimpleTarget toTarget(SimpleSource source);
                }
                """;
            case "Jsr330Mapper" -> """
                package glz.hawk.bean.mapper.integration.componentmodel;
                import glz.hawk.bean.mapper.annotation.*;
                import glz.hawk.bean.mapper.support.ComponentModel;
                @BeanMapper(componentModel = ComponentModel.JSR330)
                public interface Jsr330Mapper {
                    SimpleTarget toTarget(SimpleSource source);
                }
                """;
            case "JakartaMapper" -> """
                package glz.hawk.bean.mapper.integration.componentmodel;
                import glz.hawk.bean.mapper.annotation.*;
                import glz.hawk.bean.mapper.support.ComponentModel;
                @BeanMapper(componentModel = ComponentModel.JAKARTA)
                public interface JakartaMapper {
                    SimpleTarget toTarget(SimpleSource source);
                }
                """;
            default -> throw new IllegalArgumentException("Unknown type: " + typeName);
        };
    }
}