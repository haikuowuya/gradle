/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.model.internal.core

import spock.lang.Specification

class ModelTypeTest extends Specification {

    def "represents type variables"() {
        when:
        def type = new ModelType<Map<String, Map<Integer, Float>>>() {}

        then:
        type.typeVariables[0] == ModelType.of(String)
        type.typeVariables[1] == new ModelType<Map<Integer, Float>>() {}
        type.typeVariables[1].typeVariables[0] == ModelType.of(Integer)
        type.typeVariables[1].typeVariables[1] == ModelType.of(Float)
    }

    def "generic type compatibility"() {
        def chars = new ModelType<List<CharSequence>>() {}
        def strings = new ModelType<List<String>>() {}
        def extendsChars = new ModelType<List<? extends CharSequence>>() {}
        def superStrings = new ModelType<List<? super String>>() {}

        expect:
        !chars.isAssignableFrom(strings)

        strings.isAssignableFrom(strings)
        !strings.isAssignableFrom(extendsChars)
        !strings.isAssignableFrom(superStrings)

        chars.isAssignableFrom(chars)
        !chars.isAssignableFrom(extendsChars)
        !chars.isAssignableFrom(superStrings)

        extendsChars.isAssignableFrom(chars)
        extendsChars.isAssignableFrom(strings)
        extendsChars.isAssignableFrom(extendsChars)
        !extendsChars.isAssignableFrom(superStrings)

        superStrings.isAssignableFrom(chars)
        superStrings.isAssignableFrom(strings)
        superStrings.isAssignableFrom(superStrings)
        !superStrings.isAssignableFrom(extendsChars)
    }

    def m1(List<? extends String> strings) {}

    def m2(List<? super String> strings) {}

    def m3(List<?> anything) {}

    def "wildcards"() {
        def extendsString = ModelType.of(getClass().getDeclaredMethod("m1", List.class).getGenericParameterTypes()[0]).typeVariables[0]
        def superString = ModelType.of(getClass().getDeclaredMethod("m2", List.class).getGenericParameterTypes()[0]).typeVariables[0]
        def anything = ModelType.of(getClass().getDeclaredMethod("m3", List.class).getGenericParameterTypes()[0]).typeVariables[0]

        expect:
        extendsString.wildcard
        superString.wildcard
        anything.wildcard

        extendsString.upperBound == ModelType.of(String)
        extendsString.lowerBound == null

        superString.upperBound == null
        superString.lowerBound == ModelType.of(String)

        anything.upperBound == null
        anything.lowerBound == null
    }

    def "isSubclass"() {
        def extendsString = ModelType.of(getClass().getDeclaredMethod("m1", List.class).getGenericParameterTypes()[0]).typeVariables[0]
        def superString = ModelType.of(getClass().getDeclaredMethod("m2", List.class).getGenericParameterTypes()[0]).typeVariables[0]
        def anything = ModelType.of(getClass().getDeclaredMethod("m3", List.class).getGenericParameterTypes()[0]).typeVariables[0]

        expect:
        !ModelType.of(String).asSubclass(ModelType.of(String))
        ModelType.of(CharSequence).asSubclass(ModelType.of(String))
        !ModelType.of(String).asSubclass(ModelType.of(CharSequence))
        !anything.asSubclass(superString)
        !superString.asSubclass(anything)
        !superString.asSubclass(extendsString)
        !extendsString.asSubclass(superString)
    }

    def "has wildcards"() {
        expect:
        !ModelType.of(String).hasWildcardTypeVariables
        new ModelType<List<?>>() {}.hasWildcardTypeVariables
        new ModelType<List<? extends CharSequence>>() {}.hasWildcardTypeVariables
        new ModelType<List<? super CharSequence>>() {}.hasWildcardTypeVariables
        !new ModelType<List<List<String>>>() {}.hasWildcardTypeVariables
        new ModelType<List<List<?>>>() {}.hasWildcardTypeVariables
        new ModelType<List<List<List<?>>>>() {}.hasWildcardTypeVariables
        new ModelType<List<List<? super List<String>>>>() {}.hasWildcardTypeVariables
    }
}
