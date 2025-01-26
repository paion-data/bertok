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

import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonSlurper
import spock.lang.Specification

class NodeSpec extends Specification {

    @SuppressWarnings('GroovyAccessibility')
    def "JSON serialization of Node includes 3 attributes, one of them is '#attribute'"() {
        when: "a Node object is serialized to a JSON"
        def actual = new JsonSlurper().parseText(
                new ObjectMapper().writeValueAsString(
                        new Node( "my ID", "my node", [color: "blue", size: "medium"])
                )
        )

        then: "the JSON has 3 fields"
        actual.size() == 3

        and: "the fields contains one of the required fields"
        actual.keySet().contains(attribute)

        where:
        _ | attribute
        _ | "id"
        _ | "label"
        _ | "attributes"
    }

    def "when a Neo4J node does not contain 'label' property, an error is thrown"() {
        when: "a Neo4J node has no properties"
        Node.valueOf(Mock(org.neo4j.driver.types.Node) {asMap() >> [:]})

        then: "IllegalStateException is thrown complaining about the missing required property"
        Exception exception = thrown()
        exception instanceof IllegalStateException
        exception.message == "There seems to be a data format mismatch between Wilhelm webservice and Neo4J database. " +
                "Please file an issue at https://github.com/QubitPi/bertok/issues for a fix"
    }

    def "Neo4J node gets converted to a bertok node"() {
        when: "a happy path Neo4J node is being converted to a transparent node"
        Node actual = Node.valueOf(Mock(org.neo4j.driver.types.Node) {
            asMap() >> [
                    label: "my node",
                    color: "blue",
                    size: "medium"
            ]
            elementId() >> "my id"
        })

        then: "the transparent node is fully initialized"
        actual.id == "my id"
        actual.label == "my node"
        actual.attributes == [color: "blue", size: "medium"]
    }
}
