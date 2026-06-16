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
import glz.hawk.bean.mapper.support.ComponentModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static glz.hawk.bean.mapper.annotation.ConverterParamType.*;

/**
 * <p>BeanMapper可以指定注入的bean
 * <p>BeanMapper可以指定静态import</p>
 * <p>BeanMapper可以指定用哪个注解来标识bean，例如spring支持的Component注解</p>
 *
 * @author Zhang Peng
 */
@BeanMapper(componentModel = ComponentModel.SPRING,
    autowiredBeans = {
        @AutowiredBean(type = "glz.hawk.bean.mapper.sample.mapper.UserMapper", qualifier = {"userMapper2"}, fieldName = "userMapper2"),
        @AutowiredBean(type = "glz.hawk.bean.mapper.sample.mapper.BookStoreMapper")
    },
    staticImports = {
        "glz.hawk.bean.mapper.sample.beans.BookExPo.*",
        "glz.hawk.bean.mapper.sample.beans.BookUpdate.*"
    }
)
public interface BookMapper {
    BookUpdate convert1(BookPo bookPo);

    @PropertyMapping(source = "bookId", ignore = true)
    @PropertyMapping(source = "bookName", ignore = true)
    BookUpdate convert1_1(BookPo bookPo);

    @PropertyMapping(source = "bookId", ignore = false)
    @PropertyMapping(source = "bookName", ignore = true)
    @MappingTarget(includeAll = false)
    BookUpdate convert1_2(BookPo bookPo);

    @PropertyMapping(source = "bookId", ignore = false)
    @PropertyMapping(source = "bookName", ignore = true)
    BookUpdate convert1_3(@MappingSource(includeAll = false) BookPo bookPo);

    BookUpdate convert2(@MappingSource(nullable = true) BookPo bookPo);

    BookUpdate convert3(@MappingSource(nullable = true) BookPo bookPo, @MappingSource(nullable = true) BookPo bookExPo);

    Map<String, Object> convert4(BookPo bookPo);

    Map<String, Object> convert5(@MappingSource(nullable = true) BookPo bookPo);

    BookExPo convert6(Map<String, String> map);

    LinkedHashMap<String, Object> convert7(BookPo bookPo);

    /**
     * ElementConverter可以指定集合内元素之间的转换代码
     */
    @ElementConverter(format = "convert1($L)", params = @ConverterParam(type = ConverterParamType.SOURCE))
    List<BookUpdate> convert8(List<BookPo> bookPoList);

    @ElementConverter(format = "convert1($L)", params = @ConverterParam(type = ConverterParamType.SOURCE))
    List<BookUpdate> convert9(BookPo[] bookPos);

    @ElementConverter(format = "convert1($L)", params = @ConverterParam(type = ConverterParamType.SOURCE))
    BookUpdate[] convert10(BookPo[] bookPos);

    @ElementConverter(format = "convert1($L)", params = @ConverterParam(type = ConverterParamType.SOURCE))
    BookUpdate[] convert11(List<BookPo> bookPoList);

    void copy1(BookPo bookPo, BookUpdate bookUpdate);

    @PropertyMapping(source = "bookId", ignore = true)
    void copy1_1(BookPo bookPo, BookUpdate bookUpdate);

    void copy2(@MappingSource(nullable = true) BookPo bookPo, BookUpdate bookUpdate);

    void copy3(@MappingSource(nullable = true) BookPo bookPo, @MappingTarget(nullable = true) BookUpdate bookUpdate);

    void copy4(BookPo bookPo, BookExPo bookExPo, BookUpdate bookUpdate);

    void copy5(@MappingSource(nullable = true) BookPo bookPo, BookExPo bookExPo, BookUpdate bookUpdate);

    void copy6(@MappingSource(nullable = true) BookPo bookPo, @MappingSource(nullable = true) BookExPo bookExPo, BookUpdate bookUpdate);

    void copy7(@MappingSource(nullable = true) BookPo bookPo, @MappingSource(nullable = true) BookExPo bookExPo, @MappingTarget(nullable = true) BookUpdate bookUpdate);


    /**
     * PropertyMapping可以指定不同名的参数互相匹配
     * PropertyMapping可以指定不同类型的参数之间进行转换的转换代码
     */
    @PropertyMapping(target = "bookId", ignore = true)
    @PropertyMapping(source = "bookPo.a1", target = "a")
    @PropertyMapping(source = "bookPo.localDate", target = "localDateStr",
        converter = @Converter(format = "$L.format($T.ofPattern($S))",
            params = {@ConverterParam(type = SOURCE),
                @ConverterParam(type = TYPE, value = "java.time.format.DateTimeFormatter"),
                @ConverterParam(type = LITERAL, value = "yyyy-MM-dd")}
        )
    )
    @PropertyMapping(source = "bookPo.localDate1", target = "localDateStr1",
        converter = @Converter(format = "$L.format($T.ofPattern($S))",
            params = {@ConverterParam(type = SOURCE),
                @ConverterParam(type = TYPE, value = "java.time.format.DateTimeFormatter"),
                @ConverterParam(type = LITERAL, value = "yyyy-MM-dd")}
        )
    )
    void copy7_1(@MappingSource(nullable = true) BookPo bookPo, BookExPo bookExPo, @MappingTarget BookUpdate bookUpdate);

    /**
     * MappingSource注解可以指定不输出其参数
     * MappingTarget注解可以指定不接收任何参数
     * PropertyMapping指定的任然可以输出
     */
    @PropertyMapping(source = "bookPo.bookName", target = "bookName")
    void copy7_2(@MappingSource(nullable = true, includeAll = false) BookPo bookPo, BookExPo bookExPo, @MappingTarget(includeAll = false) BookUpdate bookUpdate);

    /**
     * 忽略bookName
     * bookId只生效一个
     */
    @PropertyMapping(target = "bookName", ignore = true)
    @PropertyMapping(source = "bookPo.bookId", target = "bookId")
    void copy7_3(BookPo bookPo, BookExPo bookExPo, BookUpdate bookUpdate);

    /**
     * default方法不生成代码
     */
    default void copy8(BookPo bookPo, BookExPo bookExPo, BookUpdate bookUpdate) {
        throw new UnsupportedOperationException();
    }

    /**
     * 静态方法不生成代码
     */
    public static void x() {
        LocalDate date = LocalDate.now();
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
