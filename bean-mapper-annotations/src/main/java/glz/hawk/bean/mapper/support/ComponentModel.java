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
package glz.hawk.bean.mapper.support;

/**
 * This enum is responsible for
 *
 * @author Hawk
 */
public enum ComponentModel {

    /**
     * The generated mapper is annotated with nothing.
     */
    DEFAULT,

    /**
     * The generated mapper is annotated with {@code @Component} in {@code Spring}, and can be retrieved via {@code @Autowired}
     */
    SPRING,

    /**
     * The generated mapper is annotated with @Named and @Singleton in {@code JSR-330}, and can be retrieved via @Inject.
     */
    JSR330,

    /**
     * The generated mapper is annotated with @Named and @Singleton in {@code JAKARTA}, and can be retrieved via @Inject.
     */
    JAKARTA,

    /**
     * The generated mapper is annotated with {@code @ApplicationScoped} in {@code JSR-365} and can be retrieved via @Inject.
     */
    CDI,

    /**
     * The generated mapper is annotated with {@code @ApplicationScoped} in {@code JAKARTA CDI} and can be retrieved via @Inject.
     */
    JAKARTA_CDI
}
