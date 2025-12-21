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

import glz.hawkframework.core.helper.StringHelper;
import glz.hawk.bean.mapper.annotation.MappingSource;
import glz.hawk.bean.mapper.annotation.MappingTarget;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static glz.hawkframework.core.helper.StringHelper.unCapitalize;

/**
 * This class is responsible for
 *
 * @author Hawk
 */
class ObjectDescriptor implements Comparable<ObjectDescriptor> {

    public final String objectName;
    public final MappingSource mappingSource;
    public final MappingTarget mappingTarget;
    public final Integer index;
    public final TypeMirror typeMirror;
    /**
     * Whether the object is defined by the return type.
     */
    public final boolean definedByReturnType;
    /**
     * TODO: 记录对象的全路径,从参数名开始计算
     */
    public final String objectFullPath;
    public final String methodFullPath;
    private final Types typeUtils;
    private final Elements elementUtils;
    private final Messager messager;

    public Map<String, PropertyDescriptor> propertyDescriptorMap = new HashMap<>();

    /**
     * parse the return type of method
     */
    ObjectDescriptor(Types typeUtils, Elements elementUtils, Messager messager, String interfaceName, String methodName, ExecutableElement methodElement) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.mappingSource = null;
        this.mappingTarget = methodElement.getAnnotation(MappingTarget.class);
        this.typeMirror = methodElement.getReturnType();
        this.methodFullPath = String.join("/", interfaceName, methodName);
        this.objectFullPath = "methodReturn";

