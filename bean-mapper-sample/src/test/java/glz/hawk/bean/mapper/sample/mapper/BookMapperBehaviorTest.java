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

import glz.hawk.bean.mapper.sample.beans.BookExPo;
import glz.hawk.bean.mapper.sample.beans.BookPo;
import glz.hawk.bean.mapper.sample.beans.BookUpdate;
import glz.hawk.bean.mapper.sample.mapper.impl.BookMapperImpl;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Behavior tests for generated BookMapperImpl.
 * These tests verify the runtime behavior of the generated mapping code.
 */
class BookMapperBehaviorTest {

    private final BookMapper mapper = new BookMapperImpl();

    @Test
    void convert1_shouldMapAllProperties() {
        BookPo source = createBookPo();

        BookUpdate result = mapper.convert1(source);

        assertThat(result).isNotNull();
        assertThat(result.getBookId()).isEqualTo(source.getBookId());
        assertThat(result.getBookName()).isEqualTo(source.getBookName());
        assertThat(result.getPublishDate()).isEqualTo(source.getPublishDate());
        assertThat(result.getRecordVersion()).isEqualTo(source.getRecordVersion());
    }

    @Test
    void convert1_1_shouldIgnoreSpecifiedProperties() {
        BookPo source = createBookPo();

        BookUpdate result = mapper.convert1_1(source);

        assertThat(result).isNotNull();
        // bookId and bookName are ignored
        assertThat(result.getBookId()).isNull();
        assertThat(result.getBookName()).isNull();
        assertThat(result.getPublishDate()).isEqualTo(source.getPublishDate());
        assertThat(result.getRecordVersion()).isEqualTo(source.getRecordVersion());
    }

    @Test
    void convert1_2_shouldMapOnlySpecifiedPropertiesWithIncludeAllFalse() {
        BookPo source = createBookPo();
        BookUpdate target = new BookUpdate();

        BookUpdate result = mapper.convert1_2(source, target);

        assertThat(result).isSameAs(target);
        // includeAll=false, only bookId is mapped (bookName has ignore=true)
        assertThat(result.getBookId()).isEqualTo(source.getBookId());
        assertThat(result.getBookName()).isNull(); // not mapped due to includeAll=false
        assertThat(result.getPublishDate()).isNull();
        assertThat(result.getRecordVersion()).isNull();
    }

    @Test
    void convert1_3_shouldMapOnlySpecifiedPropertiesWithIncludeAllFalseOnSource() {
        BookPo source = createBookPo();

        BookUpdate result = mapper.convert1_3(source);

        assertThat(result).isNotNull();
        // includeAll=false on source, only bookId is mapped (bookName has ignore=true)
        assertThat(result.getBookId()).isEqualTo(source.getBookId());
        assertThat(result.getBookName()).isNull();
        assertThat(result.getPublishDate()).isNull();
        assertThat(result.getRecordVersion()).isNull();
    }

    @Test
    void convert2_shouldHandleNullableSource() {
        BookUpdate result = mapper.convert2(null);
        assertThat(result).isNull();

        BookPo source = createBookPo();
        result = mapper.convert2(source);
        assertThat(result).isNotNull();
        assertThat(result.getBookId()).isEqualTo(source.getBookId());
    }

