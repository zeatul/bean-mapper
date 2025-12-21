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

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for
 *
 * @author Hawk
 */
class PropertyDescriptor {
    public final String propertyNam;
    public final String fullPath;
    public List<GetterDescriptor> getterDescriptors = new ArrayList<>();
    public List<SetterDescriptor> setterDescriptors = new ArrayList<>();

    PropertyDescriptor(String parentFullPath, String propertyNam) {
        this.propertyNam = propertyNam;
        this.fullPath = parentFullPath + "/" + propertyNam;
    }

    void add(GetterDescriptor getterDescriptor) {
        getterDescriptors.add(getterDescriptor);
    }

    void add(SetterDescriptor setterDescriptor) {
        setterDescriptors.add(setterDescriptor);
    }


}
