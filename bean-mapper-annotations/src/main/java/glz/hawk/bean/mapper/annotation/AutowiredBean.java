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
 * 这个注解用来注入bean供转换代码引用
 * <p>生成的实现类中的bean通过Spring的@Autowired注入，因此生成的实现类需要由Spring容器管理才能完成注入</p>
 *
 * @author Zhang Peng
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface AutowiredBean {
    /**
     * 注入的bean的类的全名
     */
    String type() default "";

    /**
     * 对应于Spring Framework的@Qualifier注解
     * <p>最多只能指定一个，因为Spring的@Qualifier不是可重复注解，同一字段上生成多个@Qualifier会导致编译失败</p>
     */
    String[] qualifier() default {};

    /**
     * 注入的bean对应的field名称，不填取小写首字母的类名
     */
    String fieldName() default "";
}
