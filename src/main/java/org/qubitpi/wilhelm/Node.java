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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotNull;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A JSON-serializable object representation of a knowledge graph node in bertok.
 * <p>
 * A {@link Node} has 3 public attributes:
 * <ol>
 *     <li> A unique string identifier within a knowledge graph
 *     <li> A string used as the caption for rendering the node
 *     <li> A map containing all other information encapsulated
 * </ol>
 */
@Immutable
@ThreadSafe
@JsonIncludeProperties({ "id", "label", "attributes" })
public class Node {

    /**
     * The database node attribute name whose value is used for displaying the node caption on UI.
     * <p>
     * For example, for Neo4J database, this would correspond to a node property.
     */
    public static final String LABEL_ATTRIBUTE = "label";

    private static final Logger LOG = LoggerFactory.getLogger(Node.class);

    private final String id;
    private final String label;
    private final Map<String, Object> attributes;

    /**
     * All-args constructor.
     *
     * @param id  The unique identifier of a node within a graph. Does not need to be unique across the database.
     * @param label  The caption for the rendering of the node
     * @param attributes  The fields attached to this node other than label
     *
     * @throws NullPointerException if any argument is {@code null}
     */
    private Node(@NotNull final String id, @NotNull final String label, @NotNull final Map<String, Object> attributes) {
        this.id = Objects.requireNonNull(id);
        this.label = Objects.requireNonNull(label);
        this.attributes = new HashMap<>(Objects.requireNonNull(attributes)); // defensive copy
    }

    /**
     * Converts a Neo4J API node to a bertok {@link Node}.
     * <p>
     * The Neo4J node must contain a property called "name", otherwise an unchecked exception is thrown. The
     * {@link org.neo4j.driver.types.Node#elementId()} would be the {@link #getId() ID of this node}; the "name"
     * property would be the {@link #getLabel() label of this node.}; the rest of the properties would be the
     * {@link #getAttributes() attributes} of this node
     *
     * @param node  A Neo4J Java driver API node
     *
     * @return a new instance of converted {@link Node}
     *
     * @throws NullPointerException if {@code node} is {@code null}
     * @throws IllegalStateException if {@code node} is missing a "name" property
     */
    public static Node valueOf(@NotNull final org.neo4j.driver.types.Node node) {
        if (!Objects.requireNonNull(node).asMap().containsKey(LABEL_ATTRIBUTE)) {
            LOG.error("Neo4J node does not contain '{}' attribute: {}", LABEL_ATTRIBUTE, node.asMap());
            throw new IllegalStateException(
                    "There seems to be a data format mismatch between Wilhelm webservice and Neo4J database. " +
                            "Please file an issue at https://github.com/QubitPi/bertok/issues for a fix"
            );
        }

        final String label = node.asMap().get(LABEL_ATTRIBUTE).toString();
        final Map<String, Object> attributes = node.asMap().entrySet().stream()
                .filter(entry -> !LABEL_ATTRIBUTE.equals(entry.getKey()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        return new Node(node.elementId(), label, attributes);
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    /**
     * Returns an immutable view of the attributes of this Node.
     *
     * @return an unmodifiable map
     */
    @NotNull
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final Node that = (Node) other;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /**
     * Returns a string representation of this Node.
     * <p>
     * The content of the string equals to {@link #getLabel() the label} of this Node.
     *
     * @return a human-readable caption of this node
     */
    @Override
    public String toString() {
        return getLabel();
    }
}
