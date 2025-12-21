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

package glz.hawk.bean.mapper.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * This class is responsible for
 *
 * @author Hawk
 */
abstract class TypeMirrorUtils {

    static TypeMirror classToTypeMirror(Class<?> cls, Types typeUtils, Elements elementUtils) {
        if (cls == null) return null;

        // 原始类型
        if (cls.isPrimitive()) {
            if (cls == boolean.class) return typeUtils.getPrimitiveType(TypeKind.BOOLEAN);
            if (cls == byte.class) return typeUtils.getPrimitiveType(TypeKind.BYTE);
            if (cls == short.class) return typeUtils.getPrimitiveType(TypeKind.SHORT);
            if (cls == int.class) return typeUtils.getPrimitiveType(TypeKind.INT);
            if (cls == long.class) return typeUtils.getPrimitiveType(TypeKind.LONG);
            if (cls == char.class) return typeUtils.getPrimitiveType(TypeKind.CHAR);
            if (cls == float.class) return typeUtils.getPrimitiveType(TypeKind.FLOAT);
            if (cls == double.class) return typeUtils.getPrimitiveType(TypeKind.DOUBLE);
            // void
            if (cls == void.class) return typeUtils.getNoType(TypeKind.VOID);
        }

        // 数组
        if (cls.isArray()) {
            TypeMirror comp = classToTypeMirror(cls.getComponentType(), typeUtils, elementUtils);
            if (comp == null) return null;
            return typeUtils.getArrayType(comp);
        }

        // 引用类型（包括包装类、集合等）
        // 使用 canonical name（例如 "java.util.List"）
        String qName = cls.getCanonicalName();
        if (qName == null) {
            // 匿名类、局部类无法通过 getTypeElement 找到
            return null;
        }

        TypeElement te = elementUtils.getTypeElement(qName);
        if (te == null) {
            // 可能在编译时不可见或类路径不同
            return null;
        }

        // 如果类没有类型参数，直接返回 asType()
        if (cls.getTypeParameters().length == 0) {
            return te.asType();
        }

        // 类声明有类型参数（例如 class Foo<T>），但运行时 Class<?> 无法提供具体实参。
        // 这里有两种常见策略：返回擦除类型，或用通配符填充类型参数。
        // 1) 返回擦除：
        //    return types.erasure(te.asType());
        // 2) 或者返回带通配符的DeclaredType，例如 Foo<?> (更保留泛型结构)
        try {
            javax.lang.model.type.TypeMirror[] wildcards =
                Arrays.stream(te.getTypeParameters().toArray(new javax.lang.model.element.TypeParameterElement[0]))
                    .map(tp -> typeUtils.getWildcardType(null, null))
                    .toArray(javax.lang.model.type.TypeMirror[]::new);
            return typeUtils.getDeclaredType(te, wildcards);
        } catch (IllegalArgumentException ex) {
            // 如果构造失败，回退到擦除
            return typeUtils.erasure(te.asType());
        }
    }

    static boolean isPrimitiveOrWrapper(TypeMirror typeMirror) {
        // primitive type
        if (typeMirror.getKind().isPrimitive()) {
            return true;
        }

        // primitive wrapper type
        String typeName = typeMirror.toString();
        return typeName.equals("java.lang.Boolean") ||
            typeName.equals("java.lang.Byte") ||
            typeName.equals("java.lang.Character") ||
            typeName.equals("java.lang.Short") ||
            typeName.equals("java.lang.Integer") ||
            typeName.equals("java.lang.Long") ||
            typeName.equals("java.lang.Float") ||
            typeName.equals("java.lang.Double");
    }

    static boolean isVoidOrWrapper(TypeMirror typeMirror, Elements elementUtils, Types typeUtils) {
        if (typeMirror.getKind() == TypeKind.VOID) {
            return true;
        }
        TypeMirror javaLangVoid = elementUtils.getTypeElement(Void.TYPE.getCanonicalName()).asType();
        return typeMirror.getKind() == TypeKind.DECLARED && typeUtils.isSameType(typeMirror, javaLangVoid);
    }

    static boolean isVoid(TypeMirror typeMirror) {
        return typeMirror.getKind() == TypeKind.VOID;
    }


