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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for collection and array mapping scenarios.
 * All types are defined in source code strings and loaded via reflection to avoid classloader issues.
 * Note: Processor requires @ElementConverter for all collection/array mappings.
 */
class CollectionMappingIntegrationTest {

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

    // ===== Tests with ElementConverter =====

    @Test
    void shouldConvertListToListWithElementConverter() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.collection.BookSource", getSourceCode("BookSource"))
            .addBean("glz.hawk.bean.mapper.integration.collection.BookTarget", getSourceCode("BookTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.collection.ListToListMapperWithConverter", getSourceCode("ListToListMapperWithConverter"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.ListToListMapperWithConverter");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Debug: print all methods
        System.out.println("=== ListToListMapperWithConverter Implementation methods ===");
        for (Method m : implClass.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookTarget");
        Class<?> listClass = Class.forName("java.util.List");

        List<Object> sources = Arrays.asList(
            createBook(sourceClass, BigInteger.ONE, "Book 1"),
            createBook(sourceClass, BigInteger.valueOf(2), "Book 2"),
            createBook(sourceClass, BigInteger.valueOf(3), "Book 3")
        );

        Method toListMethod = findMethodOnImpl(implClass, "toList", listClass);
        Object result = toListMethod.invoke(mapper, sources);

        assertThat(result).isInstanceOf(listClass);
        List<?> resultList = (List<?>) result;
        assertThat(resultList).hasSize(3);
        assertThat(invokeGetter(resultList.get(0), "getId")).isEqualTo(BigInteger.ONE);
        assertThat(invokeGetter(resultList.get(0), "getTitle")).isEqualTo("Book 1");
        assertThat(invokeGetter(resultList.get(1), "getId")).isEqualTo(BigInteger.valueOf(2));
        assertThat(invokeGetter(resultList.get(2), "getId")).isEqualTo(BigInteger.valueOf(3));
    }

    @Test
    void shouldConvertArrayToListWithElementConverter() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.collection.BookSource", getSourceCode("BookSource"))
            .addBean("glz.hawk.bean.mapper.integration.collection.BookTarget", getSourceCode("BookTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.collection.ArrayToListMapperWithConverter", getSourceCode("ArrayToListMapperWithConverter"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.ArrayToListMapperWithConverter");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Debug
        System.out.println("=== ArrayToListMapperWithConverter Implementation methods ===");
        for (Method m : implClass.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookTarget");
        Class<?> listClass = Class.forName("java.util.List");
        Class<?> arrayClass = Array.newInstance(sourceClass, 0).getClass();

        // Create properly typed array using Array.newInstance
        Object sources = Array.newInstance(sourceClass, 2);
        Array.set(sources, 0, createBook(sourceClass, BigInteger.ONE, "Book 1"));
        Array.set(sources, 1, createBook(sourceClass, BigInteger.valueOf(2), "Book 2"));

        Method toListMethod = findMethodOnImpl(implClass, "toList", arrayClass);
        Object result = toListMethod.invoke(mapper, sources);

        assertThat(result).isInstanceOf(listClass);
        List<?> resultList = (List<?>) result;
        assertThat(resultList).hasSize(2);
        assertThat(invokeGetter(resultList.get(0), "getId")).isEqualTo(BigInteger.ONE);
        assertThat(invokeGetter(resultList.get(1), "getId")).isEqualTo(BigInteger.valueOf(2));
    }

    @Test
    void shouldConvertListToArrayWithElementConverter() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.collection.BookSource", getSourceCode("BookSource"))
            .addBean("glz.hawk.bean.mapper.integration.collection.BookTarget", getSourceCode("BookTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.collection.ListToArrayMapperWithConverter", getSourceCode("ListToArrayMapperWithConverter"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.ListToArrayMapperWithConverter");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Debug
        System.out.println("=== ListToArrayMapperWithConverter Implementation methods ===");
        for (Method m : implClass.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookTarget");
        Class<?> listClass = Class.forName("java.util.List");
        Class<?> targetArrayClass = Array.newInstance(targetClass, 0).getClass();

        List<Object> sources = Arrays.asList(
            createBook(sourceClass, BigInteger.ONE, "Book 1"),
            createBook(sourceClass, BigInteger.valueOf(2), "Book 2")
        );

        Method toArrayMethod = findMethodOnImpl(implClass, "toArray", listClass);
        Object result = toArrayMethod.invoke(mapper, sources);

        assertThat(result).isInstanceOf(targetArrayClass);
        Object[] resultArray = (Object[]) result;
        assertThat(resultArray).hasSize(2);
        assertThat(invokeGetter(resultArray[0], "getId")).isEqualTo(BigInteger.ONE);
        assertThat(invokeGetter(resultArray[1], "getId")).isEqualTo(BigInteger.valueOf(2));
    }

    @Test
    void shouldConvertArrayToArrayWithElementConverter() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.collection.BookSource", getSourceCode("BookSource"))
            .addBean("glz.hawk.bean.mapper.integration.collection.BookTarget", getSourceCode("BookTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.collection.ArrayToArrayMapperWithConverter", getSourceCode("ArrayToArrayMapperWithConverter"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.ArrayToArrayMapperWithConverter");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Debug
        System.out.println("=== ArrayToArrayMapperWithConverter Implementation methods ===");
        for (Method m : implClass.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookTarget");
        Class<?> sourceArrayClass = Array.newInstance(sourceClass, 0).getClass();
        Class<?> targetArrayClass = Array.newInstance(targetClass, 0).getClass();

        // Create properly typed array using Array.newInstance
        Object sources = Array.newInstance(sourceClass, 2);
        Array.set(sources, 0, createBook(sourceClass, BigInteger.ONE, "Book 1"));
        Array.set(sources, 1, createBook(sourceClass, BigInteger.valueOf(2), "Book 2"));

        Method toArrayMethod = findMethodOnImpl(implClass, "toArray", sourceArrayClass);
        Object result = toArrayMethod.invoke(mapper, sources);

        assertThat(result).isInstanceOf(targetArrayClass);
        Object[] resultArray = (Object[]) result;
        assertThat(resultArray).hasSize(2);
        assertThat(invokeGetter(resultArray[0], "getId")).isEqualTo(BigInteger.ONE);
        assertThat(invokeGetter(resultArray[1], "getId")).isEqualTo(BigInteger.valueOf(2));
    }

    @Test
    void shouldConvertIterableToListWithElementConverter() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.collection.BookSource", getSourceCode("BookSource"))
            .addBean("glz.hawk.bean.mapper.integration.collection.BookTarget", getSourceCode("BookTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.collection.IterableToListMapperWithConverter", getSourceCode("IterableToListMapperWithConverter"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.IterableToListMapperWithConverter");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Debug
        System.out.println("=== IterableToListMapperWithConverter Implementation methods ===");
        for (Method m : implClass.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookTarget");
        Class<?> iterableClass = Class.forName("java.lang.Iterable");
        Class<?> listClass = Class.forName("java.util.List");

        List<Object> sources = Arrays.asList(
            createBook(sourceClass, BigInteger.ONE, "Book 1"),
            createBook(sourceClass, BigInteger.valueOf(2), "Book 2")
        );

        Method toListMethod = findMethodOnImpl(implClass, "toList", iterableClass);
        Object result = toListMethod.invoke(mapper, sources);

        assertThat(result).isInstanceOf(listClass);
        List<?> resultList = (List<?>) result;
        assertThat(resultList).hasSize(2);
        assertThat(invokeGetter(resultList.get(0), "getId")).isEqualTo(BigInteger.ONE);
        assertThat(invokeGetter(resultList.get(1), "getId")).isEqualTo(BigInteger.valueOf(2));
    }

    @Test
    void shouldUseExplicitElementConverterMethod() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.collection.BookSource", getSourceCode("BookSource"))
            .addBean("glz.hawk.bean.mapper.integration.collection.BookTarget", getSourceCode("BookTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.collection.ElementConverterMapper", getSourceCode("ElementConverterMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.ElementConverterMapper");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        // Debug
        System.out.println("=== ElementConverterMapper Implementation methods ===");
        for (Method m : implClass.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookTarget");
        Class<?> listClass = Class.forName("java.util.List");

        List<Object> sources = Arrays.asList(
            createBook(sourceClass, BigInteger.ONE, "Book 1"),
            createBook(sourceClass, BigInteger.valueOf(2), "Book 2")
        );

        Method toListMethod = findMethodOnImpl(implClass, "toList", listClass);
        Object result = toListMethod.invoke(mapper, sources);

        assertThat(result).isInstanceOf(listClass);
        List<?> resultList = (List<?>) result;
        assertThat(resultList).hasSize(2);
        assertThat(invokeGetter(resultList.get(0), "getId")).isEqualTo(BigInteger.ONE);
        assertThat(invokeGetter(resultList.get(1), "getId")).isEqualTo(BigInteger.valueOf(2));
    }

    @Test
    void shouldUseExplicitElementConverterForArray() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.collection.BookSource", getSourceCode("BookSource"))
            .addBean("glz.hawk.bean.mapper.integration.collection.BookTarget", getSourceCode("BookTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.collection.ElementConverterMapper", getSourceCode("ElementConverterMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.ElementConverterMapper");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookTarget");
        Class<?> sourceArrayClass = Array.newInstance(sourceClass, 0).getClass();
        Class<?> targetArrayClass = Array.newInstance(targetClass, 0).getClass();

        // Create properly typed array using Array.newInstance
        Object sources = Array.newInstance(sourceClass, 2);
        Array.set(sources, 0, createBook(sourceClass, BigInteger.ONE, "Book 1"));
        Array.set(sources, 1, createBook(sourceClass, BigInteger.valueOf(2), "Book 2"));

        Method toArrayMethod = findMethodOnImpl(implClass, "toArray", sourceArrayClass);
        Object result = toArrayMethod.invoke(mapper, sources);

        assertThat(result).isInstanceOf(targetArrayClass);
        Object[] resultArray = (Object[]) result;
        assertThat(resultArray).hasSize(2);
        assertThat(invokeGetter(resultArray[0], "getId")).isEqualTo(BigInteger.ONE);
        assertThat(invokeGetter(resultArray[1], "getId")).isEqualTo(BigInteger.valueOf(2));
    }

    @Test
    void shouldHandleNullList() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.collection.BookSource", getSourceCode("BookSource"))
            .addBean("glz.hawk.bean.mapper.integration.collection.BookTarget", getSourceCode("BookTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.collection.ListToListMapperWithConverterNullable", getSourceCode("ListToListMapperWithConverterNullable"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.ListToListMapperWithConverterNullable");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookSource");
        Class<?> listClass = Class.forName("java.util.List");

        Method toListMethod = findMethodOnImpl(implClass, "toList", listClass);
        Object result = toListMethod.invoke(mapper, (Object) null);
        assertThat(result).isNull();
    }

    @Test
    void shouldHandleEmptyList() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.collection.BookSource", getSourceCode("BookSource"))
            .addBean("glz.hawk.bean.mapper.integration.collection.BookTarget", getSourceCode("BookTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.collection.ListToListMapperWithConverter", getSourceCode("ListToListMapperWithConverter"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.ListToListMapperWithConverter");
        Object mapper = scenario.getMapper(mapperInterface);
        Class<?> implClass = mapper.getClass();

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.collection.BookSource");
        Class<?> listClass = Class.forName("java.util.List");

        Method toListMethod = findMethodOnImpl(implClass, "toList", listClass);
        Object result = toListMethod.invoke(mapper, Collections.emptyList());
        assertThat(result).isInstanceOf(listClass);
        List<?> resultList = (List<?>) result;
        assertThat(resultList).isEmpty();
    }

    // ===== Helper methods =====

    private Method findMethodOnImpl(Class<?> implClass, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        for (Method m : implClass.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                Class<?>[] methodParamTypes = m.getParameterTypes();
                if (methodParamTypes.length == paramTypes.length) {
                    boolean match = true;
                    for (int i = 0; i < paramTypes.length; i++) {
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

    private Object createBook(Class<?> bookClass, BigInteger id, String title) throws Exception {
        Object book = bookClass.getDeclaredConstructor().newInstance();
        bookClass.getMethod("setId", BigInteger.class).invoke(book, id);
        bookClass.getMethod("setTitle", String.class).invoke(book, title);
        return book;
    }

    private Object invokeGetter(Object bean, String getterName) throws Exception {
        return bean.getClass().getMethod(getterName).invoke(bean);
    }

    // ===== Source code definitions =====

    private String getSourceCode(String typeName) {
        return switch (typeName) {
            case "BookSource" -> """
                package glz.hawk.bean.mapper.integration.collection;
                import java.math.BigInteger;
                public class BookSource {
                    private BigInteger id;
                    private String title;
                    public BigInteger getId() { return id; }
                    public void setId(BigInteger id) { this.id = id; }
                    public String getTitle() { return title; }
                    public void setTitle(String title) { this.title = title; }
                }
                """;
            case "BookTarget" -> """
                package glz.hawk.bean.mapper.integration.collection;
                import java.math.BigInteger;
                public class BookTarget {
                    private BigInteger id;
                    private String title;
                    public BigInteger getId() { return id; }
                    public void setId(BigInteger id) { this.id = id; }
                    public String getTitle() { return title; }
                    public void setTitle(String title) { this.title = title; }
                }
                """;
            // All collection mappers need @ElementConverter with a single-element conversion method
            case "ListToListMapperWithConverter" -> """
                package glz.hawk.bean.mapper.integration.collection;
                import glz.hawk.bean.mapper.annotation.*;
                import java.util.List;
                import static glz.hawk.bean.mapper.annotation.ConverterParamType.*;
                @BeanMapper
                public interface ListToListMapperWithConverter {
                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    BookTarget toSingle(BookSource source);

                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    List<BookTarget> toList(List<BookSource> sources);
                }
                """;
            case "ListToListMapperWithConverterNullable" -> """
                package glz.hawk.bean.mapper.integration.collection;
                import glz.hawk.bean.mapper.annotation.*;
                import java.util.List;
                import static glz.hawk.bean.mapper.annotation.ConverterParamType.*;
                @BeanMapper
                public interface ListToListMapperWithConverterNullable {
                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    BookTarget toSingle(BookSource source);

                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    List<BookTarget> toList(@MappingSource(nullable = true) List<BookSource> sources);
                }
                """;
            case "ArrayToListMapperWithConverter" -> """
                package glz.hawk.bean.mapper.integration.collection;
                import glz.hawk.bean.mapper.annotation.*;
                import java.util.List;
                import static glz.hawk.bean.mapper.annotation.ConverterParamType.*;
                @BeanMapper
                public interface ArrayToListMapperWithConverter {
                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    BookTarget toSingle(BookSource source);

                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    List<BookTarget> toList(BookSource[] sources);
                }
                """;
            case "ListToArrayMapperWithConverter" -> """
                package glz.hawk.bean.mapper.integration.collection;
                import glz.hawk.bean.mapper.annotation.*;
                import java.util.List;
                import static glz.hawk.bean.mapper.annotation.ConverterParamType.*;
                @BeanMapper
                public interface ListToArrayMapperWithConverter {
                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    BookTarget toSingle(BookSource source);

                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    BookTarget[] toArray(List<BookSource> sources);
                }
                """;
            case "ArrayToArrayMapperWithConverter" -> """
                package glz.hawk.bean.mapper.integration.collection;
                import glz.hawk.bean.mapper.annotation.*;
                import static glz.hawk.bean.mapper.annotation.ConverterParamType.*;
                @BeanMapper
                public interface ArrayToArrayMapperWithConverter {
                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    BookTarget toSingle(BookSource source);

                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    BookTarget[] toArray(BookSource[] sources);
                }
                """;
            case "IterableToListMapperWithConverter" -> """
                package glz.hawk.bean.mapper.integration.collection;
                import glz.hawk.bean.mapper.annotation.*;
                import java.util.List;
                import static glz.hawk.bean.mapper.annotation.ConverterParamType.*;
                @BeanMapper
                public interface IterableToListMapperWithConverter {
                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    BookTarget toSingle(BookSource source);

                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    List<BookTarget> toList(Iterable<BookSource> sources);
                }
                """;
            case "ElementConverterMapper" -> """
                package glz.hawk.bean.mapper.integration.collection;
                import glz.hawk.bean.mapper.annotation.*;
                import java.util.List;
                import static glz.hawk.bean.mapper.annotation.ConverterParamType.*;
                @BeanMapper
                public interface ElementConverterMapper {
                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    BookTarget toSingle(BookSource source);

                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    List<BookTarget> toList(List<BookSource> sources);

                    @ElementConverter(format = "toSingle($L)", params = @ConverterParam(type = SOURCE))
                    BookTarget[] toArray(BookSource[] sources);
                }
                """;
            default -> throw new IllegalArgumentException("Unknown type: " + typeName);
        };
    }
}