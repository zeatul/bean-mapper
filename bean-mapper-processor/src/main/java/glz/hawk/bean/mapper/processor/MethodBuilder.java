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

import glz.hawk.bean.mapper.annotation.Converter;
import glz.hawk.bean.mapper.annotation.ConverterParam;
import glz.hawk.bean.mapper.annotation.ElementConverter;
import glz.hawk.bean.mapper.annotation.PropertyMapping;
import glz.hawk.codepoet.java.MethodSpec;
import glz.hawk.codepoet.java.ParameterSpec;
import glz.hawk.codepoet.java.javacode.ComplexJavaCodeBlockBuilder;
import glz.hawk.codepoet.java.javacode.ForFlow;
import glz.hawk.codepoet.java.javacode.IfFlow;
import glz.hawk.codepoet.java.type.*;
import glz.hawkframework.core.helper.StringHelper;

import javax.annotation.Nullable;
import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;

import static glz.hawk.bean.mapper.processor.TypeMirrorUtils.*;
import static glz.hawkframework.core.support.ArgumentSupport.argNotBlank;

/**
 * This class is responsible for
 *
 * @author Hawk
 */
class MethodBuilder {
    private final ExecutableElement executableElement;

    private final List<? extends VariableElement> variableElementList;

    private final String methodName;

    private final String interfaceName;

    private final TypeMirror returnTypeMirror;

    private final TypeName returnType;

    private final Types typeUtils;

    private final Elements elementUtils;

    private final Messager messager;

    private final String methodFullPath;

    /**
     * key是targetProperty的全路径：参数名+属性全路径
     */
    private final Map<String, ParamPropertyMapping> targetPropertyMappingMap = new HashMap<>();

    public MethodBuilder(Types typeUtils, Elements elementUtils, Messager messager, String interfaceName, ExecutableElement executableElement) {
        this.interfaceName = interfaceName;
        this.executableElement = executableElement;
        this.variableElementList = executableElement.getParameters();
        this.methodName = executableElement.getSimpleName().toString();
        this.returnTypeMirror = executableElement.getReturnType();
        this.returnType = TypeNameHelper.ofTypeMirror(returnTypeMirror);
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.methodFullPath = String.join("/", interfaceName, methodName);

    }

