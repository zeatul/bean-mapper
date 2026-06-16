package glz.hawk.bean.mapper.sample.beans;/*
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

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * This class is responsible for
 *
 * @author Zhang Peng
 */
public class BookPo {
    private BigInteger bookId;
    private String bookName;
    private LocalDate publishDate;
    private Integer recordVersion;
    private String a1;
    private LocalDate localDate;

    public LocalDate localDate1;

    public BigInteger getBookId() {
        return this.bookId;
    }

    public void setBookId(BigInteger bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return this.bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public LocalDate getPublishDate() {
        return this.publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public Integer getRecordVersion() {
        return this.recordVersion;
    }

    public void setRecordVersion(Integer recordVersion) {
        this.recordVersion = recordVersion;
    }

    public String getA1() {
        return this.a1;
    }

    public void setA1(String a1) {
        this.a1 = a1;
    }

    public LocalDate getLocalDate() {
        return this.localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDate getLocalDate1() {
        return this.localDate1;
    }

    public void setLocalDate1(LocalDate localDate1) {
        this.localDate1 = localDate1;
    }
}
