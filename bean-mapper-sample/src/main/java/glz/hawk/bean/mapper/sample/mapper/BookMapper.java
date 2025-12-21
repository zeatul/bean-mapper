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

import glz.hawk.bean.mapper.annotation.*;
import glz.hawk.bean.mapper.sample.beans.BookExPo;
import glz.hawk.bean.mapper.sample.beans.BookPo;
import glz.hawk.bean.mapper.sample.beans.BookUpdate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This interface is responsible for
 *
 * @author Zhang Peng
 */
@BeanMapper
public interface BookMapper {
    BookUpdate convert1(BookPo bookPo);

    BookUpdate convert2(@MappingSource(nullable = true) BookPo bookPo);

    BookUpdate convert3(@MappingSource(nullable = true) BookPo bookPo, @MappingSource(nullable = true) BookPo bookExPo);

    Map<String, Object> convert4(BookPo bookPo);

    Map<String, Object> convert5(@MappingSource(nullable = true) BookPo bookPo);

    BookExPo convert6(Map<String, String> map);

    LinkedHashMap<String, Object> convert7(BookPo bookPo);

    @ElementConverter(format = "convert1($L)", params = @ConverterParam(type = ConverterParamType.SOURCE))
    List<BookUpdate> convert8(List<BookPo> bookPoList);

    @ElementConverter(format = "convert1($L)", params = @ConverterParam(type = ConverterParamType.SOURCE))
    List<BookUpdate> convert9(BookPo[] bookPos);

    @ElementConverter(format = "convert1($L)", params = @ConverterParam(type = ConverterParamType.SOURCE))
    BookUpdate[] convert10(BookPo[] bookPos);

    @ElementConverter(format = "convert1($L)", params = @ConverterParam(type = ConverterParamType.SOURCE))
    BookUpdate[] convert11(List<BookPo> bookPoList);

    void copy1(BookPo bookPo, BookUpdate bookUpdate);

    void copy2(@MappingSource(nullable = true) BookPo bookPo, BookUpdate bookUpdate);

    void copy3(@MappingSource(nullable = true) BookPo bookPo, @MappingTarget(nullable = true) BookUpdate bookUpdate);

    void copy4(BookPo bookPo, BookExPo bookExPo, BookUpdate bookUpdate);

    void copy5(@MappingSource(nullable = true) BookPo bookPo, BookExPo bookExPo, BookUpdate bookUpdate);

    void copy6(@MappingSource(nullable = true) BookPo bookPo, @MappingSource(nullable = true) BookExPo bookExPo, BookUpdate bookUpdate);

    void copy7(@MappingSource(nullable = true) BookPo bookPo, @MappingSource(nullable = true) BookExPo bookExPo, @MappingTarget(nullable = true) BookUpdate bookUpdate);
}