    public MethodSpec build() {
        MethodSpec.Builder methodBuilder = MethodSpec.builder(returnType, methodName, Modifier.PUBLIC)
            .addAnnotation(Override.class);

        variableElementList.forEach(p -> methodBuilder.addParameter(ParameterSpec.builder(TypeNameHelper.ofTypeMirror(p.asType()), p.getSimpleName().toString()).build()));

        int paramCount = variableElementList.size();
        if (returnType == VoidTypeName.VOID) {
            if (paramCount < 2) {
                throw new IllegalStateException(String.format("The method(%s) without return type must have at least 2 parameters", methodFullPath));
            }
        } else {
            if (paramCount < 1) {
                throw new IllegalStateException(String.format("The method(%s) without return type must have at least 1 parameters", methodFullPath));
            }
        }

        List<ParamDescriptor> allParameterDescriptors = new ArrayList<>();
        for (int i = 0; i < variableElementList.size(); i++) {
            allParameterDescriptors.add(new ParamDescriptor(typeUtils, elementUtils, messager, interfaceName, methodName, variableElementList.get(i), i, null));
        }

        List<ParamDescriptor> annotatedByMappingSourceList = new ArrayList<>();
        List<ParamDescriptor> annotatedByMappingTargetList = new ArrayList<>();
        List<ParamDescriptor> annotatedByNoneList = new ArrayList<>();
        allParameterDescriptors.forEach(pd -> {
            if (pd.mappingTarget != null) {
                annotatedByMappingTargetList.add(pd);
            } else if (pd.mappingSource != null) {
                annotatedByMappingSourceList.add(pd);
            } else {
                annotatedByNoneList.add(pd);
            }
        });

        // 获取输入参数和输出参数
        // The item in this list is support to export properties.
        List<ParamDescriptor> sourceParameterDescriptorList = new ArrayList<>();
        // The item in this list is support to import properties.
        List<ParamDescriptor> targetParameterDescriptorList = new ArrayList<>();

        if (annotatedByMappingTargetList.size() > 1) {
            // only one parameter can be annotated with @MappingTarget
            throw new IllegalStateException(String.format("At most one method(%s) parameter may be annotated with @MappingTarget.", methodFullPath));
        } else if (annotatedByMappingTargetList.size() == 1) {
            ParamDescriptor p = annotatedByMappingTargetList.get(0);
            if (isVoid(returnTypeMirror) || typeUtils.isAssignable(returnTypeMirror, p.typeMirror)) {
                targetParameterDescriptorList.add(p);
                sourceParameterDescriptorList.addAll(annotatedByMappingSourceList);
                sourceParameterDescriptorList.addAll(annotatedByNoneList);
                Collections.sort(sourceParameterDescriptorList);
            } else {
                throw new IllegalStateException(String.format("The parameter(%s) is annotated by @MappingTarget and will work as the return object, but the return type of method(%s) is incompatible with the parameter.",
                    p.paramFullPath, methodFullPath));
            }
        } else {
            if (isVoid(returnTypeMirror)) {
                if (annotatedByNoneList.isEmpty()) {
                    throw new IllegalStateException(String.format("All the parameters of method(%s) are annotated with @MappingSource, no parameter can be chosen as the mapping target.", methodFullPath));
                } else {
                    // the last parameter which isn't annotated with @MappingSource will be chosen as the mapping target.
                    sourceParameterDescriptorList.addAll(annotatedByMappingSourceList);
                    targetParameterDescriptorList.add(annotatedByNoneList.get(annotatedByNoneList.size() - 1));
                    for (int i = 0; i < annotatedByNoneList.size() - 1; i++) {
                        sourceParameterDescriptorList.add(annotatedByNoneList.get(i));
                    }
                    Collections.sort(sourceParameterDescriptorList);
                }
            } else {
                sourceParameterDescriptorList.addAll(annotatedByMappingSourceList);
                sourceParameterDescriptorList.addAll(annotatedByNoneList);
                Collections.sort(sourceParameterDescriptorList);
                // the return type of method works as the mapping target.
                targetParameterDescriptorList.add(new ParamDescriptor(typeUtils, elementUtils, messager, interfaceName, methodName, executableElement));
            }
        }

        if (sourceParameterDescriptorList.isEmpty()) {
            throw new IllegalStateException(String.format("Cant' find any source parameter in the method(%s)", methodFullPath));
        }

        // 获取自定义的属性映射
        PropertyMapping[] mappings = executableElement.getAnnotationsByType(PropertyMapping.class);

        for (PropertyMapping mapping : mappings) {
            // source和target至少一个不能为空
            if (StringHelper.isBlank(mapping.source()) && StringHelper.isBlank(mapping.target())) {
                String msg = String.format("the method[%s] of interface[%s] has illegal PropertyMapping annotation which has no source and no target.", methodName, interfaceName);
                messager.printMessage(Diagnostic.Kind.ERROR, msg);
                throw new IllegalStateException(msg);
            }
            // 只有一个source的情况下，source不带参数名，否则带
            String source = mapping.source();
            // target不带参数名
            String target = mapping.target();

            // 当前不支持级联属性
            if (target.split("\\.").length != 1) {
                String msg = String.format("the method[%s] of interface[%s] has illegal PropertyMapping annotation which has illegal target. The target must be like [propertyName]", methodName, interfaceName);
                messager.printMessage(Diagnostic.Kind.ERROR, msg);
                throw new IllegalStateException(msg);
            }

            if (sourceParameterDescriptorList.size() > 1) {
                if (StringHelper.isBlank(source) && !mapping.ignore()) {
                    String msg = String.format("the method[%s] of interface[%s] has illegal PropertyMapping annotation which has no source.", methodName, interfaceName);
                    messager.printMessage(Diagnostic.Kind.ERROR, msg);
                    throw new IllegalStateException(msg);
                }
                if (StringHelper.isBlank(target)) {
                    String msg = String.format("the method[%s] of interface[%s] has illegal PropertyMapping annotation which has no target.", methodName, interfaceName);
                    messager.printMessage(Diagnostic.Kind.ERROR, msg);
                    throw new IllegalStateException(msg);
                }
                // 当前不支持级联属性
                if (source.split("\\.").length != 2 && !mapping.ignore()) {
                    String msg = String.format("the method[%s] of interface[%s] has illegal PropertyMapping annotation which has illegal source. The source must be like [paramName.propertyName]", methodName, interfaceName);
                    messager.printMessage(Diagnostic.Kind.ERROR, msg);
                    throw new IllegalStateException(msg);
                } else {
                    // source必须是有效的属性名
                    String paramName = source.split("\\.")[0];
                    boolean matched = false;
                    for (ParamDescriptor paramDescriptor : sourceParameterDescriptorList) {
                        if (paramName.equals(paramDescriptor.paramName)) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched && !mapping.ignore()) {
                        String msg = String.format("the method[%s] of interface[%s] has illegal PropertyMapping annotation which has illegal source. The source must be like [paramName.propertyName] and the paramName must be equal the param name", methodName, interfaceName);
                        messager.printMessage(Diagnostic.Kind.ERROR, msg);
                        throw new IllegalStateException(msg);
                    }
                }
            } else {
                if (StringHelper.isBlank(target)) {
                    target = source;
                }
                if (StringHelper.isBlank(source)) {
                    source = target;
                }
                if (source.split("\\.").length != 1) {
                    String msg = String.format("the method[%s] of interface[%s] has illegal PropertyMapping annotation which has illegal source. The source must be like [propertyName]", methodName, interfaceName);
                    messager.printMessage(Diagnostic.Kind.ERROR, msg);
                    throw new IllegalStateException(msg);
                }
            }

            // 校验target的有效性
            if (!targetParameterDescriptorList.get(0).propertyDescriptorMap.containsKey(target)) {
                String msg = String.format("the method[%s] of interface[%s] has illegal PropertyMapping annotation, illegal target[%s] was found.", methodName, interfaceName, target);
                messager.printMessage(Diagnostic.Kind.ERROR, msg);
                throw new IllegalStateException(msg);
            }

            final String fullTarget = targetParameterDescriptorList.get(0).paramFullPath + "." + target;
            final String fullSource = sourceParameterDescriptorList.size() == 1 ? sourceParameterDescriptorList.get(0).paramFullPath + "." + source : source;

            if (targetPropertyMappingMap.putIfAbsent(fullTarget, new ParamPropertyMapping(fullSource, fullTarget, mapping.converter(), mapping.ignore())) != null) {
                String msg = String.format("the method[%s] of interface[%s] has illegal PropertyMapping annotation, duplicated target[%s] was found.", methodName, interfaceName, target);
                messager.printMessage(Diagnostic.Kind.ERROR, msg);
                throw new IllegalStateException(msg);
            }
        }

        // 获取MappingSource和MappingTarget

        methodBuilder.beginMethodBody()
            .addCode(b -> {
                ParamDescriptor tpd = targetParameterDescriptorList.get(0);
                if (!tpd.definedByReturnType) {
                    if (!tpd.isNullable()) {
                        codeForParamNotNull(b, tpd);
                    } else {
                        b.beginIf("$L == null", tpd.paramName)
                            .addStatement("return")
                            .endIf();
                    }
                }

                boolean[] allSourceNullable = new boolean[]{true};
                sourceParameterDescriptorList.forEach(spd -> {
                        if (!spd.isNullable()) {
                            codeForParamNotNull(b, spd);
                            allSourceNullable[0] = false;
                        }
                    }
                );

                if (allSourceNullable[0]) {
                    b.beginIf(sourceParameterDescriptorList.stream().map(spd -> spd.paramName + " == null").collect(Collectors.joining(" && ")))
                        .addCode(c -> {
                            if (tpd.definedByReturnType) {
                                c.addStatement("return null");
                            } else {
                                c.addStatement("return");
                            }
                        })
                        .endIf();
                }

                if (tpd.definedByReturnType) {
                    if (tpd.isArray()) {
                        ParamDescriptor spd = sourceParameterDescriptorList.get(0);
                        if (spd.isIterable()) {
                            b.addStatement("$T $L = new $T[$L.size()]", tpd.typeMirror, tpd.paramName, elementTypeMirror(tpd.typeMirror), spd.paramName);
                        } else if (spd.isArray()) {
                            b.addStatement("$T $L = new $T[$L.length]", tpd.typeMirror, tpd.paramName, elementTypeMirror(tpd.typeMirror), spd.paramName);
                        } else {
                            throw new IllegalStateException(String.format("The source parameter of method(%s) must be array or collection.", methodFullPath));
                        }
                    } else {
                        TypeName typeName = tpd.isAbstractClassOrInterface() ? concreteTypeName(tpd.typeMirror) : TypeNameHelper.ofTypeMirror(tpd.typeMirror);
                        if (typeName instanceof ParameterizedTypeName) {
                            b.addStatement("$T $L = new $T<>()", tpd.typeMirror, tpd.paramName, ((ParameterizedTypeName) typeName).rawType);
                        } else if (typeName instanceof ClassName) {
                            b.addStatement("$T $L = new $T()", tpd.typeMirror, tpd.paramName, tpd.typeMirror);
                        } else {
                            throw new IllegalStateException(String.format("The source parameter of method(%s) must be class or parameterized class.", methodFullPath));
                        }
                    }
                }

                sourceParameterDescriptorList.forEach(spd -> {
                    if (spd.isNullable() && sourceParameterDescriptorList.size() > 1) {
                        b.beginIf("$L != null", spd.paramName)
                            .addCode(c -> copyProperties(b, spd, tpd))
                            .endIf();
                    } else {
                        copyProperties(b, spd, tpd);
                    }
                });

                if (!TypeMirrorUtils.isVoid(returnTypeMirror)) {
                    b.addStatement("return $L", tpd.paramName);
                }

            })
            .end();

        return methodBuilder.build();
    }