        parseTypeMirror(typeMirror, this.objectFullPath);
        this.objectName = returnObjectName();
        this.index = 0;
        this.definedByReturnType = true;


    }

    /**
     * parse the parameter of method
     */
    ObjectDescriptor(Types typeUtils, Elements elementUtils, Messager messager, String interfaceName, String methodName, VariableElement parameterElement, int index, String parentFullPath) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.objectName = parameterElement.getSimpleName().toString();
        this.typeMirror = parameterElement.asType();
        this.index = index;
        this.definedByReturnType = false;
        this.methodFullPath = String.join("/", interfaceName, methodName);
        this.objectFullPath = parentFullPath == null ? objectName : String.join(".", parentFullPath, objectName);

        parseTypeMirror(typeMirror, this.objectFullPath);

        this.mappingSource = parameterElement.getAnnotation(MappingSource.class);
        this.mappingTarget = parameterElement.getAnnotation(MappingTarget.class);
        if (mappingSource != null && mappingTarget != null) {
            throw new IllegalStateException(String.format("The parameter(%s) in the method(%s) can't be annotated %s and %s in the same time.", objectFullPath, methodFullPath, MappingSource.class.getSimpleName(), MappingTarget.class.getSimpleName()));
        }
    }

    private String returnObjectName() {
        if (mappingTarget != null && !StringHelper.isBlank(mappingTarget.returnObjectName())) {
            return mappingTarget.returnObjectName();
        } else if (typeMirror.getKind() == TypeKind.DECLARED) {
            return StringHelper.unCapitalize(typeUtils.asElement(typeMirror).getSimpleName().toString());
        } else if (typeMirror.getKind() == TypeKind.ARRAY) {
            return "array";
        } else {
            return "returnValue";
        }
    }


    private void parseTypeMirror(TypeMirror typeMirror, String parentFullPath) {
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return;
        }

        while (!(typeMirror instanceof NoType)) {
            TypeElement typeElement = (TypeElement) typeUtils.asElement(typeMirror);

            // getter
            typeElement.getEnclosedElements()
                .stream()
                .filter(this::isGetter)
                .forEach(e -> {
                    String getterName = e.getSimpleName().toString();
                    String propertyName = getterName.startsWith("get") ? unCapitalize(getterName.substring("get".length())) : unCapitalize(getterName.substring("is".length()));
                    if (StringHelper.isNotBlank(propertyName)) {
                        TypeMirror returnTypeMirror = ((ExecutableElement) e).getReturnType();
                        if (returnTypeMirror.getKind() == TypeKind.VOID) {
                            messager.printMessage(Diagnostic.Kind.WARNING, String.format("The return type of method(%s) is void.", methodFullPath));
                        } else {
                            GetterDescriptor getterDescriptor = new GetterDescriptor(getterName, AccessType.PUBLIC_METHOD, returnTypeMirror);
                            PropertyDescriptor propertyDescriptor = propertyDescriptorMap.computeIfAbsent(propertyName, p -> new PropertyDescriptor(parentFullPath, propertyName));
                            propertyDescriptor.add(getterDescriptor);
                        }
                    }

                });

            // setter
            typeElement.getEnclosedElements()
                .stream()
                .filter(this::isSetter)
                .forEach(e -> {
                    String setterName = e.getSimpleName().toString();
                    String propertyName = unCapitalize(setterName.substring(3));
                    if (StringHelper.isNotBlank(propertyName)) {
                        TypeMirror setterParameterTypeMirror = ((ExecutableElement) e).getParameters().get(0).asType();
                        SetterDescriptor setterDescriptor = new SetterDescriptor(setterName, AccessType.PUBLIC_METHOD, setterParameterTypeMirror);
                        PropertyDescriptor propertyDescriptor = propertyDescriptorMap.computeIfAbsent(propertyName, p -> new PropertyDescriptor(parentFullPath, p));
                        propertyDescriptor.add(setterDescriptor);
                    }
                });

            // field
            typeElement.getEnclosedElements()
                .stream()
                .filter(this::isPublicField)
                .forEach(e -> {
                    String propertyName = e.getSimpleName().toString();
                    TypeMirror fieldTypeMirror = e.asType();
                    PropertyDescriptor propertyDescriptor = propertyDescriptorMap.computeIfAbsent(propertyName, p -> new PropertyDescriptor(parentFullPath, p));
                    propertyDescriptor.add(new GetterDescriptor(propertyName, AccessType.PUBLIC_FIEld, fieldTypeMirror));
                    propertyDescriptor.add(new SetterDescriptor(propertyName, AccessType.PUBLIC_FIEld, fieldTypeMirror));
                });

            typeMirror = typeElement.getSuperclass();
        }

        propertyDescriptorMap.values().forEach(p -> {
            Collections.sort(p.getterDescriptors);
            Collections.sort(p.setterDescriptors);
        });
    }

    private boolean isPublicField(Element e) {
        return e.getKind() == ElementKind.FIELD && e.getModifiers().contains(Modifier.PUBLIC);
    }


    private boolean isGetter(Element e) {
        String methodName = e.getSimpleName().toString();
        return e.getKind() == ElementKind.METHOD &&
            e.getModifiers().contains(Modifier.PUBLIC) &&
            ((ExecutableElement) e).getParameters().isEmpty() &&
            (methodName.startsWith("get") || methodName.startsWith("is"));
    }

    private boolean isSetter(Element e) {
        String methodName = e.getSimpleName().toString();
        return e.getKind() == ElementKind.METHOD &&
            e.getModifiers().contains(Modifier.PUBLIC) &&
            ((ExecutableElement) e).getParameters().size() == 1 &&
            (methodName.startsWith("set"));
    }


    @Override
    public int compareTo(ObjectDescriptor o) {
        return this.index.compareTo(o.index);
    }

    boolean isMap() {
        return TypeMirrorUtils.isMap(typeMirror, typeUtils, elementUtils);
    }

    boolean isNullable() {
        if (mappingSource != null) {
            return mappingSource.nullable();
        } else if (mappingTarget != null) {
            return mappingTarget.nullable();
        }
        return false;
    }

    boolean isIterable() {
        return TypeMirrorUtils.isIterable(typeMirror, typeUtils, elementUtils);
    }

    boolean isAbstractClass() {
        return TypeMirrorUtils.isAbstractClass(typeMirror);
    }

    boolean isInterface() {
        return TypeMirrorUtils.isInterface(typeMirror);
    }

    boolean isAbstractClassOrInterface() {
        return TypeMirrorUtils.isAbstractClassOrInterface(typeMirror);
    }

    boolean isArray() {
        return TypeMirrorUtils.isArray(typeMirror);
    }

    boolean isArrayOrIterable() {
        return isArray() || isIterable();
    }
}
