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
package com.paiondata.wilhelm.application;

import com.paiondata.wilhelm.web.filters.CorsFilter;

import org.glassfish.hk2.utilities.Binder;
import com.paiondata.wilhelm.web.filters.LanguageCheckFilter;

import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * The resource configuration for the web applications.
 */
@Immutable
@ThreadSafe
@ApplicationPath("v1")
public class ResourceConfig extends org.glassfish.jersey.server.ResourceConfig {

    private static final String ENDPOINT_PACKAGE = "com.paiondata.wilhelm.web.endpoints";

    /**
     * DI Constructor that allows for finer dependency injection control.
     */
    @Inject
    public ResourceConfig() {
        packages(ENDPOINT_PACKAGE);

        register(CorsFilter.class);
        register(LanguageCheckFilter.class);

        final Binder binder = new BinderFactory().buildBinder();
        register(binder);
    }
}