    private boolean includeAllSourceProperties(ParamDescriptor paramDescriptor) {
        if (paramDescriptor.mappingSource == null) return true;
        return paramDescriptor.mappingSource.includeAll();
    }

    private boolean includeAllTargetProperties(ParamDescriptor paramDescriptor) {
        if (paramDescriptor.mappingTarget == null) return true;
        return paramDescriptor.mappingTarget.includeAll();
    }

    private TypeName concreteTypeName(TypeMirror typeMirror) {
        if (typeUtils.isSubtype(TypeMirrorUtils.classToTypeMirror(ArrayList.class, typeUtils, elementUtils), rawTypeMirror(typeMirror, typeUtils))) {
            return ParameterizedTypeName.of(ArrayList.class, WildcardTypeName.of());
        } else if (typeUtils.isSubtype(TypeMirrorUtils.classToTypeMirror(HashSet.class, typeUtils, elementUtils), rawTypeMirror(typeMirror, typeUtils))) {
            return ParameterizedTypeName.of(HashSet.class, WildcardTypeName.of());
        } else if (typeUtils.isSubtype(TypeMirrorUtils.classToTypeMirror(LinkedHashSet.class, typeUtils, elementUtils), rawTypeMirror(typeMirror, typeUtils))) {
            return ParameterizedTypeName.of(LinkedHashSet.class, WildcardTypeName.of());
        } else if (typeUtils.isSubtype(TypeMirrorUtils.classToTypeMirror(HashMap.class, typeUtils, elementUtils), rawTypeMirror(typeMirror, typeUtils))) {
            return ParameterizedTypeName.of(HashMap.class, WildcardTypeName.of());
        } else if (typeUtils.isSubtype(TypeMirrorUtils.classToTypeMirror(LinkedHashMap.class, typeUtils, elementUtils), rawTypeMirror(typeMirror, typeUtils))) {
            return ParameterizedTypeName.of(LinkedHashMap.class, WildcardTypeName.of());
        } else {
            throw new IllegalStateException(String.format("Can't find the concrete type for the typeMirror(%s)", typeMirror));
        }
    }


