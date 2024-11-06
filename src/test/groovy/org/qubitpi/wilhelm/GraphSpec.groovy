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

class GraphSpec extends Specification {

    def "JSON serialization of Graph includes 2 attributes - nodes and links"() {
        when: "a Graph object is serialized to a JSON"
        def actual = new JsonSlurper().parseText(
                new ObjectMapper().writeValueAsString(
                        new Graph(new HashSet<Node>(), new HashSet<Link>())
                )
        )

        then: "the JSON has 2 fields"
        actual.size() == 2

        and: "the fields are nodes and links"
        actual.keySet().contains("nodes")
        actual.keySet().contains("links")
    }

    @SuppressWarnings('GroovyAccessibility')
    def "When a graph has 4 nodes, one being isolated, 1 node (A) has 2 neighbors with one being the source and the other being the target node, getting neighbors on A returns 2 nodes"() {
        given: "an isolated node"
        Node isolated = new Node( "isolated", "isolated", [:])

        and: "a node with 2 neighbors"
        Node node = new Node( "node", "node", [:])
        Node neighbor1 = new Node( "neighbor1", "neighbor1", [:])
        Node neighbor2 = new Node( "neighbor2", "neighbor2", [:])

        Link link1 = new Link("pointing from node to neighbor1", "node", "neighbor1", [:])
        Link link2 = new Link("pointing from neighbor2 to node", "neighbor2", "node", [:])

        and: "the 4 nodes and 2 links belong to a graph under test"
        Graph graph = new Graph([isolated, node, neighbor1, neighbor2] as Set, [link1, link2] as Set)

        when: "retrieving the neighbors of the node"
        Set<Node> actual = graph.getUndirectedNeighborsOf(node)

        then: "both neighbor1 and neighbor2 are within the result"
        actual == [neighbor1, neighbor2] as Set
    }

    @SuppressWarnings('GroovyAccessibility')
    def "Merging 2 graphs combines their nodes and links"() {
        given: "2 graphs to be merged"
        Graph graph1 = new Graph([new Node( "node1", "node1", [:])] as Set, [new Link("link1", "", "", [:])] as Set)
        Graph graph2 = new Graph([new Node( "node2", "node2", [:])] as Set, [new Link("link2", "", "", [:])] as Set)

        when: "2 graphs are merged"
        Graph actual = graph1.merge(graph2)

        then: "the new graph contains all nodes and links from both graph1 and graph2"
        actual.nodes == [new Node( "node1", "node1", [:]), new Node( "node2", "node2", [:])] as Set
        actual.links == [new Link("link1", "", "", [:]), new Link("link2", "", "", [:])] as Set
    }
}
