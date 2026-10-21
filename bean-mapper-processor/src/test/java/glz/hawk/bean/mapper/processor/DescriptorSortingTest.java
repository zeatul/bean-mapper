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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DescriptorSortingTest {

    @Test
    void getterDescriptorsAreSortedMethodFirst() {
        GetterDescriptor fieldGetter = new GetterDescriptor("a1", AccessType.PUBLIC_FIELD, null);
        GetterDescriptor methodGetter = new GetterDescriptor("getA1", AccessType.PUBLIC_METHOD, null);
        List<GetterDescriptor> descriptors = Arrays.asList(fieldGetter, methodGetter);
        Collections.sort(descriptors);

        assertThat(descriptors.get(0)).isSameAs(methodGetter);
        assertThat(descriptors.get(1)).isSameAs(fieldGetter);
    }

    @Test
    void setterDescriptorsAreSortedMethodFirst() {
        SetterDescriptor fieldSetter = new SetterDescriptor("a1", AccessType.PUBLIC_FIELD, null);
        SetterDescriptor methodSetter = new SetterDescriptor("setA1", AccessType.PUBLIC_METHOD, null);
        List<SetterDescriptor> descriptors = Arrays.asList(fieldSetter, methodSetter);
        Collections.sort(descriptors);

        assertThat(descriptors.get(0)).isSameAs(methodSetter);
        assertThat(descriptors.get(1)).isSameAs(fieldSetter);
    }
}