    private void copyProperties(ComplexJavaCodeBlockBuilder<MethodSpec.Builder> b, ParamDescriptor spd, ParamDescriptor tpd) {
        if (spd.isArrayOrIterable() && tpd.isArrayOrIterable()) {
            if (spd.isArray() && tpd.isArray()) {
                arrayToArray(b, spd, tpd);
            } else if (spd.isArray() && tpd.isIterable()) {
                arrayToIterable(b, spd, tpd);
            } else if (spd.isIterable() && tpd.isArray()) {
                iterableToArray(b, spd, tpd);
            } else {
                iterableToIterable(b, spd, tpd);
            }
        } else if (spd.isArrayOrIterable()) {
            messager.printMessage(Diagnostic.Kind.WARNING, String.format("The type of source(%s) is array or iterable object, but the target(%s) is not array or iterable object in the method(%s)",
                spd.paramFullPath, tpd.paramFullPath, methodFullPath));
        } else if (tpd.isArrayOrIterable()) {
            messager.printMessage(Diagnostic.Kind.WARNING, String.format("The type of target(%s) is array or iterable object, but the source(%s) is not array or iterable object in the method(%s)",
                tpd.paramFullPath, spd.paramFullPath, methodFullPath));
        } else if (spd.isMap() && tpd.isMap()) {
            mapToMap(b, spd, tpd);
        } else if (spd.isMap()) {
            mapEntriesToProperties(b, spd, tpd);
        } else if (tpd.isMap()) {
            propertiesToMapEntries(b, spd, tpd);
        } else {
            propertiesToProperties(b, spd, tpd);
        }
    }

