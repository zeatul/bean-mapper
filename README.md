## Choose your favorite language

[English](#) | [中文](README_zh.md)

---

`hawk-java-generator` is a Java API for generating `.java` source files.

Source code generation is highly effective for annotation processors and metadata-driven scenarios,
especially in contexts involving enterprise-grade reference data, interface specifications, and data models.
With code generation, you can generate qualified code on demand by maintaining just the metadata.
Combined with the [Java Annotation Processing API](https://),
it significantly enhances a project's readability, standardization, and extensibility.

This repo is forked from the excellent [square/javapoet](https://github.com/square/javapoet) project which
is [no longer actively maintained](https://github.com/square/javapoet/discussions/866).\
It's a better `javapoet`:

- Optimized the performance of Java code generation.
- Automatically resolves collisions in imports and static imports，
- Offers a more intuitive way to write control flow.
- Provides an enhanced and streamlined approach to writing Javadocs.
- Introduces lambda expressions to complement the fluent API.

## Dependency

Gradle:

```groovy
implementation 'io.github.zeatul:hawk-java-generator:<version>'
implementation 'io.github.zeatul:hawk-core::<version>'
```

Maven:

```xml

<dependency>
    <groupId>io.github.zeatul</groupId>
    <artifactId>hawk-java-generator</artifactId>
    <version>$version$</version>
</dependency>
```

## Hello World

Here is a common `HelloWorld` class:

```java
public class HelloWorld {
    public static void main(String... args) {
        System.out.println("Hello,Hawk Java Generator.");
    }
}
```

And this is a `HelloWorldGenerator` class that uses `hawk-java-generator` to generate the aforementioned `HelloWorld`
class:

```java
import glz.hawk.codepoet.java.ClassSpec;
import glz.hawk.codepoet.java.JavaFile;
import glz.hawk.codepoet.java.MethodSpec;
import type.glz.hawk.poet.java.ArrayTypeName;

import javax.lang.model.element.Modifier;

import static type.glz.hawk.poet.java.VoidTypeName.VOID;

public class HelloWorldGenerator {

    public static void main(String[] args) {

        ClassSpec classSpec = ClassSpec.builder("HelloWorld", Modifier.PUBLIC)
            .addMethod(MethodSpec.builder(VOID, "main", Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ArrayTypeName.ofClass(String.class), "args")
                .varargs()
                .beginMethodBody()
                .addStatement("$T.out.println($S)", System.class, "Hello,Hawk Java Generator!")
                .end()
                .build())
            .build();

        JavaFile javaFile = JavaFile.builder("com.example", classSpec)
            .build();

        javaFile.writeTo(System.out);
    }
}
```

Declare a "main" method using `MethodSpec`, including the method name,variable parameters (varargs), return type,
modifiers, and method body.
Declare a `HelloWorld` class using `ClassSpec`, add the "main" method to this class,
and finally output the class to the console via JavaFile (it can also be written to a file).

## Code & Control Flow

The API of `hawk-java-generator` predominantly employs immutable Java objects. It also incorporates the builder pattern,
method chaining, varargs, and lambda expressions to ensure user-friendly API design.
It provides corresponding models for classes (`ClassSpec`), interfaces (`InterfaceSpec`), enums (`EnumSpec`),
annotations (`AnnotationSpec`), fields (`FieldSpec`), methods (`MethodSpec`), constructors (`ConstructorSpec`),
parameters (`ParameterSpec`), annotation instances (`AnnotationInstanceSpec`), and hierarchical Javadoc
models (`FileJavaDoc`, `TypeJavaDoc`, `FieldJavaDoc`, `MethodJavaDoc`, `ConstructorJavaDoc`).

For method bodies, constructor bodies, and Javadoc content, hawk-java-generator provides format strings,
indentation, line break APIs, and control flow models to assist in generating well-structured code.
You'll find its syntax is nearly identical to writing Java code directly.

### for loop

#### Take full control of the code formatting

This is an example of complete control over code formatting.

```java
MethodSpec sum = MethodSpec.builder(PrimitiveTypeName.INT, "sum")
    .addParameter(ArrayTypeName.ofTypeName(ArrayTypeName.ofTypeName(PrimitiveTypeName.INT)), "a")
    .beginMethodBody()
    .addCode("int sum = 0;").addNewLine()
    .addCode("for (int i = 0; i < a.length; i++){").addNewLine()
    .addIndent()
    .addCode("for (int j = 0; j< a[i].length; j++){").addNewLine()
    .addIndent()
    .addCode("sum = sum + a[i][j];").addNewLine()
    .removeIndent()
    .addCode("}").addNewLine()
    .removeIndent()
    .addCode("}").addNewLine()
    .addCode("return sum;").addNewLine()
    .end()
    .build();
```

And this is the generated output.

```java
int sum(int[][] a) {
    int sum = 0;
    for (int i = 0; i < a.length; i++) {
        for (int j = 0; j < a[i].length; j++) {
            sum = sum + a[i][j];
        }
    }
    return sum;
}
```

Use the `addCode` method to add actual code blocks (semicolons must be manually included),
the `addIndent` and `removeIndent` methods to control indentation levels,
and the `addNewLine` method to insert line breaks.

#### Code formatting is controlled by `hawk-java-generator`

Here is an example demonstrating how code formatting is controlled by `hawk-java-generator`

```java
MethodSpec sum = MethodSpec.builder(PrimitiveTypeName.INT, "sum")
    .addParameter(ArrayTypeName.ofTypeName(ArrayTypeName.ofTypeName(PrimitiveTypeName.INT)), "a")
    .beginMethodBody()
    .addStatement("int sum = 0")
    .beginFor("int i = 0; i < a.length; i++")
    .beginFor("int j = 0; j < a[i].length; j++")
    .addStatement("sum = sum + a[i][i]")
    .endFor()
    .endFor()
    .end()
    .build();
```

The generated output is identical to the example above,
but the code is much more concise and the formatting is less error-prone.\
Use `beginFor` and `endFor` methods to manage the structure of `for` loops,
and `addStatement` method to format code lines with automatic semicolon and newline handling.

### while loop

Here is an example of a `while` loop

```java
MethodSpec modulo = MethodSpec.builder(INT, "modulo", Modifier.PUBLIC)
    .addParameter(INT, "a")
    .beginMethodBody()
    .addStatement("int b = a > 0 ? a : -a")
    .beginWhile("b >= 3")
    .addStatement(" b = b / 3")
    .endWhile()
    .addStatement("return b")
    .end()
    .build();
```

And this is the generated output.

```java
public int modulo(int a) {
    int b = a > 0 ? a : -a;
    while (b >= 3) {
        b = b / 3;
    }
    return b;
}
```

Use `beginWhile` and `endWhile` methods to manage the structure of while loop statements.

### do-while loop

Here is an example of a `do while` loop

```java
MethodSpec modulo = MethodSpec.builder(INT, "modulo", Modifier.PUBLIC)
    .addParameter(INT, "a")
    .addParameter(INT, "b")
    .beginMethodBody()
    .addStatement("a = $T.abs(a)", Math.class)
    .addStatement("b = $T.abs(b)", Math.class)
    .beginDo("a > b")
    .addStatement("a = a / b")
    .endDo()
    .addStatement("return a")
    .end()
    .build();
```

And this is the generated output.

```java
public int modulo(int a, int b) {
    a = Math.abs(a);
    b = Math.abs(b);
    do {
        a = a / b;
    } while (a > b);
    return a;
}
```

Use `beginDo` and `endDo` methods to manage the structure of while loop statements.

### if-elseif-else conditional branching

Here is an example of a `if-elseif-else` conditional branching

```java
MethodSpec ifExample = MethodSpec.builder(VOID, "ifExample", Modifier.PUBLIC)
    .addParameter(INT, "a")
    .beginMethodBody()
    .beginIf("a < 5")
    .addStatement("System.out.println($S)", "The parameter is less than 5")
    .beginElseIf("a < 100")
    .addStatement("System.out.println($S)", "The parameter is greater or equal to 5 and less than 100")
    .beginElse()
    .addStatement("System.out.println($S)", "The parameter is greater than or equal to 100")
    .endIf()
    .end()
    .build();
```

And this is the generated output.

```java
public void ifExample(int a) {
    if (a < 5) {
        System.out.println("The parameter is less than 5");
    } else if (a < 100) {
        System.out.println("The parameter is greater or equal to 5 and less than 100");
    } else {
        System.out.println("The parameter is greater than or equal to 100");
    }
}
```

Use `beginIf`, `beginElseIf`, `beginElse`, and `endIf` methods to manage the structure of `if-elseif-else` conditional
branching statements.

### switch-case-default statement

Here is an example of a `switch-case-default` selection statement

```java
MethodSpec ifExample = MethodSpec.builder(VOID, "switchExample", Modifier.PUBLIC)
    .addParameter(CHAR, "ch")
    .beginMethodBody()
    .beginSwitch("ch")
    .beginCase("'a'")
    .addStatement("System.out.println($S)", "Found a")
    .breakCase()
    .beginCase("'b'")
    .addStatement("System.out.println($S)", "Found b")
    .breakCase()
    .beginCase("'c'")
    .endCase()
    .beginCase("'d'")
    .addStatement("System.out.println($S)", "Found c and d")
    .breakCase()
    .beginDefault()
    .addStatement("System.out.println($S)", "Found other char")
    .endSwitch()
    .end()
    .build();
```

And this is the generated output.

```java
public void switchExample(char ch) {
    switch (ch) {
        case 'a':
            System.out.println("Found a");
            break;
        case 'b':
            System.out.println("Found b");
            break;
        case 'c':
        case 'd':
            System.out.println("Found c and d");
            break;
        default:
            System.out.println("Found other char");
    }
}
```

Use `beginSwitch`, `beginCase`, `breakCase`, `beginDefault`, and `endSwitch` methods to manage the structure of
`switch-case-default` selection statements.

### try-catch-finally statement

Here is an example of a `try-catch-finally` statement.

```java
MethodSpec readFile = MethodSpec.builder(VOID, "readFile", Modifier.PUBLIC)
    .addAnnotation(AnnotationInstanceSpec.builder(SuppressWarnings.class).addMember("value", "CallToPrintStackTrace").build())
    .addParameter(File.class, "file")
    .beginMethodBody()
    .addStatement("$T br = null", BufferedReader.class)
    .beginTry()
    .addStatement("br = new $T(new $T(file))", BufferedReader.class, FileReader.class)
    .addStatement("String line")
    .beginWhile("(line = br.readLine()) != null")
    .addStatement("System.out.println(line)")
    .endWhile()
    .beginCatch("IOException e")
    .addStatement("e.printStackTrace()")
    .beginFinally()
    .beginIf("br != null")
    .beginTry()
    .addStatement("br.close()")
    .beginCatch("$T e", IOException.class)
    .addStatement("e.printStackTrace()")
    .endTry()
    .endIf()
    .endTry()
    .end()
    .build();
```

And this is the generated output.

```java

@SuppressWarnings("CallToPrintStackTrace")
public void readFile(File file) {
    BufferedReader br = null;
    try {
        br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
```

Use `beginTry`，`beginCatch`，`beginFinally` and `endTry` methods to manage the structure of `try-catch-finally`
statements.

Here is an example of a `try-with-resources` statement.

```java
MethodSpec readFile = MethodSpec.builder(VOID, "readFile", Modifier.PUBLIC)
    .addAnnotation(AnnotationInstanceSpec.builder(SuppressWarnings.class).addMember("value", "CallToPrintStackTrace").build())
    .addParameter(File.class, "file")
    .beginMethodBody()
    .beginTry("$T br = new $T(new $T(file))", BufferedReader.class, BufferedReader.class, FileReader.class)
    .addStatement("String line")
    .beginWhile("(line = br.readLine()) != null")
    .addStatement("System.out.println(line)")
    .endWhile()
    .beginCatch("IOException e")
    .addStatement("e.printStackTrace()")
    .endTry()
    .end()
    .build();
```

And this is the generated output.

```java

@SuppressWarnings("CallToPrintStackTrace")
public void readFile(File file) {
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

Use the parameterized `beginTry` method to generate `try-with-resources` statements.

### BracketFlow

It's useful for writing the code like (...), {...}, ({...})

### placeholder

To simplify the output of formatted strings, hawk-java-generator references JavaPoet and provides functionality similar
to String.format("template with placeholders", parameters...).

The following types of placeholders are currently supported:

#### $L for Literals

Use `$L` as a placeholder for literals. During output, it will be substituted with the literal value of the
corresponding parameter.
The current logic involves calling the `String.valueOf()` method to convert the parameter into a string for direct
output,
supporting almost all types of parameters.

#### $S for Strings

Use `$S` as a placeholder for string literals.
During output, it will be replaced with the string converted from the corresponding parameter.
The current logic calls the `String.valueOf()` method to convert the parameter into a string and
automatically encloses the output result in double quotes, supporting almost all types of parameters.

#### $T for Types

Use `$T` as a placeholder for Java types.
During output, it will be replaced with the name of the type represented by the corresponding parameter,
while simultaneously recording the fully qualified name of the type for future class reference resolution.
`$T` exclusively supports parameters that represent types:`java.lang.Class`, `javax.lang.model.type.TypeMirror`,
`javax.lang.model.element.Element`, `java.lang.reflect.Type`,
and type `type.glz.hawk.poet.java.TypeName` along with its subclasses in `hawk-java-generator` project.

#### $N for Names

Use $N as a placeholder for specific internal types in hawk-java-generator.
During output, it will be replaced with the value of the name attribute of the corresponding type.
`$N` only supports `TypeSpec`, `FieldSpec`, `MethodSpec`, `ParameterSpec`, and `java.lang.CharSequence`.

Here is an example demonstrating the usage of all placeholders:

```java
FieldSpec fieldSpec1 = FieldSpec.builder(String.class, "name", Modifier.PUBLIC).build();
FieldSpec fieldSpec2 = FieldSpec.builder(ParameterizedTypeName.of(Class.class, WildcardTypeName.of()), "clazz", Modifier.PUBLIC).build();
FieldSpec fieldSpec3 = FieldSpec.builder(PrimitiveTypeName.INT, "count", Modifier.PUBLIC).build();
FieldSpec fieldSpec4 = FieldSpec.builder(ParameterizedTypeName.of(Map.class, String.class, BigDecimal.class), "map", Modifier.PUBLIC).build();
ClassSpec classSpec = ClassSpec.builder("CodeAndControlFlowExample", Modifier.PUBLIC)
    .addField(fieldSpec1)
    .addField(fieldSpec2)
    .addField(fieldSpec3)
    .addField(fieldSpec4)
    .addConstructor(ConstructorSpec.builder(Modifier.PUBLIC)
        .beginConstructorBody()
        .addStatement("this.$N = $S", fieldSpec1, "cat")
        .addStatement("this.$N = $T.class", fieldSpec2, Map.class)
        .addStatement("this.$N = $L", fieldSpec3, 1000)
        .addStatement("this.$N = new $T<>()", fieldSpec4, HashMap.class)
        .end()
        .build())
    .build();
```

And this is the generated output.

```java

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

public class CodeAndControlFlowExample {
    public String name;
    public Class<?> clazz;
    public int count;
    public Map<String, BigDecimal> map;

    public CodeAndControlFlowExample() {
        this.name = "cat";
        this.clazz = Map.class;
        this.count = 1000;
        this.map = new HashMap<>();
    }
}
```

As shown in the example above:

* `$N` is replaced with field names (`name`, `clazz`, `count`, `map`).
* `$S` is replaced with the string `"cat"`.
* `$T` is replaced with class names `Map`, `HashMap`, and `BigDecimal`,
  while automatically adding the corresponding imports: `java.util.Map`, `java.util.HashMap`,
  and `java.math.BigDecimal`.
* `$L` is replaced with the number `1000`.

### Placeholder and Argument Matching

`glz.hawk.codepoet.java.JavaCodeBlock` is modeled after `javapoet`'s `CodeBlock` class and serves as a specialized class
for
managing code blocks. It supports multiple placeholder positioning and argument matching methods, but only one matching
approach can be used within a single code block at a time.

#### Relative Positional Arguments

Arguments are arranged in the exact order of the placeholders in the format string.
The number of arguments must match the number of placeholders, and they correspond one-to-one sequentially.

As shown below:

```java
JavaCodeBlock codeBlock = JavaCodeBlock.of("I ate $L $L.", 4, "apples");
```

Will output

```text
I ate 4 apples.
```

#### Indexed Positional Arguments

Add an integer index (starting from 1) before the placeholder in the format string,
and use this index to locate the corresponding argument in the passed argument list.

As shown below:

```java
JavaCodeBlock codeBlock = JavaCodeBlock.of("I ate $2L $1L", "apples", 4);
```

Will also output

```text
I ate 4 apples.
```

#### Named Arguments

Use the syntax `$argumentName:X` where `X` is the format character and call `JavaCodeBlock.addNamed()` or
`JavaCodeBlock.ofNamed()` with a map containing all argument keys in the format string.
Parameter names may only use characters from a-z, A-Z, 0-9, and _, and must begin with a lowercase letter.

As shown below:

```text
Map<String, Object> map = new HashMap<>();
map.put("food", "tacos");
map.put("count", 3);
JavaCodeBlock codeBlock = JavaCodeBlock.ofNamed("I ate $count:L $food:L",map);
```

Will still output

```text
I ate 4 apples.
```

## variable type

The variable type supports all types in Java syntax, used to define variables, parameters, and method return values。

### primitive type

`type.glz.hawk.poet.java.PrimitiveTypeName` is an enum class used to map Java's primitive types.

### void type

`type.glz.hawk.poet.java.VoidTypeName` is an enum class used to map Java's void type.

### class type

`type.glz.hawk.poet.java.ClassName` corresponds to a Java class.\
It provides the following static factory methods to construct a ClassName:

```java
import javax.lang.model.element.TypeElement;

public static ClassName ofClass(Class<?> clazz);

public static ClassName of(String packageName, String simpleName, String... simpleNames);

public static ClassName of(TypeElement typeElement);

public static ClassName ofGuess(String classNameString);
```

### parameterized clas type

`type.glz.hawk.poet.java.ParameterizedTypeName` corresponds to a class with type parameters
and supports all those permitted by Java syntax.\
It provides the following static factory methods to construct a ParameterizedTypeName：

```java
import type.glz.hawk.poet.java.ClassName;
import type.glz.hawk.poet.java.TypeName;

public static ParameterizedTypeName of(ClassName rawType, List<TypeName> typeArguments);

public static ParameterizedTypeName of(ClassName rawType, TypeName... typeArguments);

public static ParameterizedTypeName of(Class<?> rawType, TypeName... typeArguments);

public static ParameterizedTypeName of(Class<?> rawType, Class<?>... typeArguments);
```

### array type

`type.glz.hawk.poet.java.ArrayTypeName` corresponds to arrays and supports arrays of any type, including
multidimensional arrays.\
It provides the following static factory methods to construct an ArrayTypeName：

```java
import java.lang.reflect.Type;

import type.glz.hawk.poet.java.TypeName;

public static ArrayTypeName ofTypeName(TypeName componentTypeName);

public static ArrayTypeName ofType(Type type);

public static ArrayTypeName ofClass(Class<?> clazz);
```

### type variable

`type.glz.hawk.poet.java.TypeVariableName` is used to support type parameters required for generics.\
It provides the following static factory methods to construct a typeVariableName：

```java
import javax.lang.model.type.TypeVariable;

import type.glz.hawk.poet.java.TypeName;

public static TypeVariableName of(String name);

public static TypeVariableName of(String name, TypeName... bounds);

public static TypeVariableName of(String name, Class<?>... bounds);

public static TypeVariableName of(String name, Iterable<TypeName> bounds);

public static TypeVariableName of(TypeVariable typeVariable);
```

### wildcard type

`type.glz.hawk.poet.java.WildcardTypeName` is used to support wildcard types.\
It provides the following static factory methods to construct a WildcardTypeName：

```java
import javax.lang.model.type.WildcardType;

import type.glz.hawk.poet.java.TypeName;

public static WildcardTypeName of();

public static WildcardTypeName of(List<TypeName> upperBounds, List<TypeName> lowerBounds);

public static WildcardTypeName ofUpper(TypeName typeName);

public static WildcardTypeName ofUpper(Class<?> clazz);

public static WildcardTypeName ofLower(TypeName typeName);

public static WildcardTypeName ofLower(Class<?> clazz);

public static WildcardTypeName of(WildcardType wildcardType);
```

Here is an example demonstrating all type usages：

```java
import glz.hawk.codepoet.java.ClassSpec;
import glz.hawk.codepoet.java.InterfaceSpec;
import glz.hawk.codepoet.java.MethodSpec;

import javax.lang.model.element.Modifier;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

ClassSpec classSpec = ClassSpec.builder("TypeExample", Modifier.PUBLIC, Modifier.ABSTRACT)
        .addTypeVariable(TypeVariableName.of("K"))
        .addField(PrimitiveTypeName.DOUBLE, "d1", Modifier.STATIC)
        .addField(PrimitiveTypeName.FLOAT, "f1", Modifier.STATIC)
        .addField(LocalDateTime.class, "dateTime", Modifier.PUBLIC, Modifier.STATIC)
        .addField(ClassName.ofClass(LocalDate.class), "date")
        .addField(ClassName.ofGuess(LocalTime.class.getCanonicalName()), "time")
        .addField(ClassName.of("java.util", "Map", "Entry"), "entry")
        .addField(TypeVariableName.of("K"), "k", Modifier.PRIVATE)
        .addField(ParameterizedTypeName.of(List.class, TypeVariableName.of("K")), "ks", Modifier.PUBLIC)
        .addField(ParameterizedTypeName.of(List.class, WildcardTypeName.of()), "ks1", Modifier.PUBLIC)
        .addField(ParameterizedTypeName.of(List.class, WildcardTypeName.ofUpper(Serializable.class)), "ks2", Modifier.PUBLIC)
        .addField(ParameterizedTypeName.of(List.class, WildcardTypeName.ofLower(Serializable.class)), "ks3", Modifier.PUBLIC)
        .addField(ParameterizedTypeName.of(Map.class, ClassName.ofClass(String.class), WildcardTypeName.of()), "map1", Modifier.PUBLIC)
        .addField(ParameterizedTypeName.of(Map.class, ClassName.ofClass(String.class), ParameterizedTypeName.of(Map.class, ClassName.ofClass(Integer.class), WildcardTypeName.ofUpper(TypeVariableName.of("K")))), "map2", Modifier.PUBLIC)
        .addField(ArrayTypeName.ofTypeName(TypeVariableName.of("K")), "array1", Modifier.PUBLIC)
        .addField(PrimitiveTypeName.INT, "intA", Modifier.PUBLIC)
        .addField(Integer.class, "integerA", Modifier.PUBLIC)
        .addField(ArrayTypeName.ofTypeName(PrimitiveTypeName.INT), "intArray", Modifier.PUBLIC)
        .addField(ArrayTypeName.ofTypeName(ArrayTypeName.ofTypeName(PrimitiveTypeName.INT)), "intArrayArray", Modifier.PUBLIC)
        .addField(ArrayTypeName.ofClass(Integer.class), "integerArray", Modifier.PUBLIC)
        .addMethod(MethodSpec.builder(VoidTypeName.VOID, "method0", Modifier.PUBLIC, Modifier.ABSTRACT).build())
        .addMethod(MethodSpec.builder(TypeVariableName.of("H"), "method1", Modifier.PROTECTED, Modifier.ABSTRACT)
                .addTypeVariable(TypeVariableName.of("H"))
                .addParameter(TypeVariableName.of("K"), "k", Modifier.FINAL)
                .build())
        .addMethod(MethodSpec.builder(TypeVariableName.of("H"), "method2", Modifier.PROTECTED, Modifier.ABSTRACT)
                .addTypeVariable(TypeVariableName.of("H"))
                .addTypeVariable(TypeVariableName.of("V"))
                .addTypeVariable(TypeVariableName.of("E", Throwable.class))
                .addParameter(TypeVariableName.of("K"), "k", Modifier.FINAL)
                .addParameter(TypeVariableName.of("V"), "v", Modifier.FINAL)
                .addThrowable(TypeVariableName.of("E"))
                .build())
        .build();
```

And this is the generated output:

```java
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class TypeExample<K> {
    static double d1;
    static float f1;
    public static LocalDateTime dateTime;
    LocalDate date;
    LocalTime time;
    Entry entry;
    private K k;
    public List<K> ks;
    public List<?> ks1;
    public List<? extends Serializable> ks2;
    public List<? super Serializable> ks3;
    public Map<String, ?> map1;
    public Map<String, Map<Integer, ? extends K>> map2;
    public K[] array1;
    public int intA;
    public Integer integerA;
    public int[] intArray;
    public int[][] intArrayArray;
    public Integer[] integerArray;

    public abstract void method0();

    protected abstract <H> H method1(final K k);

    protected abstract <H, V, E extends Throwable> H method2(final K k, final V v) throws E;
}
```

## static import

The core functionality of static imports is to omit the class name and directly use static members,
thereby achieving the goal of simplifying code and enhancing expressiveness. Its scope applies to the entire Java file.\

`TypeSpec` and its subclasses provide the following APIs for adding static references:：

```java
public T addStaticImport(Enum<?> constant);

public T addStaticImport(Class<?> clazz, String... names);

public T addStaticImport(ClassName className, String... names);
```

Here is an example of static imports:

```java
ClassSpec classSpec = ClassSpec.builder("StaticImportDemo", Modifier.PUBLIC)
    .addStaticImport(PrimitiveTypeName.class, "*")
    .addStaticImport(PrimitiveTypeName.BOOLEAN)
    .addStaticImport(PrimitiveTypeName.class, "INT", "LONG")
    .addStaticImport(ClassName.ofClass(PrimitiveTypeName.class), "CHAR", "DOUBLE")
    .build();
```

And this is the generated output:

```java

import static type.glz.hawk.poet.java.PrimitiveTypeName.BOOLEAN;
import static type.glz.hawk.poet.java.PrimitiveTypeName.CHAR;
import static type.glz.hawk.poet.java.PrimitiveTypeName.DOUBLE;
import static type.glz.hawk.poet.java.PrimitiveTypeName.INT;
import static type.glz.hawk.poet.java.PrimitiveTypeName.LONG;

public class StaticImportDemo {
}
```

## Define Types

The supported types for definition are: classes, interfaces, enums, and annotations.

### Define Fields

`FieldSpec` is responsible for maintaining the field's type, name, modifiers, annotations, initialization value, and
javadoc.\
Here is an example of FieldSpec usage:

```java
import glz.hawk.codepoet.java.ClassSpec;
import glz.hawk.codepoet.java.FieldSpec;
import glz.hawk.codepoet.java.JavaFile;
import type.glz.hawk.poet.java.ArrayTypeName;
import type.glz.hawk.poet.java.ParameterizedTypeName;

import javax.annotation.Nonnull;

import java.util.Map;

import static javax.lang.model.element.Modifier.*;

FieldSpec fieldSpec1 = FieldSpec.builder(String.class, "name", PRIVATE)
        .setJavadoc("This is a field javadoc example.")
        .build();

FieldSpec fieldSpec2 = FieldSpec.builder(ArrayTypeName.ofTypeName(INT), "numbers", PUBLIC, STATIC)
        .setInitializer("new $T[]{$L, $L, $L}", INT, 1, 2, 3)
        .addAnnotation(Nonnull.class)
        .build();

ClassSpec classSpec = ClassSpec.builder("FieldDemo", PUBLIC)
        .addField(fieldSpec1)
        .addField(fieldSpec2)
        .addField(ParameterizedTypeName.of(Map.class, String.class, Object.class), "map", PRIVATE)
        .addField(String.class, "str2", PROTECTED)
        .build();
```

And this is the generated output:

```java
import java.util.Map;
import javax.annotation.Nonnull;

public class FieldDemo {
    /**
     * This is a field javadoc example.
     */
    private String name;
    @Nonnull
    public static int[] numbers = new int[]{1, 2, 3};
    private Map<String, Object> map;
    protected String str2;
}
```

Not only can `FieldSpec.Builde`r be used to define a `FieldSpec` and then add it to a class definition,
but fields can also be directly added to a class definition via a shortcut method that requires only
the type, variable name, and modifiers.
To define a static class variable, you need to add the `static` modifier.

### Define ParameterSpecs

`ParameterSpec` is responsible for maintaining the type, name, annotations, and modifiers of a method or constructor
parameter.\
Here is an example of ParameterSpec usage:

```java

import type.glz.hawk.poet.java.ArrayTypeName;

import javax.annotation.Nonnull;

import static javax.lang.model.element.Modifier.*;

ParameterSpec param1 = ParameterSpec.builder(String.class, "name").build();
ParameterSpec param2 = ParameterSpec.builder(String.class, "unmodifiableName", FINAL).build();
ParameterSpec param3 = ParameterSpec.builder(String.class, "simpleName")
        .addAnnotation(Nonnull.class)
        .build();

MethodSpec method1 = MethodSpec.builder(String.class, "search1", PUBLIC, ABSTRACT)
        .addParameters(param1, param2, param3)
        .build();

MethodSpec method2 = MethodSpec.builder(String.class, "search2", PUBLIC, ABSTRACT)
        .addParameter(String.class, "name", FINAL)
        .build();

MethodSpec method3 = MethodSpec.builder(String.class, "search3", PUBLIC, ABSTRACT)
        .addParameter(String.class, "firstName")
        .addParameter(ArrayTypeName.ofClass(String.class), "names")
        .varargs()
        .build();

ClassSpec classSpec = ClassSpec.builder("ParameterDemo", PUBLIC, ABSTRACT)
        .addMethod(method1)
        .addMethod(method2)
        .addMethod(method3)
        .build();
```

And this is the generated output:

```java
import javax.annotation.Nonnull;

public abstract class ParameterDemo {
    public abstract String search1(String name, final String unmodifiableName, @Nonnull String simpleName);

    public abstract String search2(final String name);

    public abstract String search3(String firstName, String... names);
}
```

Not only can parameters be defined using ParameterSpec.Builder and then added to a method definition,
but they can also be added directly through a shortcut method that only requires providing the type,
variable name, and modifiers.\
To define a varargs parameter: the last parameter of the method must be an array,
and you must also call `varargs()` to inform `MethodSpec` that the last parameter is a varargs parameter.

