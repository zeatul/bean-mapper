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
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for generic mapper interfaces and interface inheritance.
 * All types are defined in source code strings and loaded via reflection to avoid classloader issues.
 */
class GenericAndInheritanceIntegrationTest {

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
    void shouldImplementGenericBaseMapperMethods() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.generics.EntityA", getSourceCode("EntityA"))
            .addBean("glz.hawk.bean.mapper.integration.generics.DtoA", getSourceCode("DtoA"))
            .addMapper("glz.hawk.bean.mapper.integration.generics.BaseMapper", getSourceCode("BaseMapper"))
            .addMapper("glz.hawk.bean.mapper.integration.generics.EntityAMapper", getSourceCode("EntityAMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.generics.EntityAMapper");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Debug: print all methods on the interface and implementation
        System.out.println("=== Interface methods ===");
        for (Method m : mapperInterface.getMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }
        System.out.println("=== Implementation methods ===");
        for (Method m : implClass.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }

        Class<?> entityAClass = scenario.loadClass("glz.hawk.bean.mapper.integration.generics.EntityA");
        Class<?> dtoAClass = scenario.loadClass("glz.hawk.bean.mapper.integration.generics.DtoA");

        Object source = createEntity(entityAClass, BigInteger.ONE, "Entity A");

        // Find convert method - use implementation class
        Method convertMethod = findMethodOnImpl(implClass, "convert", entityAClass);
        Object result = convertMethod.invoke(mapper, source);
        assertThat(result).isNotNull();
        assertThat(invokeGetter(result, "getId")).isEqualTo(BigInteger.ONE);
        assertThat(invokeGetter(result, "getName")).isEqualTo("Entity A");

        // Test inherited copy method - void copy(EntityA, @MappingTarget DtoA)
        Method copyMethod = findMethodOnImpl(implClass, "copy", entityAClass, dtoAClass);
        Object target = dtoAClass.getDeclaredConstructor().newInstance();
        copyMethod.invoke(mapper, source, target);
        assertThat(invokeGetter(target, "getId")).isEqualTo(BigInteger.ONE);
        assertThat(invokeGetter(target, "getName")).isEqualTo("Entity A");
    }

    @Test
    void shouldImplementSecondGenericMapper() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.generics.EntityB", getSourceCode("EntityB"))
            .addBean("glz.hawk.bean.mapper.integration.generics.DtoB", getSourceCode("DtoB"))
            .addMapper("glz.hawk.bean.mapper.integration.generics.BaseMapper", getSourceCode("BaseMapper"))
            .addMapper("glz.hawk.bean.mapper.integration.generics.EntityBMapper", getSourceCode("EntityBMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.generics.EntityBMapper");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Debug
        System.out.println("=== EntityBMapper Interface methods ===");
        for (Method m : mapperInterface.getMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }
        System.out.println("=== EntityBMapper Implementation methods ===");
        for (Method m : implClass.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }

        Class<?> entityBClass = scenario.loadClass("glz.hawk.bean.mapper.integration.generics.EntityB");
        Class<?> dtoBClass = scenario.loadClass("glz.hawk.bean.mapper.integration.generics.DtoB");

        Object source = createEntityB(entityBClass, 42L, "Entity B");

        // Test inherited convert method
        Method convertMethod = findMethodOnImpl(implClass, "convert", entityBClass);
        Object result = convertMethod.invoke(mapper, source);
        assertThat(result).isNotNull();
        assertThat(invokeGetter(result, "getId")).isEqualTo(42L);
        assertThat(invokeGetter(result, "getTitle")).isEqualTo("Entity B");

        // Test additional method in child interface
        Method extraMethod = findMethodOnImpl(implClass, "convertWithExtra", entityBClass);
        Object result2 = extraMethod.invoke(mapper, source);
        assertThat(result2).isNotNull();
        assertThat(invokeGetter(result2, "getId")).isEqualTo(42L);
        assertThat(invokeGetter(result2, "getTitle")).isEqualTo("Entity B");
    }

    @Test
    void shouldImplementMultiLevelInheritance() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.generics.EntityA", getSourceCode("EntityA"))
            .addBean("glz.hawk.bean.mapper.integration.generics.DtoA", getSourceCode("DtoA"))
            .addMapper("glz.hawk.bean.mapper.integration.generics.AuditableMapper", getSourceCode("AuditableMapper"))
            .addMapper("glz.hawk.bean.mapper.integration.generics.OrderMapper", getSourceCode("OrderMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.generics.OrderMapper");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Debug
        System.out.println("=== OrderMapper Interface methods ===");
        for (Method m : mapperInterface.getMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }
        System.out.println("=== OrderMapper Implementation methods ===");
        for (Method m : implClass.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }

        Class<?> entityAClass = scenario.loadClass("glz.hawk.bean.mapper.integration.generics.EntityA");
        Class<?> dtoAClass = scenario.loadClass("glz.hawk.bean.mapper.integration.generics.DtoA");

        Object source = createEntity(entityAClass, BigInteger.valueOf(100), "Order Entity");

        // Test inherited create method - DtoA create(EntityA)
        Method createMethod = findMethodOnImpl(implClass, "create", entityAClass);
        Object created = createMethod.invoke(mapper, source);
        assertThat(created).isNotNull();
        assertThat(invokeGetter(created, "getId")).isEqualTo(BigInteger.valueOf(100));
        assertThat(invokeGetter(created, "getName")).isEqualTo("Order Entity");

        // Test inherited update method - void update(EntityA, @MappingTarget DtoA)
        Method updateMethod = findMethodOnImpl(implClass, "update", entityAClass, dtoAClass);
        Object target = dtoAClass.getDeclaredConstructor().newInstance();
        updateMethod.invoke(mapper, source, target);
        assertThat(invokeGetter(target, "getId")).isEqualTo(BigInteger.valueOf(100));
        assertThat(invokeGetter(target, "getName")).isEqualTo("Order Entity");
    }