    private void executePropertyConverter(IfFlow<ComplexJavaCodeBlockBuilder<MethodSpec.Builder>> bb, String initFormat, Converter converter, String source) {
        String format = String.format(initFormat, converter.format());
        List<Object> params = new ArrayList<>();
        for (ConverterParam p : converter.params()) {
            switch (p.type()) {
                case TYPE:
                    params.add(ClassName.ofGuess(argNotBlank(p.value(), "value")));
                    break;
                case SOURCE:
                    params.add(source);
                    break;
                case LITERAL:
                    params.add(argNotBlank(p.value(), "value"));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported ConverterParamType: " + p.type());
            }
        }
        bb.addStatement(format, params.toArray());
    }

    private void executeElementConverter(ForFlow<ComplexJavaCodeBlockBuilder<MethodSpec.Builder>> bb, String initFormat, String source, ParamDescriptor tpd) {
        ElementConverter elementConverter = findElementConverter();
        String format = String.format(initFormat, elementConverter.format());
        List<Object> params = new ArrayList<>();
        params.add(tpd.paramName);
        for (ConverterParam p : elementConverter.params()) {
            switch (p.type()) {
                case TYPE:
                    params.add(ClassName.ofGuess(argNotBlank(p.value(), "value")));
                    break;
                case SOURCE:
                    params.add(source);
                    break;
                case LITERAL:
                    params.add(argNotBlank(p.value(), "value"));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported ConverterParamType: " + p.type());
            }
            bb.addStatement(format, params.toArray());
        }
    }

    private void arrayToArray(ComplexJavaCodeBlockBuilder<MethodSpec.Builder> b, ParamDescriptor spd, ParamDescriptor tpd) {
        b.beginFor("int i = 0; i < $L.length; i++", spd.paramName)
            .addCode(bb -> executeElementConverter(bb, "$L[i] = %s", String.format("%s[i]", spd.paramName), tpd))
//            .addStatement("$L[i] = $L($L[i])", tpd.objectName, elementConverterMethodName, spd.objectName)
            .endFor();
    }

    private void arrayToIterable(ComplexJavaCodeBlockBuilder<MethodSpec.Builder> b, ParamDescriptor spd, ParamDescriptor tpd) {

        b.beginFor("int i = 0; i < $L.length; i++", spd.paramName)
            .addCode(bb -> executeElementConverter(bb, "$L.add(%s)", String.format("%s[i]", spd.paramName), tpd))
//            .addStatement("$L.add($L($L[i]))", tpd.objectName, elementConverterMethodName, spd.objectName)
            .endFor();
    }

