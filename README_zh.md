## 选择你喜欢的语言

[English](README.md) | [中文](#)

---

# hawk-java-generator

`hawk-java-generator` 是一个用来生成 `.java` source files的JAVA API工具包.

代码生成对于注解处理器，对于基于元素据生成代码的场合有奇效，特别是有企业级数组字典，接口规范，数据模型的场景。
通过代码生成，只要维护元数据，就可以随时生成优秀的代码。结合[JAVA注解模型API包](https://)可以大大提高项目的可读性，规范性，和扩展能力。

`hawk-java-generator`参考了优秀的[square/javapoet](https://github.com/square/javapoet)
项目，该项目已经[不再维护](https://github.com/square/javapoet/discussions/866)。\
`hawk-java-generator`是更好的`javapoet`:

- 优化了java代码生成的效率。
- 自动处理引用和静态引用的碰撞问题。
- 提供了更便利，更接近java代码习惯的控制流写法。
- 提供了更完善和更便利的javadoc的写法。
- 提供了lambda表达式来完善流式API。

## 依赖

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

这是一个随处可见的 `HelloWorld` 类:

```java
public class HelloWorld {
    public static void main(String... args) {
        System.out.println("Hello,Hawk Java Generator.");
    }
}
```

这是一个用来生成上文所述的`HelloWorld`类的`HelloWorldGenerator`类:

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

通过`MethodSpec`来声明一个"main"方法，包括方法名称，方法参数，方法返回值，方法修饰符和方法体。
通过ClassSpec来声明一个`HelloWorld`类，并将"main"方法加入该类。最后通过`JavaFile`将该类输出到控制台，当然也可以输出到文件里。

## 代码与控制流

`hawk-java-generator`的API尽可能采用不可变Java对象。同时，它还结合了建造者模式、链式调用，可变参数和lambda表达式，以使API更易于使用。
hawk-java-generator为类（`ClassSpec`）、接口（`InterfaceSpec`）、枚举（`EnumSpec`）、注解（`AnnotationSpec`）、
字段（`FieldSpec`）、方法（`MethodSpec`）、构造函数（`ConstructorSpec`）、参数（`ParameterSpec`）、注解实例（
`AnnotationInstanceSpec`）
、javadoc（`FileJavaDoc`, `TypeJavaDoc`, `FieldJavaDoc`, `MethodJavaDoc`, `ConstructorJavaDoc`）提供了相应的模型。

对于方法和构造函数的方法体，以及javadoc的内容，`hawk-java-generator`提供了格式化字符串，缩进，换行API和控制流模型来帮助生成代码。
你将发现写法和直接写java代码的方式几乎一摸一样。

### for 循环

#### 代码格式完全控制

这是一个完全控制代码格式的示例

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

这是生成的结果

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

通过`addCode`方法来添加实际代码（分号需要手动添加），通过`addIndent`和`removeIndent`方法来控制缩进，通过`addNewLine`方法来换行。

#### 代码格式由`hawk-java-generator`控制

这是一个由`hawk-java-generator`来控制代码格式的示例

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

生成的结果和上文的示例完全一样，但是代码非常简洁，而且格式也不容易出错.\
通过`beginFor`和`endFor`方法来维护`for`循环的格式，通过`addStatement`方法来维护代码行的格式（自动添加分号和换行符）。

### while 循环

这是一个`while`循环的示例

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

这是生成的结果

```java
public int modulo(int a) {
    int b = a > 0 ? a : -a;
    while (b >= 3) {
        b = b / 3;
    }
    return b;
}
```

通过`beginWhile`和`endWhile`方法来控制while循环语句的格式

### do while 循环

这是一个`do-while`循环的示例

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

这是生成的结果

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

通过`beginDo`和`endDo`方法来控制`do while`循环语句的格式

### if-elseif-else 条件分支

这是一个`if-elseif-else`条件分支的示例

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

这是生成的结果

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

通过`beginIf`，`beginElseIf`，`beginElse`，`endIf`方法来控制`if-elseif-else`条件分支语句的格式。

### switch-case-default 选择分支

这是一个`switch-case-default`选择分支语句的示例

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

这是生成的结果

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

通过`beginSwitch`，`beginCase`，`breakCase`，`beginDefault`，`endSwitch`方法来控制`switch-case-default`选择分支语句的格式。

### try-catch-finally 语句

这是一个`try-catch-finally`语句的示例

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

这是生成的结果

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

通过`beginTry`，`beginCatch`，`beginFinally`和`endTry`方法来控制`try-catch-finally`语句的格式。

这是一个`try-with-resources`语句的示例

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

这是生成的结果

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

通过带参数的`beginTry`方法来生成`try-with-resources`语句。

### BracketFlow

用来帮助书写类似 (...), {...}, ({...})这样的代码块，提供缩进，启示换行，结束换行等选项

### 占位符

为了方便格式化字符串的输出， `hawk-java-generator`参照`JavaPoet`，提供了类似String.format("template", parameters...)
的功能。\
目前提供了如下几种类型的占位符：

#### $L指代字面量

使用`$L`来作为字面量的占位符，它在输出时，会被替换为对应的参数的字面值。\
当前逻辑是调用String.valueOf()方法将参数转成字符串直接输出，几乎支持所有类型的参数。

#### $S指代字符串

使用`$S`来作为字符串的占位符，它在输出时，会被替换为对应参数转换成的字符串。
`$L`调用String.valueOf()方法将参数转成字符串输出，并且自动用双引号加输出结果包起来，几乎支持所有类型的参数。

#### $T指代类型

使用`$T`来作为java类型的占位符，它在输出时，会被替换为对应的参数表示类型的名称，同时会记录该类型的完整名称，作为将来计算类引用的依据。
`$T`仅仅支持用来表达类型的参数：`java.lang.Class`，`javax.lang.model.type.TypeMirror`，`javax.lang.model.element.Element`，
`java.lang.reflect.Type`，`hawk-java-generator`的自有类型`type.glz.hawk.poet.java.TypeName`以及其子类。

#### $N指代名字

使用`$N`来作为`hawk-java-generator`内部特定类型的占位符，它在输出时，会被替换为对应的类型的名称属性的值。\
`$N`仅仅支持`TypeSpec`，`FieldSpec`，`MethodSpec`，`ParameterSpec`和`java.lang.CharSequence`。

这是一个演示所有占位符用法的例子：

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

这是输出结果

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

如上例所示：
`$N`被field的名称（`name`、`clazz`、`count`）替换。
`$S`被字符串`"cat"`替换。
`$T`被`Map`，`HashMap`和`BigDecimal`的类名替换并且自动添加了类引用`java.util.Map`，`java.util.HashMap`和
`java.math.BigDecimal`。
`$L`被数字`1000`替换。

### 定位符和参数匹配

`glz.hawk.codepoet.java.JavaCodeBlock`模仿了`javapoet`的`CodeBlock`类，是专门用来管理代码块的类，它支持多种定位占位符和参数匹配的方法，
但同一个代码块同时只能使用一种匹配方法。

#### 相对位置参数

参数按照格式化字符串内的占位符一样的方式排列，有多少个占位符就有多少给参数，它们按顺序一一对应。

如下所示：

```java
JavaCodeBlock codeBlock = JavaCodeBlock.of("I ate $L $L.", 4, "apples");
```

将输出

```text
I ate 4 apples.
```

#### 索引位置参数

在格式化字符串里的占位符前加上整数索引（从1开始计算），通过索引在传入的参数列表里找到该占位符对应的参数。

如下所示：

```java
JavaCodeBlock codeBlock = JavaCodeBlock.of("I ate $2L $1L", "apples", 4);
```

也将输出

```text
I ate 4 apples.
```

#### 命名参数

使用`$argumentName:X` 语法（其中'X'为格式字符）来定义格式化字符串，并传递包含格式字符串中所有参数键的map作为参数给
`JavaCodeBlock.addNamed()` 或`JavaCodeBlock.ofNamed()`方法。
参数名称允许使用 a-z、A-Z、0-9 及 _ 字符，且必须以小写字母开头。

如下所示：

```text
Map<String, Object> map = new HashMap<>();
map.put("food", "tacos");
map.put("count", 3);
JavaCodeBlock codeBlock = JavaCodeBlock.ofNamed("I ate $count:L $food:L",map);
```

也将输出

```text
I ate 4 apples.
```

## 变量类型

变量类型支持Java语法中的所有类型，用来定义变量，参数，方法返回值。

### 基础类型

`type.glz.hawk.poet.java.PrimitiveTypeName`是一个枚举类，用来映射java的基础类型。

### void类型

`type.glz.hawk.poet.java.VoidTypeName`是一个枚举类，用来映射java的void类型。

### 类类型

`type.glz.hawk.poet.java.ClassName`对应于java类。\
提供了如下几个静态工厂方法用来构造ClassName：

```java
import javax.lang.model.element.TypeElement;

public static ClassName ofClass(Class<?> clazz);

public static ClassName of(String packageName, String simpleName, String... simpleNames);

public static ClassName of(TypeElement typeElement);

public static ClassName ofGuess(String classNameString);
```

### 参数化类类型

`type.glz.hawk.poet.java.ParameterizedTypeName`对应于带类型参数的类，支持java语法允许的所有类型参数。\
提供了如下几个静态工厂用来构造ParameterizedTypeName：

```java
import type.glz.hawk.poet.java.ClassName;
import type.glz.hawk.poet.java.TypeName;

public static ParameterizedTypeName of(ClassName rawType, List<TypeName> typeArguments);

public static ParameterizedTypeName of(ClassName rawType, TypeName... typeArguments);

public static ParameterizedTypeName of(Class<?> rawType, TypeName... typeArguments);

public static ParameterizedTypeName of(Class<?> rawType, Class<?>... typeArguments);
```

### 数组类型

`type.glz.hawk.poet.java.ArrayTypeName`对应于数组，支持任意类型的数组，包括多维数组。\
提供了如下几个静态工厂用来构造ArrayTypeName：

```java
import java.lang.reflect.Type;

import type.glz.hawk.poet.java.TypeName;

public static ArrayTypeName ofTypeName(TypeName componentTypeName);

public static ArrayTypeName ofType(Type type);

public static ArrayTypeName ofClass(Class<?> clazz);
```

### 类型变量

`type.glz.hawk.poet.java.TypeVariableName`用来支持泛型所需的类型参数。\
提供了如下几个静态工厂用来构造TypeVariableName：

```java
import javax.lang.model.type.TypeVariable;

import type.glz.hawk.poet.java.TypeName;

public static TypeVariableName of(String name);

public static TypeVariableName of(String name, TypeName... bounds);

public static TypeVariableName of(String name, Class<?>... bounds);

public static TypeVariableName of(String name, Iterable<TypeName> bounds);

public static TypeVariableName of(TypeVariable typeVariable);
```

### 通配符类型

`type.glz.hawk.poet.java.WildcardTypeName`用来支持通配符类型。\
提供了如下几个静态工厂用来构造WildcardTypeName：

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

这是一个所有类型用法的示例：

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

这是生成的代码：

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

## 静态引用

静态引用的核心功能是省略类名，直接使用静态成员，从而达到简化代码、提高表达力的目的。它的作用域是整个java文件范围内。

TypeSpec提供了如下增加静态引用的API：

```java
public T addStaticImport(Enum<?> constant);

public T addStaticImport(Class<?> clazz, String... names);

public T addStaticImport(ClassName className, String... names);
```

这是一个静态引用的示例：

```java
ClassSpec classSpec = ClassSpec.builder("StaticImportDemo", Modifier.PUBLIC)
    .addStaticImport(PrimitiveTypeName.class, "*")
    .addStaticImport(PrimitiveTypeName.BOOLEAN)
    .addStaticImport(PrimitiveTypeName.class, "INT", "LONG")
    .addStaticImport(ClassName.ofClass(PrimitiveTypeName.class), "CHAR", "DOUBLE")
    .build();
```

这是生成的代码

```java

import static type.glz.hawk.poet.java.PrimitiveTypeName.BOOLEAN;
import static type.glz.hawk.poet.java.PrimitiveTypeName.CHAR;
import static type.glz.hawk.poet.java.PrimitiveTypeName.DOUBLE;
import static type.glz.hawk.poet.java.PrimitiveTypeName.INT;
import static type.glz.hawk.poet.java.PrimitiveTypeName.LONG;

public class StaticImportDemo {
}
```

## 定义类型

支持定义的类型有：类、接口、枚举、注解。

### 定义类变量

`FieldSpec`负责维护类变量的类型，名称，修饰符，注解，初始化值和注释。\
这是一个`FieldSpec`用法的示例：

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

这是生成的代码

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

不仅可以用`FieldSpec.Builder`定义`FieldSpec`，再添加到类定义中，也可以直接通过提供类型，变量名称和修饰符的快捷方式直接添加变量到类定义中。
定义静态类变量只需要添加修饰符`static`

### 定义参数

`ParameterSpec`负责维护方法或者构造函数的参数的类型，名称，注解和修饰符。\
这是一个`ParameterSpec`的用法示例。

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

这是生成的代码：

```java
import javax.annotation.Nonnull;

public abstract class ParameterDemo {
    public abstract String search1(String name, final String unmodifiableName, @Nonnull String simpleName);

    public abstract String search2(final String name);

    public abstract String search3(String firstName, String... names);
}
```

不仅可以用`ParameterSpec.Builder`定义参数，再添加到方法定义中，也可以直接通过提供类型，变量名称和修饰符的快捷方式直接添加参数到方法定义中。\
定义可变参数：方法的最后一个参数必须是数组，同时还要调用`varargs()`告诉`MethodSpec`最后一个参数是可变参数。

## 定义方法
`MethodSpec`负责维护方法的返回类型、名称、参数、异常、修饰符、注解、方法体和注释。\
这是一个`MethodSpec`的用法示例：
```java
MethodSpec method1 = MethodSpec.builder(VOID, "getValue", PUBLIC, ABSTRACT)
                .addParameter(String.class, "key")
                .build();

MethodSpec method2 = MethodSpec.builder(INT, "sum", PUBLIC)
        .addParameter(INT, "a")
        .addParameter(INT, "b")
        .beginMethodBody()
        .addStatement("return a + b")
        .end()
        .build();

MethodJavadoc methodJavadoc = MethodJavadoc.builder()
        .addBlockTag(BlockTag.builder(BlockTagType.PARAM,"file").add("the file to be read.").build())
        .addBlockTag(BlockTag.builder(BlockTagType.RETURN).add("the content of file.").build())
        .addBlockTag(BlockTag.builder(IOException.class).add("if met error while read file").build())
        .beginJavadoc()
        .addDocument("read a file")
        .end()
        .build();

MethodSpec method3 = MethodSpec.builder(String.class, "readFile", PUBLIC, ABSTRACT)
        .addParameter(File.class, "file")
        .addThrowable(IOException.class)
        .setJavadoc(methodJavadoc)
        .build();

ClassSpec classSpec = ClassSpec.builder("MethodDemo", PUBLIC, ABSTRACT)
        .addMethod(method1)
        .addMethod(method2)
        .addMethod(method3)
        .build();
```
这是生成的代码：

```java
import java.io.File;
import java.io.IOException;

public abstract class MethodDemo {
    public abstract void getValue(String key);

    public int sum(int a, int b) {
        return a + b;
    }

    /**
     * read a file
     *
     * @param file the file to be read.
     * @return the content of file.
     * @throws IOException if met error while read file
     */
    public abstract String readFile(File file) throws IOException;
}
```

## 定义构造函数

### 定义接口

### 定义类

#### 定义类初始化块

#### 定义静态类初始化块

### 定义枚举

### 定义注解

### 定义泛型




### 定义内部类

### 定义匿名内部类

### 定义注解实例

### 定义javadoc

### 输出

JavaFile负责提供package和java文件的Java动词，并提供了输出到IO流的功能。








