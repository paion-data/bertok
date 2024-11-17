/*
 * Copyright Jiaqi Liu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qubitpi.wilhelm

import spock.lang.Specification

import java.util.stream.Collectors

class LanguageSpec extends Specification {

    def 'There are 3 valid languages'() {
        expect:
        Language.values().size() == 3
    }

    def "Only certain languages are supported"() {
        given: "all restricted languages"
        List<String> allValidDatabaseNames = ["German", "Ancient Greek", "Latin"]
        List<String> allValidPathNames = ["german", "ancientGreek", "latin"]

        expect: "no other database name is allowed"
        allValidDatabaseNames.containsAll(
                Arrays.stream(Language.values()).map {it -> it.databaseName}.collect(Collectors.toList())
        )

        and: "no other API path name is allowed"
        allValidPathNames.containsAll(Arrays.stream(Language.values()).map {it -> it.pathName}.collect(Collectors.toList()))
    }

    def "'#databaseName' can be converted to object"() {
        when: "a supported database name is being converted to a Language object"
        Language.ofDatabaseName(databaseName)

        then: "no error occurs"
        noExceptionThrown()

        where:
        _ | databaseName
        _ | "German"
        _ | "Ancient Greek"
        _ | "Latin"
    }

    def "'#pathName' can be converted to object"() {
        when: "a supported API language name is being converted to a Language object"
        Language.ofClientValue(pathName)

        then: "no error occurs"
        noExceptionThrown()

        where:
        _ | pathName
        _ | "german"
        _ | "ancientGreek"
        _ | "latin"
    }

    @SuppressWarnings('GroovyAccessibility')
    def "Invalid language cannot construct the object"() {
        when: "invalid database name is used to construct the object"
        Language.valueOf("foo", (language) -> language.getDatabaseName())

        then: "error occurs"
        Exception exception = thrown(IllegalArgumentException)
        exception.message == "'foo' is not a recognized language. Acceptable ones are German, Ancient Greek, Latin"

        when: "invalid API path name is used to construct the object"
        Language.valueOf("foo", (language) -> language.getPathName())

        then: "error occurs"
        exception = thrown(IllegalArgumentException)
        exception.message == "'foo' is not a recognized language. Acceptable ones are german, ancientGreek, latin"
    }
}