    private void iterableToArray(ComplexJavaCodeBlockBuilder<MethodSpec.Builder> b, ParamDescriptor spd, ParamDescriptor tpd) {
        b.beginFor("int i = 0; i < $L.size(); i++", spd.paramName)
            .addCode(bb -> executeElementConverter(bb, "$L[i] = %s", String.format("%s.get(i)", spd.paramName), tpd))
//            .addStatement("$L[i] = $L($L.get(i))", tpd.objectName, elementConverterMethodName, spd.objectName)
            .endFor();
    }

    private void iterableToIterable(ComplexJavaCodeBlockBuilder<MethodSpec.Builder> b, ParamDescriptor spd, ParamDescriptor tpd) {
        TypeMirror elementTypeMirror = elementTypeMirror(spd.typeMirror);
        String elementName = elementName(elementTypeMirror);
        b.beginFor("$T $L : $L", elementTypeMirror, elementName, spd.paramName)
            .addCode(bb -> executeElementConverter(bb, "$L.add(%s)", elementName, tpd))
//            .addStatement("$L.add($L($L))", tpd.objectName, elementConverterMethodName, elementName)
            .endFor();
    }

    private String elementName(TypeMirror typeMirror) {
        TypeName typeName = TypeNameHelper.ofTypeMirror(typeMirror);
        if (typeName instanceof ClassName) {
            return StringHelper.unCapitalize(((ClassName) typeName).simpleName());
        } else if (typeName instanceof ParameterizedTypeName) {
            return StringHelper.unCapitalize(((ParameterizedTypeName) typeName).rawType.simpleName());
        } else {
            return "element";
        }
    }


    private void mapToMap(ComplexJavaCodeBlockBuilder<MethodSpec.Builder> b, ParamDescriptor spd, ParamDescriptor tpd) {
        MapMirror s = getMapMirror(typeUtils, spd.typeMirror);
        MapMirror t = getMapMirror(typeUtils, tpd.typeMirror);
        if (typeUtils.isAssignable(s.keyMirror, t.keyMirror) && typeUtils.isAssignable(s.valueMirror, t.valueMirror)) {
            b.addStatement("$L.putAll($L)", tpd.paramName, spd.paramName);
        } else {
            messager.printMessage(Diagnostic.Kind.WARNING, String.format("The type of source(%s) is incompatible with the type of target(%s) in the method(%s).", spd.paramFullPath, tpd.paramFullPath, methodFullPath));
        }
    }

    private void mapEntriesToProperties(ComplexJavaCodeBlockBuilder<MethodSpec.Builder> b, ParamDescriptor spd, ParamDescriptor tpd) {
        MapMirror s = getMapMirror(typeUtils, spd.typeMirror);
        if (!typeUtils.isAssignable(TypeMirrorUtils.classToTypeMirror(String.class, typeUtils, elementUtils), s.keyMirror)) {
            messager.printMessage(Diagnostic.Kind.WARNING, String.format("The type of the source(%s)'s key can't accept String in the method(%s).", spd.paramFullPath, methodFullPath));
            return;
        }
        tpd.propertyDescriptorMap.keySet().forEach(objectName -> {
            PropertyDescriptor t = tpd.propertyDescriptorMap.get(objectName);
            t.setterDescriptors.forEach(setterDescriptor -> {
                if (!typeUtils.isAssignable(s.valueMirror, setterDescriptor.typeMirror)) {
                    return;
                }
                switch (setterDescriptor.setAccessType) {
                    case PUBLIC_METHOD:
                        b.addStatement("$L.$L($L.get($S))", tpd.paramName, setterDescriptor.setterName, spd.paramName, t.propertyNam);
                        break;
                    case PUBLIC_FIEld:
                        b.addStatement("$L.$L = $L.get($S)", tpd.paramName, setterDescriptor.setterName, spd.paramName, t.propertyNam);
                        break;
                    default:
                        throw new IllegalStateException("Unsupported AccessType: " + setterDescriptor.setAccessType.name());
                }
            });
        });
    }