    @Test
    void convert9_shouldConvertArrayToList() {
        BookPo[] sources = {createBookPo(BigInteger.ONE, "Book 1"), createBookPo(BigInteger.valueOf(2), "Book 2")};

        List<BookUpdate> result = mapper.convert9(sources);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBookId()).isEqualTo(BigInteger.ONE);
        assertThat(result.get(0).getBookName()).isEqualTo("Book 1");
        assertThat(result.get(1).getBookId()).isEqualTo(BigInteger.valueOf(2));
        assertThat(result.get(1).getBookName()).isEqualTo("Book 2");
    }

    @Test
    void convert10_shouldConvertArrayToArray() {
        BookPo[] sources = {createBookPo(BigInteger.ONE, "Book 1"), createBookPo(BigInteger.valueOf(2), "Book 2")};

        BookUpdate[] result = mapper.convert10(sources);

        assertThat(result).hasSize(2);
        assertThat(result[0].getBookId()).isEqualTo(BigInteger.ONE);
        assertThat(result[0].getBookName()).isEqualTo("Book 1");
        assertThat(result[1].getBookId()).isEqualTo(BigInteger.valueOf(2));
        assertThat(result[1].getBookName()).isEqualTo("Book 2");
    }

    @Test
    void convert11_shouldConvertListToArray() {
        List<BookPo> sources = Arrays.asList(createBookPo(BigInteger.ONE, "Book 1"), createBookPo(BigInteger.valueOf(2), "Book 2"));

        BookUpdate[] result = mapper.convert11(sources);

        assertThat(result).hasSize(2);
        assertThat(result[0].getBookId()).isEqualTo(BigInteger.ONE);
        assertThat(result[0].getBookName()).isEqualTo("Book 1");
        assertThat(result[1].getBookId()).isEqualTo(BigInteger.valueOf(2));
        assertThat(result[1].getBookName()).isEqualTo("Book 2");
    }

    @Test
    void convert12_shouldUseCustomConverter() {
        BookExPo source = new BookExPo();
        source.fieldDate = LocalDate.of(2024, 6, 15);

        BookUpdate result = mapper.convert12(source);

        assertThat(result).isNotNull();
        // Converter just passes the field value directly ($L)
        assertThat(result.getPublishDate()).isEqualTo(LocalDate.of(2024, 6, 15));
    }

    @Test
    void copy1_shouldCopyAllProperties() {
        BookPo source = createBookPo();
        BookUpdate target = new BookUpdate();

        mapper.copy1(source, target);

        assertThat(target.getBookId()).isEqualTo(source.getBookId());
        assertThat(target.getBookName()).isEqualTo(source.getBookName());
        assertThat(target.getPublishDate()).isEqualTo(source.getPublishDate());
        assertThat(target.getRecordVersion()).isEqualTo(source.getRecordVersion());
    }

    @Test
    void copy1_1_shouldIgnoreSpecifiedProperties() {
        BookPo source = createBookPo();
        BookUpdate target = new BookUpdate();

        mapper.copy1_1(source, target);

        // bookId is ignored
        assertThat(target.getBookId()).isNull();
        assertThat(target.getBookName()).isEqualTo(source.getBookName());
        assertThat(target.getPublishDate()).isEqualTo(source.getPublishDate());
        assertThat(target.getRecordVersion()).isEqualTo(source.getRecordVersion());
    }

    @Test
    void copy2_shouldHandleNullableSource() {
        BookUpdate target = new BookUpdate();
        mapper.copy2(null, target);
        assertThat(target.getBookId()).isNull();

        BookPo source = createBookPo();
        mapper.copy2(source, target);
        assertThat(target.getBookId()).isEqualTo(source.getBookId());
    }

    @Test
    void convert1_shouldThrowWhenSourceIsNull() {
        assertThatThrownBy(() -> mapper.convert1(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("can't be null");
    }

    @Test
    void copy1_shouldThrowWhenTargetIsNull() {
        assertThatThrownBy(() -> mapper.copy1(createBookPo(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("can't be null");
    }

    @Test
    void convert1_2_shouldThrowWhenTargetIsNull() {
        assertThatThrownBy(() -> mapper.convert1_2(createBookPo(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("can't be null");
    }

    @Test
    void inheritedConvert_shouldWorkFromBaseMapper() {
        // This method comes from BaseMapper<S, T> interface
        BookPo source = createBookPo();

        BookUpdate result = mapper.convert(source);

        assertThat(result).isNotNull();
        assertThat(result.getBookId()).isEqualTo(source.getBookId());
    }

    @Test
    void inheritedCopy_shouldWorkFromBaseMapper() {
        // This method comes from BaseMapper<S, T> interface
        BookPo source = createBookPo();
        BookUpdate target = new BookUpdate();

        mapper.copy(source, target);

        assertThat(target.getBookId()).isEqualTo(source.getBookId());
    }

    private BookPo createBookPo() {
        return createBookPo(BigInteger.valueOf(100), "Test Book");
    }

    private BookPo createBookPo(BigInteger id, String name) {
        BookPo po = new BookPo();
        po.setBookId(id);
        po.setBookName(name);
        po.setPublishDate(LocalDate.of(2024, 1, 1));
        po.setRecordVersion(1);
        po.localDate1 = LocalDate.of(2024, 6, 15); // public field
        return po;
    }
}