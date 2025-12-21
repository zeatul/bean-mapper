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

package glz.hawk.bean.mapper.annotation;


import glz.hawk.bean.mapper.generate.DefaultImplClassNameGenerator;
import glz.hawk.bean.mapper.generate.DefaultImplClassPackageGenerator;
import glz.hawk.bean.mapper.support.ComponentModel;

import java.lang.annotation.*;
import java.util.function.Function;

/**
 * This annotation is responsible for
 *
 * @author Zhang Peng
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface BeanMapper {

    /**
     * If the value of this attribute is {@code true}, the field copying will be executed recursively
     */
    boolean deepCopy() default false;

    /**
     * The instance of {@code implClassPackageGeneratorClass} will be used to generate the package of mapper implementation class.
     */
    Class<? extends Function<String, String>> implClassPackageGeneratorClass() default DefaultImplClassPackageGenerator.class;

    /**
     * The instance of {@code implClassNameGeneratorClass} will be used to generate the name of mapper implementation class.
     */
    Class<? extends Function<String, String>> implClassNameGeneratorClass() default DefaultImplClassNameGenerator.class;

    /**
     * Specifies the component model to which the generated mapper should
     * adhere. Supported values are
     * <ul>
     * <li> {@code DEFAULT}: the mapper uses no component model</li>
     * <li>
     * {@code cdi}: the generated mapper is an application-scoped CDI bean and
     * can be retrieved via {@code @Inject}</li>
     * <li>
     * {@code spring}: the generated mapper is a Spring bean and
     * can be retrieved via {@code @Autowired}</li>
     * <li>
     * {@code jsr330}: the generated mapper is annotated with {@code @javax.inject.Named} and
     * {@code @Singleton}, and can be retrieved via {@code @Inject}.
     * The annotations will either be from javax.inject or jakarta.inject,
     * depending on which one is available, with javax.inject having precedence.</li>
     * <li>
     * {@code jakarta}: the generated mapper is annotated with {@code @jakarta.inject.Named} and
     * {@code @Singleton}, and can be retrieved via {@code @Inject}.</li>
     * </ul>
     *
     * @return The component model for the generated mapper.
     */
    ComponentModel componentModel() default ComponentModel.DEFAULT;

    /**
     * This attribute provides the static imports for the code generator, If the code use static method to convert the source to the target.
     */
    String[] staticImports() default{};
}
