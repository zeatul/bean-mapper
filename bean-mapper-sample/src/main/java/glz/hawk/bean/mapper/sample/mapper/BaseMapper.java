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

package glz.hawk.bean.mapper.sample.mapper;

import glz.hawk.bean.mapper.annotation.MappingTarget;

/**
 * 泛型基接口，验证从父接口继承的方法也会生成实现，且类型参数会被替换为子接口的实际类型参数
 *
 * @author Hawk
 */
public interface BaseMapper<S, T> {

    /**
     * 继承的方法，类型参数S和T在子接口中被替换为实际类型
     */
    T convert(S source);

    void copy(S source, @MappingTarget T target);
}
