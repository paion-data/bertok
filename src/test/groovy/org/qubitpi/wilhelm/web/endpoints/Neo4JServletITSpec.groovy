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

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.greaterThan
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.matchesPattern
import static org.hamcrest.Matchers.matchesRegex

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.qubitpi.wilhelm.JettyServerFactory
import org.qubitpi.wilhelm.application.ResourceConfig

import org.eclipse.jetty.server.Server
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.spock.Testcontainers

import groovy.json.JsonBuilder
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.specification.Argument
import jakarta.ws.rs.core.MediaType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration
import java.time.temporal.ChronoUnit

@Testcontainers
class Neo4JServletITSpec extends Specification {

    static final int PORT = 8080

    @Shared
    static final GenericContainer NEO4J_CONTAINER = new GenericContainer<>("jack20191124/wilhelm-vocabulary")
            .withExposedPorts(7474, 7687)
            .withEnv([
                    NEO4J_AUTH: "none",
                    NEO4J_ACCEPT_LICENSE_AGREEMENT: "yes",
                    NEO4JLABS_PLUGINS: "[\"apoc\"]"
            ])
            .waitingFor(new LogMessageWaitStrategy().withRegEx(".*INFO  Started.*"))
            .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))

    Server server = JettyServerFactory.newInstance(PORT, "/v1/*", new ResourceConfig())

    def setupSpec() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = PORT
        RestAssured.basePath = "/v1"

        NEO4J_CONTAINER.start()

        System.setProperty("NEO4J_URI", String.format("neo4j://localhost:%d", NEO4J_CONTAINER.getMappedPort(7687)))
        System.setProperty("NEO4J_USERNAME", "NOT USED")
        System.setProperty("NEO4J_PASSWORD", "NOT USED")
        System.setProperty("NEO4J_DATABASE", "neo4j")

        // We need to figure out how to fill DB with some initial data. Coming back to this ASAP
    }

    def setup() {
        server.start()
    }

    def cleanup() {
        server.stop()
    }

    def "Get count by language returns a list of one map entry, whose key is 'count' and value is the total"() {
        expect:
        RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get("/neo4j/languages/german/count")
                .then()
                .statusCode(200)
                .body("[0].count", greaterThan(1))
    }

    def "Get vocabulary by language returns a list of map, with each entry containing 'term' and 'definition' keys"() {
        expect:
        RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .queryParams([perPage: "10", page: "1"])
                .when()
                .get("/neo4j/languages/german")
                .then()
                .statusCode(200)
                .body("[0]", hasKey("term"))
                .body("[0]", hasKey("definition"))
    }

    @Unroll
    def "When #endpoint receives a invalid language, a 404 error response is sent with details"() {
        expect:
        RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get("/neo4j/languages/myInvalidLanguage")
                .then()
                .statusCode(400)
                .body(equalTo("'myInvalidLanguage' is not a recognized language. Acceptable ones are german, ancientGreek, latin"))

        where:
        _ | endpoint
        _ | "/neo4j/languages/myInvalidLanguage"
        _ | "/neo4j/languages/myInvalidLanguage/count"
    }

    def "Expand a word returns a map of two keys - 'nodes' & 'links'"() {
        expect:
        RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get("/neo4j/expand/dreißig")
                .then()
                .statusCode(200)
                .body("", hasKey("nodes"))
                .body("", hasKey("links"))
                .body("nodes[0]", hasKey("id"))
                .body("links[0]", hasKey("sourceNodeId"))
                .body("links[0]", hasKey("targetNodeId"))
    }
}
