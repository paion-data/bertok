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
package org.qubitpi.wilhelm.config;

import org.aeonbits.owner.Config;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * {@link ApplicationConfig} provides an interface for retrieving configuration values, allowing for implicit type
 * conversion, defaulting, and use of a runtime properties interface to override configured settings.
 * <p>
 * {@link ApplicationConfig} tries to load the configurations from several sources in the following order:
 * <ol>
 *     <li> the <a href="https://docs.oracle.com/javase/tutorial/essential/environment/env.html">
 *          operating system's environment variables</a>; for instance, an environment variable can be set with
 *          {@code export EXAMPLE_CONFIG_KEY_NAME="foo"}
 *     <li> the <a href="https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html">
 *          Java system properties</a>; for example, a Java system property can
 *          be set using {@code System.setProperty("EXAMPLE_CONFIG_KEY_NAME", "foo")}
 *     <li> a file named <b>application.properties</b> placed under CLASSPATH. This file can be put under
 *          {@code src/main/resources} source directory with contents, for example, {@code EXAMPLE_CONFIG_KEY_NAME=foo}
 * </ol>
 * Note that environment config has higher priority than Java system properties. Java system properties have higher
 * priority than file based configuration.
 */
@Immutable
@ThreadSafe
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"system:env", "system:properties", "classpath:application.properties"})
public interface ApplicationConfig extends Config {

    /**
     * The URL to a Neo4j instance.
     *
     * @return a string
     */
    @Key("NEO4J_URI")
    String neo4jUrl();

    /**
     * The "principal" of the basic authentication scheme, using a username and a password.
     * <p>
     * It is used to create the {@link org.neo4j.driver.AuthTokens#basic(String, String) token} representing the
     * principal and is the "username" part.
     *
     * @return a string
     */
    @Key("NEO4J_USERNAME")
    String neo4jUsername();

    /**
     * The "credential" of the basic authentication scheme, using a username and a password.
     * <p>
     * It is used to create the {@link org.neo4j.driver.AuthTokens#basic(String, String) token} representing the
     * principal and is the "password" part.
     *
     * @return a string
     */
    @Key("NEO4J_PASSWORD")
    String neo4jPassword();

    /**
     * The name of the Neo4J database that's backing this webservice.
     *
     * @return a string
     */
    @Key("NEO4J_DATABASE")
    String neo4jDatabase();
}
