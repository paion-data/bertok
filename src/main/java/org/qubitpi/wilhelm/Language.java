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

import jakarta.validation.constraints.NotNull;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A natural language represented in webservice that bridges the client request data format to database request format.
 */
@Immutable
@ThreadSafe
public enum Language {

    /**
     * German language.
     */
    GERMAN("german", "German"),

    /**
     * Ancient Greek.
     */
    ANCIENT_GREEK("ancientGreek", "Ancient Greek"),

    /**
     * Latin.
     */
    LATIN("latin", "Latin");

    private final String pathName;
    private final String databaseName;

    /**
     * All-args constructor.
     *
     * @param pathName  The client-side language name
     * @param databaseName  The database language name
     */
    Language(@NotNull final String pathName, @NotNull final String databaseName) {
        this.pathName = pathName;
        this.databaseName = databaseName;
    }

    /**
     * Constructs a {@link Language} from its {@link #getDatabaseName() database name}.
     *
     * @param language  The client-side requested language name
     *
     * @return a new instance
     *
     * @throws IllegalArgumentException if the language name is not a valid one
     */
    @NotNull
    public static Language ofDatabaseName(@NotNull final String language) {
        return valueOf(language, Language::getDatabaseName);
    }

    /**
     * Constructs a {@link Language} from its {@link #getPathName() client-side name}.
     *
     * @param language  The client-side requested language name
     *
     * @return a new instance
     *
     * @throws IllegalArgumentException if the language name is not a valid one
     */
    @NotNull
    public static Language ofClientValue(@NotNull final String language) throws IllegalArgumentException {
        return valueOf(language, Language::getPathName);
    }

    /**
     * Converts a string value to a {@link Language} object.
     * <p>
     * The string value must match one of the {@link #getDatabaseName() database name} or
     * {@link #getPathName() client API name}.
     *
     * @param language  A string whose value is equal to either {@link #getDatabaseName() database name} or
     * {@link #getPathName() client API name}
     * @param nameExtractor  If {@code language} matches {@link #getDatabaseName()}, use {@link #getDatabaseName()};
     * otherwise use {@link #getPathName()}
     *
     * @return a new instance
     *
     * @throws IllegalArgumentException if the language name is not a valid one
     */
    private static Language valueOf(@NotNull final String language, final Function<Language, String> nameExtractor) {
        return Arrays.stream(values())
                .filter(value -> nameExtractor.apply(value).equals(language))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(
                                "'%s' is not a recognized language. Acceptable ones are %s",
                                language,
                                Arrays.stream(values())
                                        .map(nameExtractor)
                                        .collect(Collectors.joining(", "))
                        )
                ));
    }

    @NotNull
    public String getPathName() {
        return pathName;
    }

    @NotNull
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String toString() {
        return getDatabaseName();
    }
}
