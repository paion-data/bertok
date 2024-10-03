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
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.matchesPattern

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
import jakarta.ws.rs.core.MediaType
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration
import java.time.temporal.ChronoUnit

@Testcontainers
class DataServletITSpec extends Specification {

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

    def "Healthchecking endpoints returns 200"() {
        expect:
        RestAssured.given()
                .when()
                .get("/data/healthcheck")
                .then()
                .statusCode(200)
    }

    def "Get vocabulary by language returns a list of map, with each entry containing 'term' and 'definition' keys"() {
        expect:
        RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .queryParams([perPage: "10", page: "1"])
                .when()
                .get("/data/languages/german")
                .then()
                .statusCode(200)
                .body("[0]", hasKey("term"))
                .body("[0]", hasKey("definition"))
    }

    def "Expand a word returns a map of two keys - 'nodes' & 'links'"() {
        expect:
        RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get("/data/expand/nämlich")
                .then()
                .statusCode(200)
                .body("", hasKey("nodes"))
                .body("", hasKey("links"))
                .body("nodes[0]", hasKey("id"))
                .body("links[0]", hasKey("sourceNodeId"))
                .body("links[0]", hasKey("targetNodeId"))
    }
}
