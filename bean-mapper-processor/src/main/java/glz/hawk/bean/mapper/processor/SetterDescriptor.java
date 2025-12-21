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

import javax.lang.model.type.TypeMirror;

/**
 * This class is responsible for
 *
 * @author Hawk
 */
class SetterDescriptor implements Comparable<SetterDescriptor>{
    public final String setterName;
    public final AccessType setAccessType;
    public final TypeMirror typeMirror;

    SetterDescriptor(String setterName, AccessType setAccessType, TypeMirror typeMirror) {
        this.setterName = setterName;
        this.setAccessType = setAccessType;
        this.typeMirror = typeMirror;
    }

    @Override
    public int compareTo(SetterDescriptor that) {
        return this.setAccessType.index.compareTo(that.setAccessType.index);
    }
}
