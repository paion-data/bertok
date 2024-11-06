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
package org.qubitpi.wilhelm.web.endpoints


import org.neo4j.driver.Value
import org.neo4j.driver.internal.types.InternalTypeSystem

import spock.lang.Specification
import spock.lang.Unroll

class Neo4JServletSpec extends Specification {

    @SuppressWarnings('GroovyAccessibility')
    def "Embedded Neo4J Value objects are recursively expanded to become plain JSONable map"() {
        given: "a 2-level nested result object"
        Value value = Mock(Value) {
            type() >> InternalTypeSystem.TYPE_SYSTEM.NODE()
            keys() >> ["term", "definition"]
            get("term") >> Mock(Value) {
                type() >> InternalTypeSystem.TYPE_SYSTEM.NODE()
                keys() >> ["name", "language"]
                get("name") >> Mock(Value) {
                    type() >> InternalTypeSystem.TYPE_SYSTEM.STRING()
                    asString() >> "Hallo"
                }
                get("language") >> Mock(Value) {
                    type() >> InternalTypeSystem.TYPE_SYSTEM.STRING()
                    asString() >> "German"
                }
            }
            get("definition") >> Mock(Value) {
                type() >> InternalTypeSystem.TYPE_SYSTEM.NODE()
                keys() >> ["name"]
                get("name") >> Mock(Value) {
                    type() >> InternalTypeSystem.TYPE_SYSTEM.STRING()
                    asString() >> "Hello"
                }
            }
        }

        expect:
        Neo4JServlet.expand(value) == [
                term: [
                    name: "Hallo",
                    language: "German"
                ],
                definition: [
                    name: "Hello"
                ]
        ]
    }

    @Unroll
    @SuppressWarnings('GroovyAccessibility')
    def "Value of type '#valueType' #isOrNot terminal type"() {
        given:
        Value value = Mock(Value) {
            type() >> valueType
        }

        expect:
        Neo4JServlet.isTerminalValue(value) == isTerminalType

        where:
        valueType                                || isTerminalType
        InternalTypeSystem.TYPE_SYSTEM.INTEGER() || true
        InternalTypeSystem.TYPE_SYSTEM.BOOLEAN() || true
        InternalTypeSystem.TYPE_SYSTEM.STRING()  || true
        InternalTypeSystem.TYPE_SYSTEM.LIST()    || false
        InternalTypeSystem.TYPE_SYSTEM.MAP()     || false
        InternalTypeSystem.TYPE_SYSTEM.NODE()    || false

        isOrNot = isTerminalType ? "is" : "is not"
    }
}
