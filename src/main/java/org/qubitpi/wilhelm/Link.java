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

import org.neo4j.driver.types.Relationship;
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
 * A JSON-serializable object representation of a directed link of knowledge graph in bertok.
 * <p>
 * A {@link Link} has 4 public attributes:
 * <ol>
 *     <li> A string used as the caption for rendering the link
 *     <li> The {@link Node#getId() unique identifier of node} originating this link
 *     <li> The {@link Node#getId() unique identifier of node} pointed to by this link
 *     <li> A map containing all other information encapsulated
 * </ol>
 */
@Immutable
@ThreadSafe
@JsonIncludeProperties({ "label", "sourceNodeId", "targetNodeId", "attributes" })
public class Link {

    /**
     * The database node attribute name whose value is used for displaying the relationship caption on UI.
     * <p>
     * For example, for Neo4J database, this would correspond to a relationship property.
     */
    public static final String LABEL_ATTRIBUTE = "label";

    private static final Logger LOG = LoggerFactory.getLogger(Link.class);

    private final String label;
    private final String sourceNodeId;
    private final String targetNodeId;
    private final Map<String, Object> attributes;

    /**
     * All-args constructor.
     *
     * @param label  The caption for the rendering of the link
     * @param sourceNodeId  The {@link Node#getId() ID} of the node originating this directed {@link Link}
     * @param targetNodeId  The {@link Node#getId() ID} of the node pointed to by this directed {@link Link}
     * @param attributes  The fields attached to this node other than label
     *
     * @throws NullPointerException if any argument is {@code null}
     */
    private Link(
            @NotNull final String label,
            @NotNull final String sourceNodeId,
            @NotNull final String targetNodeId,
            @NotNull final Map<String, Object> attributes
    ) {
        this.label = Objects.requireNonNull(label);
        this.sourceNodeId = Objects.requireNonNull(sourceNodeId);
        this.targetNodeId = Objects.requireNonNull(targetNodeId);
        this.attributes = new HashMap<>(Objects.requireNonNull(attributes)); // defensive copy
    }

    /**
     * Converts a Neo4J API relationship to a bertok {@link Link}.
     * <p>
     * The Neo4J relationship must contain a property called "name", otherwise an unchecked exception is thrown. The
     * {@link Relationship#startNodeElementId()} would be the {@link Node#getId() source node ID}; the
     * {@link Relationship#endNodeElementId()} would be the {@link Node#getId() target node ID} the "name" property
     * would be the {@link #getLabel() label of this node.}; the rest of the properties would be the
     * {@link #getAttributes() attributes} of this node
     *
     * @param relationship  A Neo4J Java driver API relationship
     *
     * @return a new instance of converted {@link Link}
     *
     * @throws NullPointerException if {@code relationship} is {@code null}
     * @throws IllegalStateException if {@code relationship} is missing a "name" property
     */
    public static Link valueOf(final Relationship relationship) {
        if (!Objects.requireNonNull(relationship).asMap().containsKey(LABEL_ATTRIBUTE)) {
            LOG.error("Neo4J relationship does not contain '{}' attribute: {}", LABEL_ATTRIBUTE, relationship.asMap());
            throw new IllegalStateException(
                    "There seems to be a data format mismatch between Wilhelm webservice and Neo4J database. " +
                            "Please file an issue at https://github.com/QubitPi/bertok/issues for a fix"
            );
        }

        final String label = relationship.asMap().get(LABEL_ATTRIBUTE).toString();
        final Map<String, Object> attributes = relationship.asMap().entrySet().stream()
                .filter(entry -> !LABEL_ATTRIBUTE.equals(entry.getKey()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        return new Link(label, relationship.startNodeElementId(), relationship.endNodeElementId(), attributes);
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    @NotNull
    public String getSourceNodeId() {
        return sourceNodeId;
    }

    @NotNull
    public String getTargetNodeId() {
        return targetNodeId;
    }

    /**
     * Returns an immutable view of the attributes of this Link.
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
        final Link that = (Link) other;
        return Objects.equals(getLabel(), that.getLabel()) && Objects.equals(
                getSourceNodeId(),
                that.getSourceNodeId()
        ) && Objects.equals(
                getTargetNodeId(),
                that.getTargetNodeId()
        ) && Objects.equals(getAttributes(), that.getAttributes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLabel(), getSourceNodeId(), getTargetNodeId(), getAttributes());
    }

    @Override
    public String toString() {
        return String.format("(%s)-%s-(%s)", getSourceNodeId(), getLabel(), getTargetNodeId());
    }
}
