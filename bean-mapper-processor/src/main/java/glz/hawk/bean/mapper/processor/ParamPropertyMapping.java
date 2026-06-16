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
import glz.hawkframework.core.helper.StringHelper;

/**
 * This class is responsible for
 * <p>定义参数之间的属性映射</p>
 *
 * @author Zhang Peng
 */
public class ParamPropertyMapping {

    /**
     * 全路径，包括对应的方法参数名
     */
    public final String sourceParamPropertyName;

    /**
     * 全路径，包括对应的方法参数名或特定返回对象名
     */
    public final String targetParamPropertyName;

    /**
     * 是否忽略映射
     */
    public final boolean ignore;

    public final Converter converter;

    public ParamPropertyMapping(String sourceParamPropertyName, String targetParamPropertyName, Converter converter, boolean ignore) {
        this.sourceParamPropertyName = sourceParamPropertyName;
        this.targetParamPropertyName = targetParamPropertyName;
        this.converter = StringHelper.isBlank(converter.format()) ? null : converter;
        this.ignore = ignore;
    }

}
