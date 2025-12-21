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
 * This annotation is responsible for providing some mapping rules.
 * <p>Only one parameter can be annotated by {@link MappingTarget}.</p>
 * <p>The parameter annotated by {@link MappingTarget} shouldn't be annotated by {@link MappingSource}.</p>
 *
 *
 * @author Zhang Peng
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER,ElementType.METHOD})
public @interface MappingTarget {

    /**
     * This attribute is used to define the return object name while the method is annotated with {@code @MappingTarget}.
     */
    String returnObjectName() default "";

    boolean nullable() default false;
}
