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
package org.qubitpi.wilhelm.web.endpoints;

import org.aeonbits.owner.ConfigFactory;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.EagerResult;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.QueryConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.qubitpi.wilhelm.Graph;
import org.qubitpi.wilhelm.Language;
import org.qubitpi.wilhelm.LanguageCheck;
import org.qubitpi.wilhelm.Link;
import org.qubitpi.wilhelm.Node;
import org.qubitpi.wilhelm.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Endpoint that contains a basic sanity-check.
 */
@Singleton
@Immutable
@ThreadSafe
@Path("/neo4j")
@Produces(MediaType.APPLICATION_JSON)
public class Neo4JServlet {

    private static final Logger LOG = LoggerFactory.getLogger(Neo4JServlet.class);
    private static final ApplicationConfig APPLICATION_CONFIG = ConfigFactory.create(ApplicationConfig.class);
    private static final String NEO4J_URL = APPLICATION_CONFIG.neo4jUrl();
    private static final String NEO4J_USERNAME = APPLICATION_CONFIG.neo4jUsername();
    private static final String NEO4J_PASSWORD = APPLICATION_CONFIG.neo4jPassword();
    private static final String NEO4J_DATABASE = APPLICATION_CONFIG.neo4jDatabase();

    /**
     * Constructor for dependency injection.
     */
    @Inject
    public Neo4JServlet() {
        // intentionally left blank
    }

    /**
     * Returns the total number of terms of a specified langauges.
     *
     * @param language  The language. Must be one of the valid return value of {@link Language#getPathName()}; otherwise
     * a 400 response is returned
     *
     * @return a list of one map entry, whose key is 'count' and value is the total
     */
    @GET
    @LanguageCheck
    @Path("/languages/{language}/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCountByLanguage(@NotNull @PathParam("language") final String language) {
        final Language requestedLanguage = Language.ofClientValue(language);

        final String query = String.format(
                "MATCH (term:Term {language: '%s'}) RETURN count(*) as count", requestedLanguage.getDatabaseName()
        );

        return Response
                .status(Response.Status.OK)
                .entity(executeNonPathQuery(query))
                .build();
    }

    /**
     * Get paginated vocabularies of a language.
     *
     * @param language  The language. Must be one of the valid return value of {@link Language#getPathName()}; otherwise
     * a 400 response is returned
     * @param perPage  Requested number of words to be displayed on each page of results
     * @param page  Requested page of results desired
     *
     * @return the paginated Neo4J query results in JSON format
     */
    @GET
    @LanguageCheck
    @Path("/languages/{language}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVocabularyByLanguagePaged(
            @NotNull @PathParam("language") final String language,
            @NotNull @QueryParam("perPage") final String perPage,
            @NotNull @QueryParam("page") final String page
    ) {
        final Language requestedLanguage = Language.ofClientValue(language);

        final String query = String.format(
                "MATCH (t:Term WHERE t.language = '%s')-[r]->(d:Definition) " +
                "RETURN t.name AS term, d.name AS definition " +
                "SKIP %s LIMIT %s",
                requestedLanguage.getDatabaseName(), (Integer.parseInt(page) - 1) * Integer.parseInt(perPage), perPage
        );

        return Response
                .status(Response.Status.OK)
                .entity(executeNonPathQuery(query))
                .build();
    }

    /**
     * Search all nodes whose label contains a specified keyword.
     *
     * @param keyword  The provided keyword
     *
     * @return all nodes whose "name" attribute contains the search keyword
     */
    @GET
    @Path("/search/{keyword}")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("MultipleStringLiterals")
    public Response search(@NotNull @PathParam("keyword") final String keyword) {
        final String query = String.format("MATCH (node) WHERE node.name =~ '.*%s.*' RETURN node", keyword);

        return Response
                .status(Response.Status.OK)
                .entity(executeNonPathQuery(query))
                .build();
    }

    /**
     * Runs a cypher query against Neo4J database and return result as a JSON-serializable.
     * <p>
     * Use this method only if the {@code query} does not involve path, because this method cannot handle query result
     * that has path object nested in it
     *
     * @param query  A standard cypher query string
     *
     * @return query's native result
     */
    private Object executeNonPathQuery(@NotNull final String query) {
        return executeNativeQuery(query).records()
                .stream()
                .map(
                        record -> record.keys()
                                .stream()
                                .map(key -> new AbstractMap.SimpleImmutableEntry<>(key, expand(record.get(key))))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                )
                .collect(Collectors.toList());
    }

