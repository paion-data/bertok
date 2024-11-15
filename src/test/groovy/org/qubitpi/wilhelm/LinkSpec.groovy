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

import org.neo4j.driver.types.Relationship

import groovy.json.JsonSlurper
import spock.lang.Specification

class LinkSpec extends Specification {

    @SuppressWarnings('GroovyAccessibility')
    def "JSON serialization of Link includes 4 attributes, one of them is '#attribute'"() {
        when: "a Node object is serialized to a JSON"
        def actual = new JsonSlurper().parseText(
                new ObjectMapper().writeValueAsString(
                        new Link( "my link", "node1", "node2", [type: "follows"])
                )
        )

        then: "the JSON has 4 fields"
        actual.size() == 4

        and: "the fields contains one of the required fields"
        actual.keySet().contains(attribute)

        where:
        _ | attribute
        _ | "label"
        _ | "sourceNodeId"
        _ | "targetNodeId"
        _ | "attributes"
    }

    def "when a Neo4J relationship does not contain 'label' property, an error is thrown"() {
        when: "a Neo4J node has no properties"
        Link.valueOf(Mock(Relationship) {asMap() >> [:]})

        then: "IllegalStateException is thrown complaining about the missing required property"
        Exception exception = thrown()
        exception instanceof IllegalStateException
        exception.message == "There seems to be a data format mismatch between Wilhelm webservice and Neo4J database. " +
                "Please file an issue at https://github.com/QubitPi/wilhelm-ws/issues for a fix"
    }

    def "Neo4J relationship gets converted to a wilhelm-ws link"() {
        when: "a happy path Neo4J relationship is being converted to a transparent link"
        Link actual = Link.valueOf(Mock(Relationship) {
            asMap() >> [
                    label: "my node",
                    type: "follows"
            ]
            startNodeElementId() >> "node1"
            endNodeElementId() >> "node2"
        })

        then: "the transparent link is fully initialized"
        actual.label == "my node"
        actual.sourceNodeId == "node1"
        actual.targetNodeId == "node2"
        actual.attributes == [type: "follows"]
    }
}
