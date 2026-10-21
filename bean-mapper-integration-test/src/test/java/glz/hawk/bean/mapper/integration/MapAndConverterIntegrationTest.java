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
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Map mapping and custom converter scenarios.
 * All types are defined in source code strings and loaded via reflection to avoid classloader issues.
 */
class MapAndConverterIntegrationTest {

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
    void shouldConvertBeanToMap() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserSource", getSourceCode("UserSource"))
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserTarget", getSourceCode("UserTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.mapconverter.BeanToMapMapper", getSourceCode("BeanToMapMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.BeanToMapMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.UserSource");
        Class<?> mapClass = Class.forName("java.util.Map");

        Object source = createUserSource(sourceClass, 1L, "John", LocalDate.of(1990, 1, 1), 30);

        Object result = mapperInterface.getMethod("toMap", sourceClass).invoke(mapper, source);

        assertThat(result).isInstanceOf(mapClass);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertThat(resultMap.get("id")).isEqualTo(1L);
        assertThat(resultMap.get("name")).isEqualTo("John");
        assertThat(resultMap.get("birthDate")).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(resultMap.get("age")).isEqualTo(30);
    }

    @Test
    void shouldConvertMapToBean() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserMapSource", getSourceCode("UserMapSource"))
            .addMapper("glz.hawk.bean.mapper.integration.mapconverter.MapToBeanMapper", getSourceCode("MapToBeanMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.MapToBeanMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.UserMapSource");
        Class<?> mapClass = Class.forName("java.util.Map");

        Map<String, String> map = new HashMap<>();
        map.put("userId", "123");
        map.put("userName", "Jane");
        map.put("email", "jane@example.com");

        Object result = mapperInterface.getMethod("fromMap", mapClass).invoke(mapper, map);

        assertThat(result).isNotNull();
        assertThat(invokeGetter(result, "getUserId")).isEqualTo("123");
        assertThat(invokeGetter(result, "getUserName")).isEqualTo("Jane");
        assertThat(invokeGetter(result, "getEmail")).isEqualTo("jane@example.com");
    }

    @Test
    void shouldTransformMapToMap() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addMapper("glz.hawk.bean.mapper.integration.mapconverter.MapToMapMapper", getSourceCode("MapToMapMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.MapToMapMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> mapClass = Class.forName("java.util.Map");

        Map<String, String> source = new HashMap<>();
        source.put("key1", "value1");
        source.put("key2", "value2");

        Object result = mapperInterface.getMethod("transform", mapClass).invoke(mapper, source);

        assertThat(result).isInstanceOf(mapClass);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertThat(resultMap.get("key1")).isEqualTo("value1");
        assertThat(resultMap.get("key2")).isEqualTo("value2");
    }

    @Test
    void shouldUseCustomConverterForDateFormatting() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserSource", getSourceCode("UserSource"))
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserTarget", getSourceCode("UserTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.mapconverter.ConverterMapper", getSourceCode("ConverterMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.ConverterMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.UserSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.UserTarget");

        Object source = createUserSource(sourceClass, 1L, "Test", LocalDate.of(2024, 6, 15), null);

        Object result = mapperInterface.getMethod("toTarget", sourceClass).invoke(mapper, source);

        assertThat(result).isNotNull();
        assertThat(invokeGetter(result, "getBirthDateStr")).isEqualTo("2024-06-15");
    }

    @Test
    void shouldUseCustomConverterForAgeToString() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserSource", getSourceCode("UserSource"))
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserTarget", getSourceCode("UserTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.mapconverter.ConverterMapper", getSourceCode("ConverterMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.ConverterMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.UserSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.UserTarget");

        Object source = createUserSource(sourceClass, null, null, null, 25);

        Object result = mapperInterface.getMethod("toTargetWithAgeConverter", sourceClass).invoke(mapper, source);

        assertThat(result).isNotNull();
        assertThat(invokeGetter(result, "getAgeStr")).isEqualTo("25");
    }

    @Test
    void shouldRenamePropertyWithPropertyMapping() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserSource", getSourceCode("UserSource"))
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserMapSource", getSourceCode("UserMapSource"))
            .addMapper("glz.hawk.bean.mapper.integration.mapconverter.PropertyMappingMapper", getSourceCode("PropertyMappingMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.PropertyMappingMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.UserSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.UserMapSource");

        Object source = createUserSource(sourceClass, 1L, "Test Name", null, null);

        Object result = mapperInterface.getMethod("mapWithRenameAndIgnore", sourceClass).invoke(mapper, source);

        assertThat(result).isNotNull();
        // id -> userId (renamed, with Long to String converter)
        assertThat(invokeGetter(result, "getUserId")).isEqualTo("1");
        // name is ignored (UserMapSource has userName, not name)
        assertThat(invokeGetter(result, "getUserName")).isNull();
    }

    @Test
    void shouldIgnoreTargetProperty() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserSource", getSourceCode("UserSource"))
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserMapSource", getSourceCode("UserMapSource"))
            .addMapper("glz.hawk.bean.mapper.integration.mapconverter.PropertyMappingMapper", getSourceCode("PropertyMappingMapper"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.PropertyMappingMapper");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.UserSource");
        Class<?> targetClass = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.UserMapSource");

        Object source = createUserSource(sourceClass, 1L, "Test", null, null);

        Object result = mapperInterface.getMethod("mapWithIgnoreTarget", sourceClass).invoke(mapper, source);

        assertThat(result).isNotNull();
        assertThat(invokeGetter(result, "getUserId")).isEqualTo("1");
        assertThat(invokeGetter(result, "getUserName")).isEqualTo("Test");
        // email is ignored
        assertThat(invokeGetter(result, "getEmail")).isNull();
    }

    @Test
    void shouldHandleNullMap() throws Exception {
        IntegrationTestCompiler.CompiledScenario scenario = compiler.scenario()
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserSource", getSourceCode("UserSource"))
            .addBean("glz.hawk.bean.mapper.integration.mapconverter.UserTarget", getSourceCode("UserTarget"))
            .addMapper("glz.hawk.bean.mapper.integration.mapconverter.BeanToMapMapperNullable", getSourceCode("BeanToMapMapperNullable"))
            .build();

        Class<?> mapperInterface = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.BeanToMapMapperNullable");
        Object mapper = scenario.getMapper(mapperInterface);

        Class<?> sourceClass = scenario.loadClass("glz.hawk.bean.mapper.integration.mapconverter.UserSource");
        Class<?> mapClass = Class.forName("java.util.Map");

        // The generated code should return null for null source when nullable=true
        Object result = mapperInterface.getMethod("toMap", sourceClass).invoke(mapper, (Object) null);
        assertThat(result).isNull();
    }

    // ===== Helper methods =====

    private Object createUserSource(Class<?> sourceClass, Long id, String name, LocalDate birthDate, Integer age) throws Exception {
        Object source = sourceClass.getDeclaredConstructor().newInstance();
        if (id != null) sourceClass.getMethod("setId", Long.class).invoke(source, id);
        if (name != null) sourceClass.getMethod("setName", String.class).invoke(source, name);
        if (birthDate != null) sourceClass.getMethod("setBirthDate", LocalDate.class).invoke(source, birthDate);
        if (age != null) sourceClass.getMethod("setAge", Integer.class).invoke(source, age);
        return source;
    }

    private Object invokeGetter(Object bean, String getterName) throws Exception {
        return bean.getClass().getMethod(getterName).invoke(bean);
    }

    // ===== Source code definitions =====

    private String getSourceCode(String typeName) {
        return switch (typeName) {
            case "UserSource" -> """
                package glz.hawk.bean.mapper.integration.mapconverter;
                import java.time.LocalDate;
                public class UserSource {
                    private Long id;
                    private String name;
                    private LocalDate birthDate;
                    private Integer age;
                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                    public LocalDate getBirthDate() { return birthDate; }
                    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
                    public Integer getAge() { return age; }
                    public void setAge(Integer age) { this.age = age; }
                }
                """;
            case "UserTarget" -> """
                package glz.hawk.bean.mapper.integration.mapconverter;
                public class UserTarget {
                    private Long id;
                    private String name;
                    private String birthDateStr;
                    private String ageStr;
                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                    public String getBirthDateStr() { return birthDateStr; }
                    public void setBirthDateStr(String birthDateStr) { this.birthDateStr = birthDateStr; }
                    public String getAgeStr() { return ageStr; }
                    public void setAgeStr(String ageStr) { this.ageStr = ageStr; }
                }
                """;
            case "UserMapSource" -> """
                package glz.hawk.bean.mapper.integration.mapconverter;
                public class UserMapSource {
                    private String userId;
                    private String userName;
                    private String email;
                    public String getUserId() { return userId; }
                    public void setUserId(String userId) { this.userId = userId; }
                    public String getUserName() { return userName; }
                    public void setUserName(String userName) { this.userName = userName; }
                    public String getEmail() { return email; }
                    public void setEmail(String email) { this.email = email; }
                }
                """;
            case "BeanToMapMapper" -> """
                package glz.hawk.bean.mapper.integration.mapconverter;
                import glz.hawk.bean.mapper.annotation.BeanMapper;
                import java.util.Map;
                @BeanMapper
                public interface BeanToMapMapper {
                    Map<String, Object> toMap(UserSource source);
                }
                """;
            case "BeanToMapMapperNullable" -> """
                package glz.hawk.bean.mapper.integration.mapconverter;
                import glz.hawk.bean.mapper.annotation.*;
                import java.util.Map;
                @BeanMapper
                public interface BeanToMapMapperNullable {
                    Map<String, Object> toMap(@MappingSource(nullable = true) UserSource source);
                }
                """;
            case "MapToBeanMapper" -> """
                package glz.hawk.bean.mapper.integration.mapconverter;
                import glz.hawk.bean.mapper.annotation.BeanMapper;
                import java.util.Map;
                @BeanMapper
                public interface MapToBeanMapper {
                    UserMapSource fromMap(Map<String, String> map);
                }
                """;
            case "MapToMapMapper" -> """
                package glz.hawk.bean.mapper.integration.mapconverter;
                import glz.hawk.bean.mapper.annotation.BeanMapper;
                import java.util.Map;
                @BeanMapper
                public interface MapToMapMapper {
                    Map<String, String> transform(Map<String, String> map);
                }
                """;
            case "ConverterMapper" -> """
                package glz.hawk.bean.mapper.integration.mapconverter;
                import glz.hawk.bean.mapper.annotation.*;
                import java.time.format.DateTimeFormatter;
                import static glz.hawk.bean.mapper.annotation.ConverterParamType.*;
                @BeanMapper
                public interface ConverterMapper {
                    @PropertyMapping(source = "birthDate", target = "birthDateStr",
                        converter = @Converter(format = "$L.format($T.ofPattern($S))",
                            params = {
                                @ConverterParam(type = SOURCE),
                                @ConverterParam(type = TYPE, value = "java.time.format.DateTimeFormatter"),
                                @ConverterParam(type = LITERAL, value = "yyyy-MM-dd")
                            }
                        )
                    )
                    UserTarget toTarget(UserSource source);

                    @PropertyMapping(source = "age", target = "ageStr",
                        converter = @Converter(format = "String.valueOf($L)",
                            params = @ConverterParam(type = SOURCE)
                        )
                    )
                    UserTarget toTargetWithAgeConverter(UserSource source);
                }
                """;
            case "PropertyMappingMapper" -> """
                package glz.hawk.bean.mapper.integration.mapconverter;
                import glz.hawk.bean.mapper.annotation.*;
                import static glz.hawk.bean.mapper.annotation.ConverterParamType.*;
                @BeanMapper
                public interface PropertyMappingMapper {
                    @PropertyMapping(source = "id", target = "userId",
                        converter = @Converter(format = "String.valueOf($L)",
                            params = @ConverterParam(type = SOURCE)
                        )
                    )
                    @PropertyMapping(source = "name", target = "userName", ignore = true)
                    UserMapSource mapWithRenameAndIgnore(UserSource source);

                    @PropertyMapping(source = "id", target = "userId",
                        converter = @Converter(format = "String.valueOf($L)",
                            params = @ConverterParam(type = SOURCE)
                        )
                    )
                    @PropertyMapping(source = "name", target = "userName")
                    @PropertyMapping(target = "email", ignore = true)
                    UserMapSource mapWithIgnoreTarget(UserSource source);
                }
                """;
            default -> throw new IllegalArgumentException("Unknown type: " + typeName);
        };
    }
}