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

import glz.hawk.bean.mapper.annotation.BeanMapper;
import glz.hawk.codepoet.java.AnnotationInstanceSpec;
import glz.hawk.codepoet.java.ClassSpec;
import glz.hawk.codepoet.java.JavaFile;
import glz.hawk.codepoet.java.type.ClassName;
import glz.hawk.codepoet.java.type.TypeNameHelper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * This class is responsible for
 *
 * @author Hawk
 */
@SupportedAnnotationTypes("glz.hawk.bean.mapper.annotation.BeanMapper")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BeanMapperAnnotationProcessor extends AbstractProcessor {

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
        messager.printMessage(Diagnostic.Kind.NOTE, "Start to Execute BeanMapperAnnotationProcessor");
        roundEnv.getElementsAnnotatedWith(BeanMapper.class)
            .stream()
            .map(e -> (TypeElement) e)
            .forEach(typeElement -> {
                if (typeElement.getKind() != ElementKind.INTERFACE) {
                    throw new IllegalStateException("Only support interface. Current type is " + typeElement);
                }

                if (TypeNameHelper.isInnerClass(typeElement)) {
                    throw new IllegalStateException("Inner class is unsupported. Current type is " + typeElement);
                }

                BeanMapper objectMapper = typeElement.getAnnotation(BeanMapper.class);

                final String mapperClassPackage = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                final String mapperClassName = typeElement.getSimpleName().toString();

                final String mapperImplClassPackage = ClassUtils.instance(ClassUtils.getFunctionClass(objectMapper::implClassPackageGeneratorClass)).apply(mapperClassPackage);
                final String mapperImplClassName = ClassUtils.instance(ClassUtils.getFunctionClass(objectMapper::implClassNameGeneratorClass)).apply(mapperClassName);

                ClassSpec.Builder builder = ClassSpec.builder(mapperImplClassName, Modifier.PUBLIC)
                    .addSuperInterface(ClassName.of(typeElement));

                switch (objectMapper.componentModel()) {
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
                        throw new IllegalStateException("Unsupported ComponentMode: " + objectMapper.componentModel().name());
                }

                typeElement.getEnclosedElements()
                    .stream()
                    .filter(e -> e.getKind() == ElementKind.METHOD)
                    .map(e -> (ExecutableElement) e)
                    .map(e -> new MethodBuilder(typeUtils, elementUtils, messager, mapperClassName, e))
                    .map(MethodBuilder::build)
                    .forEach(builder::addMethod);

                JavaFile javaFile = JavaFile.builder(mapperImplClassPackage, builder.build()).build();
                javaFile.writeTo(filer);
            });

        messager.printMessage(Diagnostic.Kind.NOTE, "Finished BeanMapperAnnotationProcessor...");
        return true;
    }
}
