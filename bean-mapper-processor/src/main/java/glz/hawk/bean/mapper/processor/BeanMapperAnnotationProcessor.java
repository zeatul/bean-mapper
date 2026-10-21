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

import glz.hawk.bean.mapper.annotation.AutowiredBean;
import glz.hawk.bean.mapper.annotation.BeanMapper;
import glz.hawk.codepoet.java.AnnotationInstanceSpec;
import glz.hawk.codepoet.java.ClassSpec;
import glz.hawk.codepoet.java.FieldSpec;
import glz.hawk.codepoet.java.JavaFile;
import glz.hawk.codepoet.java.type.ClassName;
import glz.hawk.codepoet.java.type.ParameterizedTypeName;
import glz.hawk.codepoet.java.type.TypeName;
import glz.hawk.codepoet.java.type.TypeNameHelper;
import glz.hawk.codepoet.java.type.TypeVariableName;
import glz.hawkframework.core.helper.StringHelper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for
 *
 * @author Hawk
 */
@SupportedAnnotationTypes("glz.hawk.bean.mapper.annotation.BeanMapper")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BeanMapperAnnotationProcessor extends AbstractProcessor {

    private static final ClassName SPRING_AUTOWIRED = ClassName.of("org.springframework.beans.factory.annotation", "Autowired");
    private static final ClassName SPRING_QUALIFIER = ClassName.of("org.springframework.beans.factory.annotation", "Qualifier");

    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.typeUtils = processingEnv.getTypeUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.errorRaised()) {
            return true;
        }
        roundEnv.getElementsAnnotatedWith(BeanMapper.class)
            .stream()
            .map(e -> (TypeElement) e)
            .forEach(typeElement -> {
                if (typeElement.getKind() != ElementKind.INTERFACE) {
                    String msg = "Only support interface. Current type is " + typeElement;
                    messager.printMessage(Diagnostic.Kind.ERROR, msg);
                    throw new IllegalStateException(msg);
                }

                if (TypeNameHelper.isInnerClass(typeElement)) {
                    String msg = "Inner class is unsupported. Current type is " + typeElement;
                    messager.printMessage(Diagnostic.Kind.ERROR, msg);
                    throw new IllegalStateException(msg);
                }

                BeanMapper beanMapper = typeElement.getAnnotation(BeanMapper.class);

                final String mapperClassPackage = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                final String mapperClassName = typeElement.getSimpleName().toString();

                final String mapperImplClassPackage = ClassUtils.instance(ClassUtils.getFunctionClass(beanMapper::implClassPackageGeneratorClass)).apply(mapperClassPackage);
                final String mapperImplClassName = ClassUtils.instance(ClassUtils.getFunctionClass(beanMapper::implClassNameGeneratorClass)).apply(mapperClassName);

                ClassSpec.Builder builder = ClassSpec.builder(mapperImplClassName, Modifier.PUBLIC)
                    .addSuperInterface(superInterfaceName(typeElement));
                // 复制接口的类型参数到实现类
                for (TypeParameterElement typeParameter : typeElement.getTypeParameters()) {
                    builder.addTypeVariable(TypeVariableName.of(typeParameter.getSimpleName().toString()));
                }

                switch (beanMapper.componentModel()) {
                    case DEFAULT:
                        // do nothing
                        break;
                    case SPRING:
                        builder.addAnnotation(AnnotationInstanceSpec.builder(ClassName.of("org.springframework.stereotype", "Component")).build());
                        break;
                    case JSR330:
                        builder.addAnnotation(AnnotationInstanceSpec.builder(ClassName.of("javax.inject", "Singleton")).build());
                        builder.addAnnotation(AnnotationInstanceSpec.builder(ClassName.of("javax.inject", "Named")).build());
                        break;
                    case CDI:
                        builder.addAnnotation(AnnotationInstanceSpec.builder(ClassName.of("javax.enterprise.context", "ApplicationScoped")).build());
                        break;
                    case JAKARTA:
                        builder.addAnnotation(AnnotationInstanceSpec.builder(ClassName.of("jakarta.inject", "Singleton")).build());
                        builder.addAnnotation(AnnotationInstanceSpec.builder(ClassName.of("jakarta.inject", "Named")).build());
                        break;
                    case JAKARTA_CDI:
                        builder.addAnnotation(AnnotationInstanceSpec.builder(ClassName.of("jakarta.enterprise.context", "ApplicationScoped")).build());
                        break;
                    default:
                        String msg = "Unsupported ComponentMode: " + beanMapper.componentModel().name();
                        messager.printMessage(Diagnostic.Kind.ERROR, msg);
                        throw new IllegalStateException(msg);
                }

                // 处理接口声明的方法，包括从父接口继承的方法（类型参数会被替换为接口的实际类型参数）
                DeclaredType mapperType = (DeclaredType) typeElement.asType();
                TypeElement objectElement = elementUtils.getTypeElement(Object.class.getCanonicalName());

                elementUtils.getAllMembers(typeElement)
                    .stream()
                    .filter(e -> e.getKind() == ElementKind.METHOD)
                    .map(e -> (ExecutableElement) e)
                    .filter(e -> !(e.getModifiers().contains(Modifier.STATIC) || e.isDefault()))
                    .filter(e -> objectElement == null || !objectElement.equals(e.getEnclosingElement()))
                    .map(e -> new MethodBuilder(typeUtils, elementUtils, messager, mapperClassName, e,
                        (ExecutableType) typeUtils.asMemberOf(mapperType, e)))
                    .map(MethodBuilder::build)
                    .forEach(builder::addMethod);

                // 注入bean
                for (AutowiredBean autowiredBean : beanMapper.autowiredBeans()) {
                    builder.addField(buildAutowiredBeanField(autowiredBean));
                }

                // 静态引用
                for (String staticImport : beanMapper.staticImports()) {
                    if (StringHelper.isEmpty(staticImport)) {
                        String msg = "Static Import is empty";
                        messager.printMessage(Diagnostic.Kind.ERROR, msg);
                        throw new IllegalStateException(msg);
                    }
                    builder.addStaticImport(staticImport);
                }

                JavaFile javaFile = JavaFile.builder(mapperImplClassPackage, builder.build()).build();
                javaFile.writeTo(filer);
            });

        return true;
    }

    /**
     * 构造参数化的父接口类型名称，例如 {@code BaseMapper<BookPo, BookUpdate>}
     */
    private TypeName superInterfaceName(TypeElement typeElement) {
        List<? extends TypeParameterElement> typeParameters = typeElement.getTypeParameters();
        if (typeParameters.isEmpty()) {
            return ClassName.of(typeElement);
        }
        List<TypeName> typeArguments = new ArrayList<>();
        for (TypeParameterElement typeParameter : typeParameters) {
            typeArguments.add(TypeVariableName.of(typeParameter.getSimpleName().toString()));
        }
        return ParameterizedTypeName.of(ClassName.of(typeElement), typeArguments);
    }

    FieldSpec buildAutowiredBeanField(AutowiredBean autowiredBean) {
        ClassName className = ClassName.ofGuess(autowiredBean.type());
        String fieldName = StringHelper.isNotBlank(autowiredBean.fieldName()) ? autowiredBean.fieldName()
            : StringHelper.unCapitalize(className.simpleName());
        FieldSpec.Builder builder = FieldSpec.builder(className, fieldName, Modifier.PRIVATE);
        builder.addAnnotation(AnnotationInstanceSpec.builder(SPRING_AUTOWIRED).build());
        String[] qualifiers = autowiredBean.qualifier();
        if (qualifiers.length > 1) {
            // Spring的@Qualifier不是可重复注解，同一字段上生成多个@Qualifier会导致编译失败
            String msg = String.format("The autowired bean(%s) can't be annotated with more than one @Qualifier.", autowiredBean.type());
            messager.printMessage(Diagnostic.Kind.ERROR, msg);
            throw new IllegalStateException(msg);
        }
        for (String qualifier : qualifiers) {
            builder.addAnnotation(AnnotationInstanceSpec.builder(SPRING_QUALIFIER).addMember("value", qualifier).build());
        }
        return builder.build();

    }
}