    private void propertiesToMapEntries(ComplexJavaCodeBlockBuilder<MethodSpec.Builder> b, ParamDescriptor spd, ParamDescriptor tpd) {
        MapMirror t = getMapMirror(typeUtils, tpd.typeMirror);
        if (!typeUtils.isAssignable(TypeMirrorUtils.classToTypeMirror(String.class, typeUtils, elementUtils), t.keyMirror)) {
            messager.printMessage(Diagnostic.Kind.WARNING, String.format("The type of the target(%s)'s key can't accept String in the method(%s).", spd.paramFullPath, methodFullPath));
            return;
        }
        spd.propertyDescriptorMap.keySet().forEach(objectName -> {
            PropertyDescriptor s = spd.propertyDescriptorMap.get(objectName);
            s.getterDescriptors.forEach(getterDescriptor -> {
                if (!typeUtils.isAssignable(getterDescriptor.typeMirror, t.valueMirror)) {
                    return;
                }
                switch (getterDescriptor.getAccessType) {
                    case PUBLIC_METHOD:
                        b.addStatement("$L.put($S, $L.$L())", tpd.paramName, s.propertyNam, spd.paramName, getterDescriptor.getterName);
                        break;
                    case PUBLIC_FIEld:
                        b.addStatement("$L.put($S, $L.$L)", tpd.paramName, s.propertyNam, spd.paramName, getterDescriptor.getterName);
                        break;
                    default:
                        throw new IllegalStateException("Unsupported AccessType: " + getterDescriptor.getAccessType.name());
                }
            });
        });
    }

    private void propertiesToProperties(ComplexJavaCodeBlockBuilder<MethodSpec.Builder> b, ParamDescriptor spd, ParamDescriptor tpd) {
        tpd.propertyDescriptorMap.keySet().forEach(objectName -> {
            PropertyDescriptor t = tpd.propertyDescriptorMap.get(objectName);
            PropertyCopySetUp found = find(spd, tpd, objectName, t);
            if (found == null) return;
            GetterDescriptor getterDescriptor = found.getterDescriptor;
            SetterDescriptor setterDescriptor = found.setterDescriptor;
            Converter converter = found.converter;
            switch (setterDescriptor.setAccessType) {
                case PUBLIC_METHOD:
                    switch (getterDescriptor.getAccessType) {
                        case PUBLIC_METHOD:
                            if (converter == null) {
                                b.addStatement("$L.$L($L.$L())", tpd.paramName, setterDescriptor.setterName, spd.paramName, getterDescriptor.getterName);
                            } else {
                                String source = String.format("%s.%s()", spd.paramName, getterDescriptor.getterName);
                                String target = String.format("%s.%s", tpd.paramName, setterDescriptor.setterName);
                                b.beginIf("$L.$L() != null", spd.paramName, getterDescriptor.getterName)
                                    .addCode(bb -> executePropertyConverter(bb, target + "(%s)", converter, source))
                                    .endIf();
                            }
                            break;
                        case PUBLIC_FIEld:
                            if (converter == null) {
                                b.addStatement("$L.$L($L.$L)", tpd.paramName, setterDescriptor.setterName, spd.paramName, getterDescriptor.getterName);
                            } else {
                                String source = String.format("%s.%s", spd.paramName, getterDescriptor.getterName);
                                String target = String.format("%s.%s", tpd.paramName, setterDescriptor.setterName);
                                b.beginIf("$L.$L() != null", spd.paramName, getterDescriptor.getterName)
                                    .addCode(bb -> executePropertyConverter(bb, target + "(%s)", converter, source))
                                    .endIf();
                            }
                            break;
                        default:
                            throw new IllegalStateException("Unsupported AccessType: " + getterDescriptor.getAccessType.name());
                    }
                    break;
                case PUBLIC_FIEld:
                    switch (getterDescriptor.getAccessType) {
                        case PUBLIC_METHOD:
                            if (converter == null) {
                                b.addStatement("$L.$L = $L.$L()", tpd.paramName, setterDescriptor.setterName, spd.paramName, getterDescriptor.getterName);
                            } else {
                                String source = String.format("%s.%s()", spd.paramName, getterDescriptor.getterName);
                                String target = String.format("%s.%s = ", tpd.paramName, setterDescriptor.setterName);
                                b.beginIf("$L.$L() != null", spd.paramName, getterDescriptor.getterName)
                                    .addCode(bb -> executePropertyConverter(bb, target + "%s", converter, source))
                                    .endIf();
                            }
                            break;
                        case PUBLIC_FIEld:
                            if (converter == null) {
                                b.addStatement("$L.$L = $L.$L", tpd.paramName, setterDescriptor.setterName, spd.paramName, getterDescriptor.getterName);
                            } else {
                                String source = String.format("%s.%s", spd.paramName, getterDescriptor.getterName);
                                String target = String.format("%s.%s = ", tpd.paramName, setterDescriptor.setterName);
                                b.beginIf("$L.$L != null", spd.paramName, getterDescriptor.getterName)
                                    .addCode(bb -> executePropertyConverter(bb, target + "%s", converter, source))
                                    .endIf();
                            }
                            break;
                        default:
                            throw new IllegalStateException("Unsupported AccessType: " + getterDescriptor.getAccessType.name());
                    }
                    break;
                default:
                    throw new IllegalStateException("Unsupported AccessType: " + setterDescriptor.setAccessType.name());
            }
        });
    }