    /**
     * Transforms a Neo4J {@link Value} object into a Jackson-serializable Java object.
     *
     * See https://neo4j.com/docs/java-manual/current/data-types/ for more details
     *
     * @param value  An object graph. Cannot be {@code null}
     *
     * @return a {@link Map} representation of the object graph and can be Jackson-serialized
     */
    private static Object expand(@NotNull final Value value) {
        if (isTerminalValue(value)) {
            if (value.type().equals(InternalTypeSystem.TYPE_SYSTEM.INTEGER())) {
                return value.asInt();
            } else if (value.type().equals(InternalTypeSystem.TYPE_SYSTEM.BOOLEAN())) {
                return value.asBoolean();
            } else {
                return value.asString();
            }
        }

        return StreamSupport.stream(value.keys().spliterator(), false)
                .map(key -> new AbstractMap.SimpleImmutableEntry<>(key, expand(value.get(key))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Returns whether or not a {@link Value} object is the "leaf" node in Jackson serialization.
     * <p>
     * A "leaf" node is defined to be one of
     * <ul>
     *     <li> integer
     *     <li> string
     *     <li> boolean
     * </ul>
     *
     * @param value  An object graph. Cannot be {@code null}
     *
     * @return {@code true} if the object is simply a Jackson-serializable leaf node or {@code false} otherwise
     */
    private static boolean isTerminalValue(@NotNull final Value value) {
        return value.type().equals(InternalTypeSystem.TYPE_SYSTEM.INTEGER())
                || value.type().equals(InternalTypeSystem.TYPE_SYSTEM.STRING())
                || value.type().equals(InternalTypeSystem.TYPE_SYSTEM.BOOLEAN());
    }

    /**
     * Recursively find all related terms and definitions of a word.
     *
     * @param word  The word to expand
     *
     * @return a JSON representation of the expanded sub-graph
     */
    @GET
    @Path("/expand/{word}")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("MultipleStringLiterals")
    public Response expand(@NotNull @PathParam("word") final String word) {
        return expandApoc(word, "3");
    }

    /**
     * Recursively find all related terms and definitions of a word using multiple Cypher queries with a plain BFS
     * algorithm.
     * <p>
     * This is good for large sub-graph expand because it breaks huge memory consumption into sub-expand queries. But
     * this endpoint sends multiple queries to database which incurs roundtrips and large Network I/O
     *
     * @param word  The word to expand
     *
     * @return a JSON representation of the expanded sub-graph
     */
    @GET
    @Path("/expandDfs/{word}")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("MultipleStringLiterals")
    public Response expandDfs(@NotNull @PathParam("word") final String word) {
        return Response
                .status(Response.Status.OK)
                .entity(expandDfs(word, new HashSet<>()))
                .build();
    }

    /**
     * Recursively find all related terms and definitions of a word using multiple Cypher queries with a plain BFS
     * algorithm.
     *
     * @param label  The word to expand
     * @param visited  A record that keeps track of visited nodes
     *
     * @return the expanded sub-graph
     */
    private Graph expandDfs(@NotNull final String label, @NotNull final Set<String> visited) {
        if (visited.contains(label)) {
            return Graph.emptyGraph();
        }

        visited.add(label);
        final Graph oneHopExpand = (Graph) expandApoc(label, "1").getEntity();
        final Node wordNode = oneHopExpand.getNodes().stream()
                .filter(node -> label.equals(node.getLabel()))
                .findFirst()
                .orElseThrow(() -> {
                    final String message = String.format("'%s' was not found in graph %s", label, oneHopExpand);
                    LOG.error(message);
                    return new IllegalArgumentException(message);
                });
        return oneHopExpand.getUndirectedNeighborsOf(wordNode).stream()
                .map(neighbor -> expandDfs(neighbor.getLabel(), visited))
                .reduce(oneHopExpand, Graph::merge);
    }

    /**
     * Recursively find all related terms and definitions of a word using a single Cypher query with apoc extension.
     * <p>
     * This is bad for large sub-graph expand because it will exhaust memories allocated for the query in database. This
     * is good for small-subgraph expand when WS and database are far away from each other.
     *
     * @param word  The word to expand
     * @param maxHops  The max length of expanded path. Use "-1" for unlimitedly long path.
     *
     * @return a JSON representation of the expanded sub-graph. The format of the JSON would be
     */
    @GET
    @Path("/expandApoc/{word}")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("MultipleStringLiterals")
    public Response expandApoc(
            @NotNull @PathParam("word") final String word,
            @NotNull @QueryParam("maxHops") @DefaultValue("-1") final String maxHops
    ) {
        LOG.info("apoc expanding '{}' with max hops of {}", word, maxHops);

        final String query = String.format(
                """
                        MATCH (node{name:'%s'})
                        CALL apoc.path.expand(node, "LINK", null, 1, %s)
                        YIELD path
                        RETURN path, length(path) AS hops
                        ORDER BY hops;
                """,
                word.replace("'", "\\'"), maxHops
        );

        final EagerResult result = executeNativeQuery(query);

        final Set<Node> nodes = new HashSet<>();
        final Set<Link> links = new HashSet<>();

        result.records().stream()
                .map(record -> record.get("path").asPath())
                .forEach(path -> {
                    path.nodes().forEach(node -> nodes.add(Node.valueOf(node)));
                    path.relationships().forEach(relationship -> links.add(Link.valueOf(relationship)));
                });

        return Response
                .status(Response.Status.OK)
                .entity(new Graph(nodes, links))
                .build();
    }


    /**
     * Runs a cypher query against Neo4J database and return the query result unmodified.
     *
     * @param query  A standard cypher query string
     *
     * @return query's native result
     *
     * @throws IllegalStateException if a query execution error occurs
     */
    @NotNull
    private EagerResult executeNativeQuery(@NotNull final String query) {
        try (Driver driver = GraphDatabase.driver(NEO4J_URL, AuthTokens.basic(NEO4J_USERNAME, NEO4J_PASSWORD))) {
            driver.verifyConnectivity();

            return driver.executableQuery(query)
                    .withConfig(QueryConfig.builder().withDatabase(NEO4J_DATABASE).build())
                    .execute();
        }
    }
}
