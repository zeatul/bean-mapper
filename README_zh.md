## Choose your favorite language / 选择你喜欢的语言

[English](README.md) | [中文](README_zh.md)

---

<a name="english"></a>
# bean-mapper

`bean-mapper` is a compile-time bean mapping code generator based on Java Annotation Processing, similar to MapStruct. It generates mapping implementation classes from annotated mapper interfaces at compile time, eliminating runtime runtime reflection overhead.

## Dependency

### Gradle

```groovy
dependencies {
    implementation 'io.github.zeatul:bean-mapper-annotations:<version>'
    annotationProcessor 'io.github.zeatul:bean-mapper-processor:<version>'
}
```

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>io.github.zeatul</groupId>
        <artifactId>bean-mapper-annotations</artifactId>
        <version>${version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>io.github.zeatul</groupId>
                        <artifactId>bean-mapper-processor</artifactId>
                        <version>${version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Quick Start

Define a mapper interface annotated with `@BeanMapper`:

```java
@BeanMapper(componentModel = ComponentModel.SPRING)
public interface UserMapper {
    UserDTO toDTO(UserEntity entity);
    
    UserEntity toEntity(UserDTO dto);
    
    void copy(UserEntity source, @MappingTarget UserEntity target);
}
```

The processor generates `UserMapperImpl` implementing the interface. Use it via dependency injection (Spring) or direct instantiation:

```java
@Autowired
private UserMapper userMapper;

// or
UserMapper mapper = new UserMapperImpl();
```

## Annotations

### @BeanMapper

Marks an interface as a bean mapper. The processor generates an implementation class named `<InterfaceName>Impl` in the `<package>.impl` package.

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `componentModel` | `ComponentModel` | `DEFAULT` | DI framework integration: `DEFAULT`, `SPRING`, `JAKARTA_CDI` |
| `autowiredBeans` | `AutowiredBean[]` | `{}` | Beans to inject into the generated implementation |
| `staticImports` | `String[]` | `{}` | Static imports to add to the generated class |

### @MappingTarget

Marks a method parameter as the mapping target (output object). **Must be placed on a parameter, not on the method.** (Since v1.1, `@Target` only includes `ElementType.PARAMETER`; method-level usage was previously silently ignored and is now a compile error.)

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `includeAll` | `boolean` | `true` | Whether to map all readable properties by default |
| `nullable` | `boolean` | `false` | Whether the target parameter can be null |

### @MappingSource

Marks a method parameter as a mapping source (input object).

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `includeAll` | `boolean` | `true` | Whether to map all writable properties by default |
| `nullable` | `boolean` | `false` | Whether the source parameter can be null |

### @PropertyMapping

Defines a custom property mapping rule between source and target.

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `source` | `String` | `""` | Source property path (e.g., `"user.name"` or `"paramName.property"`) |
| `target` | `String` | `""` | Target property name (single property, no path) |
| `ignore` | `boolean` | `false` | Whether to ignore this mapping |
| `converter` | `Converter` | `@Converter` | Custom conversion logic |

At least one of `source` or `target` must be specified.

### @Converter / @ElementConverter

Provides custom conversion code for property or element mapping.

```java
@PropertyMapping(source = "localDate", target = "localDateStr",
    converter = @Converter(format = "$L.format($T.ofPattern($S))",
        params = {@ConverterParam(type = ConverterParamType.SOURCE),
                  @ConverterParam(type = ConverterParamType.TYPE, value = "java.time.format.DateTimeFormatter"),
                  @ConverterParam(type = ConverterParamType.LITERAL, value = "yyyy-MM-dd")}))
```

### @AutowiredBean

Declares a bean to be injected into the generated implementation.

```java
@BeanMapper(
    componentModel = ComponentModel.SPRING,
    autowiredBeans = {
        @AutowiredBean(type = "com.example.UserMapper", qualifier = {"userMapper2"}, fieldName = "userMapper2")
    }
)
public interface OrderMapper {
    OrderDTO toDTO(OrderEntity entity);
}
```

The generated class will have a field `private UserMapper userMapper2;` annotated with `@Autowired`/`@Named("userMapper2")`.

### @ElementConverter

Specifies element-level conversion for collection/array mappings.

```java
@ElementConverter(format = "convert1($L)", params = @ConverterParam(type = ConverterParamType.SOURCE))
List<OrderDTO> toDTOs(List<OrderEntity> entities);
```

## Component Models

| Model | Generated Annotations |
|-------|----------------------|
| `DEFAULT` | None (plain instantiation) |
| `SPRING` | `@Component`, `@Autowired`, `@Qualifier` (if specified) |
| `JAKARTA_CDI` | `@ApplicationScoped`, `@Inject`, `@Named` |

## Supported Mapping Scenarios

| Scenario | Example |
|----------|---------|
| Bean ↔ Bean | `DTO toDTO(Entity entity)` |
| Bean → Bean (existing target) | `void copy(Entity source, @MappingTarget DTO target)` |
| Array → List | `List<DTO> toDTOs(Entity[] entities)` |
| List → Array | `DTO[] toDTOs(List<Entity> entities)` |
| List → List | `List<DTO> toDTOs(List<Entity> entities)` |
| Map → Bean | `Bean fromMap(Map<String, Object> map)` |
| Bean → Map | `Map<String, Object> toMap(Bean bean)` |
| Map → Map | `Map<String, String> transform(Map<String, String> map)` |
| Custom converter | See `@Converter` above |
| Element converter | See `@ElementConverter` above |

## Generic Mapper Interfaces

Since v1.1, generic mapper interfaces are supported. Type parameters are propagated to the generated implementation:

```java
public interface BaseMapper<S, T> {
    T convert(S source);
    void copy(S source, @MappingTarget T target);
}

@BeanMapper
public interface UserMapper extends BaseMapper<UserEntity, UserDTO> {
    // UserMapperImpl implements UserMapper, BaseMapper<UserEntity, UserDTO>
    // Methods from BaseMapper have S→UserEntity, T→UserDTO substituted
}
```

The generated `UserMapperImpl` declares the same type parameters and implements the parameterized superinterface.

## Interface Inheritance

Mapper interfaces can extend other interfaces. All non-static, non-default methods from the entire hierarchy are processed with type parameter substitution:

```java
public interface AuditableMapper<S, T> {
    T create(S source);
    void update(S source, @MappingTarget T target);
}

@BeanMapper
public interface OrderMapper extends AuditableMapper<OrderEntity, OrderDTO> {
    // Generates create(OrderEntity) → OrderDTO
    // Generates update(OrderEntity, @MappingTarget OrderDTO) → void
}
```

## Breaking Changes (v1.1+)

| Change | Migration |
|--------|-----------|
| `@MappingTarget` can only be on parameters (not methods) | Move `@MappingTarget` from method to the target parameter |
| Array/iterable mismatch is now ERROR (was WARNING) | Fix mismatched source/target types |
| Generated code uses `String.replace` for converter formats | Converter format strings with `%` no longer cause `IllegalFormatException` |

## Generated Code Location

By default, implementations are generated to:

```
build/generated/sources/annotationProcessor/java/main/<package>.impl/<InterfaceName>Impl.java
```

## Configuration Options

| Option | Description |
|--------|-------------|
| `componentModel` | DI framework: `DEFAULT`, `SPRING`, `JAKARTA_CDI` |
| `autowiredBeans` | Additional beans to inject |
| `staticImports` | Static imports for the generated class |

## Requirements

- Java 8+ (uses JDK 8 toolchain for compilation)
- Annotation processing enabled in build tool

## Limitations

- Cascade/nested property mapping (e.g., `user.address.city`) not yet supported
- `includeAll=false` only works for bean→bean mappings, not for Map conversions
- Injected beans don't generate getter methods (planned)

---

## FAQ / 常见问题

### Q1: 编译报错 "The parameter cannot be null" / 编译报错 "The parameter cannot be null"

**原因**: 生成的代码会对非空参数进行校验。 / **Cause**: Generated code validates non-null parameters.

**解决**: 使用 `@MappingSource(nullable = true)` 或 `@MappingTarget(nullable = true)` 标记可为空的参数。 / **Solution**: Use `@MappingSource(nullable = true)` or `@MappingTarget(nullable = true)` to mark nullable parameters.

```java
@BeanMapper
public interface UserMapper {
    // 源参数可为空，返回 null / Source param can be null, returns null
    UserDTO toDTO(@MappingSource(nullable = true) UserEntity entity);

    // 目标参数可为空，直接返回 / Target param can be null, returns early
    void copy(@MappingSource(nullable = true) UserEntity source,
              @MappingTarget(nullable = true) UserEntity target);
}
```

### Q2: 如何忽略某些属性不映射？ / How to ignore certain properties?

**方法 1**: 使用 `@PropertyMapping(ignore = true)` / **Method 1**: Use `@PropertyMapping(ignore = true)`

```java
@BeanMapper
public interface UserMapper {
    @PropertyMapping(target = "password", ignore = true)
    UserDTO toDTO(UserEntity entity);
}
```

**方法 2**: 使用 `includeAll = false` 仅映射显式指定的属性 / **Method 2**: Use `includeAll = false` to map only explicitly specified properties

```java
@BeanMapper
public interface UserMapper {
    @PropertyMapping(source = "name", target = "fullName")
    UserDTO toDTO(@MappingSource(includeAll = false) UserEntity entity);
}
```

### Q3: 属性类型不兼容如何转换？ / How to convert incompatible property types?

使用 `@PropertyMapping` 的 `converter` 属性：/ Use `@PropertyMapping`'s `converter` attribute:

```java
@BeanMapper
public interface UserMapper {
    @PropertyMapping(source = "birthDate", target = "birthDateStr",
        converter = @Converter(format = "$L.format($T.ofPattern($S))",
            params = {
                @ConverterParam(type = ConverterParamType.SOURCE),
                @ConverterParam(type = ConverterParamType.TYPE, value = "java.time.format.DateTimeFormatter"),
                @ConverterParam(type = ConverterParamType.LITERAL, value = "yyyy-MM-dd")
            }))
    UserDTO toDTO(UserEntity entity);
}
```

**Converter 格式说明 / Converter format guide**:
- `$L` - 源值 / Source value
- `$T` - 类型引用（自动添加 import） / Type reference (auto import)
- `$S` - 字面量字符串 / String literal
- `%s` 占位符会被自动替换，避免 `IllegalFormatException` / `%s` placeholders auto-replaced to avoid `IllegalFormatException`

### Q4: 集合/数组映射需要什么配置？ / What configuration for collection/array mapping?

**必须**在方法上添加 `@ElementConverter` 指定元素转换逻辑：/ **Must** add `@ElementConverter` on method to specify element conversion:

```java
@BeanMapper
@ElementConverter(format = "toDTO($L)", params = @ConverterParam(type = ConverterParamType.SOURCE))
public interface OrderMapper {
    OrderDTO toDTO(OrderEntity entity);
    List<OrderDTO> toDTOs(List<OrderEntity> entities);
    OrderDTO[] toDTOs(OrderEntity[] entities);
}
```

> **注意**: 所有集合/数组映射方法都需要 `@ElementConverter`，否则编译报错。 / **Note**: All collection/array mapping methods require `@ElementConverter`, otherwise compile error.

### Q5: 如何使用 Spring/CDI 组件模型？ / How to use Spring/CDI component models?

```java
// Spring
@BeanMapper(componentModel = ComponentModel.SPRING)
public interface UserMapper { ... }

// CDI (Jakarta)
@BeanMapper(componentModel = ComponentModel.JAKARTA_CDI)
public interface UserMapper { ... }

// JSR330 (@Inject)
@BeanMapper(componentModel = ComponentModel.JSR330)
public interface UserMapper { ... }
```

### Q6: 如何注入其他 Mapper/Bean？ / How to inject other Mappers/Beans?

```java
@BeanMapper(componentModel = ComponentModel.SPRING,
    autowiredBeans = {
        @AutowiredBean(type = "com.example.OrderMapper", qualifier = {"orderMapper"}, fieldName = "orderMapper"),
        @AutowiredBean(type = "com.example.ProductService", fieldName = "productService")
    })
public interface OrderMapper {
    OrderDTO toDTO(OrderEntity entity);
}
```

生成的代码会包含：/ Generated code will contain:
```java
@Autowired
@Qualifier("orderMapper")
private OrderMapper orderMapper;

@Autowired
private ProductService productService;
```

> **限制**: 同一字段只能有一个 `@Qualifier`（Spring 限制）。 / **Limitation**: One field can only have one `@Qualifier` (Spring limitation).

### Q7: 泛型 Mapper 接口如何使用？ / How to use generic Mapper interfaces?

```java
public interface BaseMapper<S, T> {
    T convert(S source);
    void copy(S source, @MappingTarget T target);
}

@BeanMapper
public interface UserMapper extends BaseMapper<UserEntity, UserDTO> {
    // 自动实现 convert(UserEntity) -> UserDTO
    // 自动实现 copy(UserEntity, @MappingTarget UserDTO)
}
```

### Q8: 接口继承如何工作？ / How does interface inheritance work?

```java
public interface AuditableMapper<S, T> {
    T create(S source);
    void update(S source, @MappingTarget T target);
}

@BeanMapper
public interface OrderMapper extends AuditableMapper<OrderEntity, OrderDTO> {
    // 自动实现 create(OrderEntity) -> OrderDTO
    // 自动实现 update(OrderEntity, @MappingTarget OrderDTO) -> void
}
```

---

## 最佳实践 / Best Practices

### 1. 明确指定映射方向 / Explicitly specify mapping direction

```java
// 推荐：明确指定 source/target / Recommended: Explicitly specify source/target
@PropertyMapping(source = "userName", target = "name")
UserDTO toDTO(UserEntity entity);

// 不推荐：依赖同名自动映射（重构时易出错） / Not recommended: Rely on auto-mapping by same name (error-prone during refactoring)
UserDTO toDTO(UserEntity entity);
```

### 2. 合理使用 `includeAll = false` / Use `includeAll = false` wisely

```java
// 场景：仅映射核心字段，忽略大量非必要字段 / Scenario: Map only core fields, ignore many unnecessary fields
@BeanMapper
public interface OrderSummaryMapper {
    @PropertyMapping(source = "id", target = "orderId")
    @PropertyMapping(source = "totalAmount", target = "amount")
    OrderSummary toSummary(@MappingSource(includeAll = false) Order order);
}
```

### 3. 复杂类型转换提取为静态方法 / Extract complex conversions to static methods

```java
public class Converters {
    public static String localDateToString(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
    }
}

@BeanMapper(staticImports = "com.example.Converters.localDateToString")
public interface UserMapper {
    @PropertyMapping(source = "birthDate", target = "birthDateStr",
        converter = @Converter(format = "localDateToString($L)",
            params = @ConverterParam(type = ConverterParamType.SOURCE)))
    UserDTO toDTO(UserEntity entity);
}
```

### 4. 集合映射优先使用流式转换 / Prefer stream-style conversion for collections

```java
// 推荐：使用 @ElementConverter 引用单元素转换方法 / Recommended: Use @ElementConverter referencing single-element conversion
@ElementConverter(format = "toDTO($L)", params = @ConverterParam(type = ConverterParamType.SOURCE))
List<OrderDTO> toDTOs(List<OrderEntity> entities);
```

### 5. 避免在实体类中使用复杂泛型 / Avoid complex generics in entity classes

```java
// 不推荐：嵌套泛型难以映射 / Not recommended: Nested generics hard to map
class Order { List<Map<String, List<Item>>> items; }

// 推荐：扁平化设计 / Recommended: Flattened design
class Order { List<OrderItem> items; }
class OrderItem { String sku; int quantity; }
```

---

## 性能优化 / Performance Optimization

### 1. 编译期生成 = 零运行时反射 / Compile-time generation = Zero runtime reflection

Bean-Mapper 在编译期生成实现类，运行时无反射开销，性能接近手写代码。 / Bean-Mapper generates implementation at compile-time, zero runtime reflection overhead, performance close to hand-written code.

### 2. 避免不必要的对象创建 / Avoid unnecessary object creation

```java
// 推荐：复用目标对象 / Recommended: Reuse target object
void copy(UserEntity source, @MappingTarget UserDTO target);

// 避免：每次创建新对象（如非必要） / Avoid: Creating new object each time (unless necessary)
UserDTO toDTO(UserEntity source);
```

### 3. 大批量映射考虑分批处理 / Consider batching for large volume mapping

```java
// 大列表分批映射，避免内存压力 / Large list batch mapping to avoid memory pressure
List<UserDTO> toDTOs(List<UserEntity> entities); // 生成的代码已优化循环 / Generated code optimizes loops
```

---

## 迁移指南 / Migration Guide

### 从 MapStruct 迁移 / Migrating from MapStruct

| MapStruct | Bean-Mapper |
|-----------|-------------|
| `@Mapper` | `@BeanMapper` |
| `@Mapping(source = "x", target = "y")` | `@PropertyMapping(source = "x", target = "y")` |
| `@Mapping(target = "x", ignore = true)` | `@PropertyMapping(target = "x", ignore = true)` |
| `@Mapper(componentModel = "spring")` | `@BeanMapper(componentModel = ComponentModel.SPRING)` |
| `uses = MyConverters.class` | `staticImports = "com.example.Converters.*"` |
| `@IterableMapping` | `@ElementConverter` (在方法上 / on method) |
| `injectionStrategy = CONSTRUCTOR` | 不支持（使用字段注入） / Not supported (uses field injection) |

**注意差异 / Key differences**:
1. Bean-Mapper 要求集合映射方法必须有 `@ElementConverter` / Bean-Mapper requires `@ElementConverter` for collection mapping
2. Bean-Mapper 不支持 `@AfterMapping` 等生命周期钩子 / Bean-Mapper doesn't support `@AfterMapping` lifecycle hooks
3. Bean-Mapper 的 `qualifiedByName` 对应静态导入 + 方法引用 / Bean-Mapper's `qualifiedByName` maps to static imports + method references

### 从手写映射代码迁移 / Migrating from hand-written mapping code

1. 定义接口，添加 `@BeanMapper` / Define interface, add `@BeanMapper`
2. 逐个方法对照原手写逻辑添加 `@PropertyMapping` / `@Converter` / Add `@PropertyMapping` / `@Converter` per method matching original logic
3. 运行编译生成实现类 / Compile to generate implementation
4. 替换原手写实现为生成的实现类 / Replace hand-written impl with generated impl
5. 运行测试验证 / Run tests to verify

---

## 故障排查 / Troubleshooting

### 编译错误 / Compile Errors

| 错误信息 / Error Message | 原因 / Cause | 解决 / Solution |
|--------------------------|--------------|-----------------|
| "The converter method used between datasets... isn't defined!" | 缺少 `@ElementConverter` / Missing `@ElementConverter` | 在方法上添加 `@ElementConverter` / Add `@ElementConverter` on method |
| "illegal target... must be like [propertyName]" | target 包含点号 / target contains dot | target 必须是单个属性名 / target must be single property name |
| "duplicated target... was found" | 同一属性被多次映射 / Same property mapped multiple times | 检查 `@PropertyMapping` 是否重复 / Check for duplicate `@PropertyMapping` |
| "can't find the concrete type for the typeMirror" | 接口/抽象类无法实例化 / Interface/abstract class cannot be instantiated | 确保返回类型是具体类，或指定实现类 / Ensure return type is concrete class, or specify implementation |

### 运行时异常 / Runtime Exceptions

| 异常 / Exception | 原因 / Cause | 解决 / Solution |
|------------------|--------------|-----------------|
| `IllegalArgumentException: parameter can't be null` | 传入了 null 但未标记 nullable / Passed null but not marked nullable | 添加 `@MappingSource(nullable = true)` / Add `@MappingSource(nullable = true)` |
| `NullPointerException` | 源对象属性为空且无空值处理 / Source property null without handling | 在 Converter 中处理 null，或使用 `@MappingSource(nullable = true)` / Handle null in Converter, or use `@MappingSource(nullable = true)` |
| `ClassCastException` | 类型转换失败 / Type conversion failed | 检查 Converter 逻辑，确保类型兼容 / Check Converter logic, ensure type compatibility |

### 调试技巧 / Debugging Tips

1. **查看生成的代码** / **View generated code**: `build/generated/sources/annotationProcessor/java/main/<package>.impl/<Mapper>Impl.java`
2. **启用详细编译日志** / **Enable verbose compile log**: `./gradlew compileJava --info`
3. **增量编译问题** / **Incremental compilation issues**: `./gradlew clean compileJava` 强制重新生成 / Force regeneration

---

## 版本兼容性 / Version Compatibility

| Bean-Mapper | Java | Spring | Gradle |
|-------------|------|--------|--------|
| 1.x | 8+ | 5.x, 6.x | 7.x, 8.x |

> **注意**: Java 8 是最低要求，推荐使用 Java 17+ 以获得最佳编译性能。 / **Note**: Java 8 minimum, recommend Java 17+ for best compile performance.

---

## 贡献指南 / Contributing

1. Fork 项目 / Fork project
2. 创建特性分支 / Create feature branch
3. 编写测试用例（集成测试优先） / Write tests (integration tests preferred)
4. 确保所有测试通过: `./gradlew test` / Ensure all tests pass
5. 提交 PR / Submit PR

---

## 许可证 / License

Apache License 2.0

---

*最后更新: 2026-10-16 | Bean-Mapper v1.x* / *Last updated: 2026-10-16 | Bean-Mapper v1.x*