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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is responsible for
 *
 * @author Hawk
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface ConverterParam {
    ConverterParamType type();

    /**
     * If the value of {@link #type()} is {@link ConverterParamType#TYPE TYPE}, fill in the fully qualified class name for value.
     * <p>If the value of {@link #type()} is {@link ConverterParamType#SOURCE SOURCE}, leave the value field empty.
     * <p>If the value of {@link #type()} is {@link ConverterParamType#LITERAL LITERAL}, fill in the required parameter value, such as a date formatting pattern.
     */
    String value() default "";
}
