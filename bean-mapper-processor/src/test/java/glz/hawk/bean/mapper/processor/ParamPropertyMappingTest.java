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
import glz.hawk.bean.mapper.annotation.ConverterParam;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;

class ParamPropertyMappingTest {

    private static final Converter BLANK_CONVERTER = new Converter() {
        @Override
        public String format() {
            return "";
        }

        @Override
        public ConverterParam[] params() {
            return new ConverterParam[0];
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Converter.class;
        }
    };

    private static final Converter FORMAT_CONVERTER = new Converter() {
        @Override
        public String format() {
            return "$L.format($T.ofPattern($S))";
        }

        @Override
        public ConverterParam[] params() {
            return new ConverterParam[0];
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Converter.class;
        }
    };

    @Test
    void blankFormatConverterIsTreatedAsAbsent() {
        ParamPropertyMapping mapping = new ParamPropertyMapping("src.a", "tgt.a", BLANK_CONVERTER, false);

        assertThat(mapping.sourceParamPropertyName).isEqualTo("src.a");
        assertThat(mapping.targetParamPropertyName).isEqualTo("tgt.a");
        assertThat(mapping.ignore).isFalse();
        assertThat(mapping.converter).isNull();
    }

    @Test
    void formatConverterIsKept() {
        ParamPropertyMapping mapping = new ParamPropertyMapping("src.a", "tgt.a", FORMAT_CONVERTER, true);

        assertThat(mapping.ignore).isTrue();
        assertThat(mapping.converter).isSameAs(FORMAT_CONVERTER);
    }
}
