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
package org.qubitpi.wilhelm.web.filters;

import org.qubitpi.wilhelm.Language;
import org.qubitpi.wilhelm.LanguageCheck;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * A {@link ContainerRequestFilter} that validates the {@code language} path param in endpoint requests and only applies
 * to those with "languages/{language}" in the middle of its endpoint path, for example "/languages/{language}/count".
 * <p>
 * Endpoints wishing to be validated by this filter must satisfy 2 requirements:
 * <ol>
 *     <li> having a {@link jakarta.ws.rs.PathParam "language" @PathParam}
 *     <li> the resource/endpoint method has been annotated with {@link LanguageCheck}
 * </ol>
 */
@Immutable
@ThreadSafe
@Provider
@LanguageCheck
public class LanguageCheckFilter implements ContainerRequestFilter {

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) {
        final String requestedLanguage = containerRequestContext
                .getUriInfo()
                .getPathParameters()
                .get("language")
                .get(0);
        try {
            Language.ofClientValue(requestedLanguage);
        } catch (final IllegalArgumentException exception) {
            containerRequestContext.abortWith(
                    Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(exception.getMessage())
                            .build()
            );
        }
    }
}
