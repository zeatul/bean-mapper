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

import java.lang.annotation.*;

/**
 * This annotation is responsible for
 *
 * @author Hawk
 */
@Repeatable(PropertyMapping.PropertyMappings.class)
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface PropertyMapping {


    /**
     * The source name of the configured property as defined by the JavaBeans specification.
     * <p>This may either be a simple property name (e.g. "address") or a dot-separated property path (e.g. "address.city"
     * or "address.city.name").</p>
     * <p>In case the annotated method has several source parameters,
     * the property name must be qualified with the parameter name, e.g. "addressParam.city".</p>
     * <p>If this attribute's value is empty, use the value of {@link #target()}</p>
     */
    String source() default "";

    /**
     * The target name of the configured property as defined by the JavaBeans specification.
     * <p>p>The same target property must not be mapped more than once.</p>
     */
    String target() default "";

    /**
     * This attribute is used to construct a Java statement that converts the source value into the value required by the target.
     * <p>Especially when the source type and target type are incompatible. <p/>
     */
    Converter converter() default @Converter();

    /**
     * Whether the property specified via {@link #target()} should be ignored by the generated mapping method or not.
     */
    boolean ignore() default false;

    // 容器注解
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    @interface PropertyMappings {
        PropertyMapping[] value();
    }
}