    static boolean isMap(TypeMirror typeMirror, Types typeUtils, Elements elementUtils) {

        if (typeMirror.getKind() != TypeKind.DECLARED && typeMirror.getKind() != TypeKind.TYPEVAR && typeMirror.getKind() != TypeKind.WILDCARD) {
            return false;
        }

        TypeElement mapElement = elementUtils.getTypeElement(Map.class.getCanonicalName());
        if (mapElement == null) return false;


        TypeMirror wildcard = typeUtils.getWildcardType(null, null);
        TypeMirror mapOfUnknown = typeUtils.getDeclaredType(mapElement, wildcard, wildcard);


        if (typeUtils.isAssignable(typeMirror, mapOfUnknown)) {
            return true;
        }

        try {
            TypeMirror erasedTm = typeUtils.erasure(typeMirror);
            TypeMirror erasedMap = typeUtils.erasure(mapOfUnknown);
            return typeUtils.isAssignable(erasedTm, erasedMap);
        } catch (Exception e) {
            return false;
        }
    }

    public static MapMirror getMapMirror(Types typeUtils, TypeMirror typeMirror) {
        DeclaredType declaredMapType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredMapType.getTypeArguments();
        return new MapMirror(typeArguments.get(0), typeArguments.get(1));
    }

    public static boolean isArray(TypeMirror typeMirror) {
        return typeMirror.getKind() == TypeKind.ARRAY;
    }

    public static boolean isAbstractClass(TypeMirror typeMirror) {
        return isAbstractTemplate(typeMirror, te -> te.getKind() == ElementKind.CLASS && te.getModifiers().contains(Modifier.ABSTRACT));
    }

    public static boolean isInterface(TypeMirror typeMirror) {
        return isAbstractTemplate(typeMirror, te -> te.getKind() == ElementKind.INTERFACE);
    }

    public static boolean isAbstractClassOrInterface(TypeMirror typeMirror) {
        return isAbstractTemplate(typeMirror, te -> te.getKind() == ElementKind.INTERFACE || (te.getKind() == ElementKind.CLASS && te.getModifiers().contains(Modifier.ABSTRACT)));

    }

    public static boolean isIterable(TypeMirror typeMirror, Types typeUtils, Elements elementUtils) {

        TypeElement iterableElem = elementUtils.getTypeElement(Iterable.class.getCanonicalName());
        if (iterableElem == null) return false;

        TypeMirror iterableOfUnknown = typeUtils.getDeclaredType(iterableElem, typeUtils.getWildcardType(null, null));

        if (typeUtils.isAssignable(typeMirror, iterableOfUnknown)) {
            return true;
        }

        try {
            TypeMirror erasedTm = typeUtils.erasure(typeMirror);
            TypeMirror erasedIterable = typeUtils.erasure(iterableOfUnknown);
            return typeUtils.isAssignable(erasedTm, erasedIterable);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isAbstractTemplate(TypeMirror typeMirror, Function<TypeElement, Boolean> booleanFunction) {
        if (typeMirror == null) return false;
        if (typeMirror.getKind() != TypeKind.DECLARED) return false;
        Element el = ((DeclaredType) typeMirror).asElement();
        if (!(el instanceof TypeElement)) return false;
        TypeElement te = (TypeElement) el;
        return booleanFunction.apply(te);
    }

    static TypeMirror elementTypeMirror(TypeMirror typeMirror) {
        switch (typeMirror.getKind()) {
            case ARRAY:
                return ((ArrayType) typeMirror).getComponentType();
            case DECLARED:
                return ((DeclaredType) typeMirror).getTypeArguments().get(0);
            default:
                throw new IllegalStateException(String.format("The typeMirror() isn't a collection or array."));
        }
    }

    static TypeMirror rawTypeMirror(TypeMirror typeMirror,Types typeUtils){
        if (typeMirror.getKind() != TypeKind.DECLARED){
            throw new IllegalArgumentException(String.format("The typeMirror(%s) is not a class",typeMirror ));
        }

        return ((DeclaredType)typeMirror).getTypeArguments().isEmpty()? typeMirror:typeUtils.erasure(typeMirror);

    }
}