    // ===== Helper methods =====

    private Method findMethodOnImpl(Class<?> implClass, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        for (Method m : implClass.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                Class<?>[] methodParamTypes = m.getParameterTypes();
                if (methodParamTypes.length == paramTypes.length) {
                    boolean match = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        // Check assignability since of type erasure
                        if (!methodParamTypes[i].isAssignableFrom(paramTypes[i]) && !paramTypes[i].isAssignableFrom(methodParamTypes[i])) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return m;
                    }
                }
            }
        }
        throw new NoSuchMethodException("Method " + name + " with params " + Arrays.toString(paramTypes) + " not found in " + implClass.getName());
    }

    private Object createEntity(Class<?> entityClass, BigInteger id, String name) throws Exception {
        Object entity = entityClass.getDeclaredConstructor().newInstance();
        entityClass.getMethod("setId", BigInteger.class).invoke(entity, id);
        entityClass.getMethod("setName", String.class).invoke(entity, name);
        return entity;
    }

    private Object createEntityB(Class<?> entityClass, Long id, String title) throws Exception {
        Object entity = entityClass.getDeclaredConstructor().newInstance();
        entityClass.getMethod("setId", Long.class).invoke(entity, id);
        entityClass.getMethod("setTitle", String.class).invoke(entity, title);
        return entity;
    }

    private Object invokeGetter(Object bean, String getterName) throws Exception {
        return bean.getClass().getMethod(getterName).invoke(bean);
    }

    // ===== Source code definitions =====

    private String getSourceCode(String typeName) {
        return switch (typeName) {
            case "EntityA" -> """
                package glz.hawk.bean.mapper.integration.generics;
                import java.math.BigInteger;
                public class EntityA {
                    private BigInteger id;
                    private String name;
                    public BigInteger getId() { return id; }
                    public void setId(BigInteger id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                }
                """;
            case "DtoA" -> """
                package glz.hawk.bean.mapper.integration.generics;
                import java.math.BigInteger;
                public class DtoA {
                    private BigInteger id;
                    private String name;
                    public BigInteger getId() { return id; }
                    public void setId(BigInteger id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                }
                """;
            case "EntityB" -> """
                package glz.hawk.bean.mapper.integration.generics;
                public class EntityB {
                    private Long id;
                    private String title;
                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getTitle() { return title; }
                    public void setTitle(String title) { this.title = title; }
                }
                """;
            case "DtoB" -> """
                package glz.hawk.bean.mapper.integration.generics;
                public class DtoB {
                    private Long id;
                    private String title;
                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getTitle() { return title; }
                    public void setTitle(String title) { this.title = title; }
                }
                """;
            case "BaseMapper" -> """
                package glz.hawk.bean.mapper.integration.generics;
                import glz.hawk.bean.mapper.annotation.MappingTarget;
                public interface BaseMapper<S, T> {
                    T convert(S source);
                    void copy(S source, @MappingTarget T target);
                }
                """;
            case "EntityAMapper" -> """
                package glz.hawk.bean.mapper.integration.generics;
                import glz.hawk.bean.mapper.annotation.BeanMapper;
                @BeanMapper
                public interface EntityAMapper extends BaseMapper<EntityA, DtoA> {
                }
                """;
            case "EntityBMapper" -> """
                package glz.hawk.bean.mapper.integration.generics;
                import glz.hawk.bean.mapper.annotation.BeanMapper;
                @BeanMapper
                public interface EntityBMapper extends BaseMapper<EntityB, DtoB> {
                    DtoB convertWithExtra(EntityB source);
                }
                """;
            case "AuditableMapper" -> """
                package glz.hawk.bean.mapper.integration.generics;
                import glz.hawk.bean.mapper.annotation.MappingTarget;
                public interface AuditableMapper<S, T> {
                    T create(S source);
                    void update(S source, @MappingTarget T target);
                }
                """;
            case "OrderMapper" -> """
                package glz.hawk.bean.mapper.integration.generics;
                import glz.hawk.bean.mapper.annotation.BeanMapper;
                @BeanMapper
                public interface OrderMapper extends AuditableMapper<EntityA, DtoA> {
                }
                """;
            default -> throw new IllegalArgumentException("Unknown type: " + typeName);
        };
    }
}