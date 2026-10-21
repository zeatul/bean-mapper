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

package glz.hawk.bean.mapper.generate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultImplClassNameGeneratorTest {

    @Test
    void appendsImplSuffix() {
        DefaultImplClassNameGenerator generator = new DefaultImplClassNameGenerator();
        assertThat(generator.apply("UserMapper")).isEqualTo("UserMapperImpl");
        assertThat(generator.apply("BookMapper")).isEqualTo("BookMapperImpl");
    }

    @Test
    void rejectsBlankClassName() {
        DefaultImplClassNameGenerator generator = new DefaultImplClassNameGenerator();
        assertThatThrownBy(() -> generator.apply(""))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> generator.apply("   "))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> generator.apply(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
