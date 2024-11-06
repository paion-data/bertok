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
package org.qubitpi.wilhelm;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotNull;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A JSON-serializable object representation of a knowledge graph in wilhelm-ws.
 */
@Immutable
@ThreadSafe
@SuppressWarnings("ClassCanBeRecord")
@JsonIncludeProperties({ "nodes", "links" })
public class Graph {

    private static final Logger LOG = LoggerFactory.getLogger(Graph.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final Set<Node> nodes;
    private final Set<Link> links;

    /**
     * All-args constructor.
     *
     * @param nodes  The set of all nodes contained in this Graph, cannot be {@code null}
     * @param links  The set of all links contained in this Graph, cannot be {@code null}
     */
    public Graph(@NotNull final Set<Node> nodes, @NotNull final Set<Link> links) {
        this.nodes = new HashSet<>(Objects.requireNonNull(nodes));
        this.links = new HashSet<>(Objects.requireNonNull(links));
    }

    /**
     * Creates a new {@link Graph} instance with no initial nodes or links in it.
     *
     * @return a new instance
     */
    public static Graph emptyGraph() {
        return new Graph(new HashSet<>(), new HashSet<>());
    }

    /**
     * Returns whether or not this {@link Graph} has neither nodes noe links.
     *
     * @return {@code true} if no nodes or links exist in this {@link Graph}, or {@code false} otherwise.
     */
    public boolean isEmpty() {
        return getNodes().isEmpty() && getLinks().isEmpty();
    }

    /**
     * Returns all weakly connected neighbors of a specified node.
     * <p>
     * If the node has no such neighrbors, this method returns an empty list
     *
     * @param node a node from this {@link Graph}
     *
     * @return all nodes each of which has a link between it and the provided node.
     */
    @NotNull
    public Set<Node> getUndirectedNeighborsOf(final Node node) {
        final Set<String> neighborIds = getLinks().stream()
                .filter(link ->
                        node.getId().equals(link.getSourceNodeId()) || node.getId().equals(link.getTargetNodeId())
                )
                .flatMap(link -> Stream.of(link.getSourceNodeId(), link.getTargetNodeId()))
                .filter(id -> !node.getId().equals(id))
                .collect(Collectors.toUnmodifiableSet());

        return getNodes().stream()
                .filter(it -> neighborIds.contains(it.getId()))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Combines the nodes and links from this {@link Graph} instance and the other one and returns a new {@link Graph}.
     *
     * @param that the other {@link Graph} instance to be merged with this {@link Graph}
     *
     * @return a new instance
     */
    public Graph merge(@NotNull final Graph that) {
        return new Graph(
                Stream.of(this.getNodes(), that.getNodes()).flatMap(Set::stream).collect(Collectors.toSet()),
                Stream.of(this.getLinks(), that.getLinks()).flatMap(Set::stream).collect(Collectors.toSet())
        );
    }

    /**
     * Returns an unmodifiable view of all the nodes in this Graph instance.
     *
     * @return an immutable list of nodes
     */
    @NotNull
    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    /**
     * Returns an unmodifiable view of all the links in this Graph instance.
     *
     * @return an immutable list of links
     */
    @NotNull
    public Set<Link> getLinks() {
        return Collections.unmodifiableSet(links);
    }

    /**
     * Returns a JSON serialization of this Graph instance. It contains 2 fields: nodes and links, each of which is a
     * list of nodes and links respectively. Each list element is itself a JSON object whose structure are defined by
     * Jackson's serialization on {@link Node} and {@link Link}.
     *
     * @return a JSON string
     */
    @NotNull
    @Override
    public String toString() {
        try {
            return JSON_MAPPER.writeValueAsString(this);
        } catch (final JsonProcessingException exception) {
            LOG.error(exception.getMessage());
            throw new IllegalStateException(exception);
        }
    }
}
