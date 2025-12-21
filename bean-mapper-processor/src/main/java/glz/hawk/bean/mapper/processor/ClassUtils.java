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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is responsible for
 *
 * @author Hawk
 */
 abstract class ClassUtils {
    static <T> T instance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static Class<? extends Function<String, String>> getFunctionClass(Supplier<Class<? extends Function<String, String>>> classSupplier) {
        try {
            return classSupplier.get();
        } catch (MirroredTypeException ex) {
            String qualifiedName = ((TypeElement) ((DeclaredType) ex.getTypeMirror()).asElement()).getQualifiedName().toString();
            try {
                return (Class<? extends Function<String, String>>) Class.forName(qualifiedName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