    private String parsePropertyName(String paramProperTyName) {
        String[] strArray = paramProperTyName.split("\\.");
        if (strArray.length > 1) return strArray[1];
        return paramProperTyName;
    }

    private String parseParamName(String paramProperTyName) {
        String[] strArray = paramProperTyName.split("\\.");
        if (strArray.length > 1) return strArray[0];
        return null;
    }


    private @Nullable PropertyCopySetUp find(ParamDescriptor spd, ParamDescriptor tpd, String objectName, PropertyDescriptor t) {

        PropertyDescriptor s = null;
        ParamPropertyMapping mapping = targetPropertyMappingMap.get(t.fullPath);
        if (mapping != null) {
            if (mapping.ignore) return null; //target指向ignore，无法映射
            String paramName = parseParamName(mapping.sourceParamPropertyName);
            if (StringHelper.isNotBlank(paramName) && !spd.paramName.equals(paramName)) return null; // 参数名不匹配，无法映射

            String sourceParamPropertyName = parsePropertyName(mapping.sourceParamPropertyName);
            if ((s = spd.propertyDescriptorMap.get(sourceParamPropertyName)) == null) { // 参数没有任何属性匹配mapping指定的source
                String msg = String.format("The method[%s] has illegal PropertyMapping annotation ,Illegal source[%s] was found.", spd.methodFullPath, mapping.sourceParamPropertyName);
                messager.printMessage(Diagnostic.Kind.ERROR, msg);
                throw new IllegalStateException(msg);
            }
        } else {
            if (includeAllSourceProperties(spd) && includeAllTargetProperties(tpd)) s = spd.propertyDescriptorMap.get(objectName);
        }

        if (s != null) {
            for (GetterDescriptor getterDescriptor : s.getterDescriptors) {
                for (SetterDescriptor setterDescriptor : t.setterDescriptors) {
                    if (mapping != null && mapping.converter != null) {
                        return new PropertyCopySetUp(getterDescriptor, setterDescriptor, mapping.converter);
                    }
                    if (typeUtils.isAssignable(setterDescriptor.typeMirror, getterDescriptor.typeMirror)) {
                        return new PropertyCopySetUp(getterDescriptor, setterDescriptor);
                    }
                }
            }
        }
        return null;
    }

    private void codeForParamNotNull(ComplexJavaCodeBlockBuilder<MethodSpec.Builder> b, ParamDescriptor paramDescriptor) {
        if (!paramDescriptor.isNullable()) {
            b.beginIf("$L == null", paramDescriptor.paramName)
                .addStatement("throw new $T($S)", IllegalArgumentException.class, String.format("The parameter('%s') in the method(%s) can't be null.", paramDescriptor.paramFullPath, methodFullPath))
                .endIf();
        }
    }

    private ElementConverter findElementConverter() {
        return Optional.ofNullable(this.executableElement.getAnnotation(ElementConverter.class))
            .orElseThrow(() -> new IllegalStateException(String.format("The converter method used between datasets in the method(%s) isn't defined!", methodFullPath)));
    }
}
