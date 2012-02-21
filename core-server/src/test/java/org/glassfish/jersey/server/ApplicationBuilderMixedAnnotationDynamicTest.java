/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.server;

import org.glassfish.jersey.message.internal.Requests;
import org.glassfish.jersey.message.internal.Responses;
import org.glassfish.jersey.process.Inflector;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class ApplicationBuilderMixedAnnotationDynamicTest {

    static volatile String name = "Lady";

    @Path("/name")
    public static class NameResource {

        @GET
        @Produces("text/plain")
        public String getMethod() {
            return name;
        }
    }

    @Test
    public void testPutGet() throws Exception {
        final ResourceConfig resourceConfig = ResourceConfig.builder().addClasses(NameResource.class).build();
        final Application.Builder appBuilder = Application.builder(resourceConfig);

        appBuilder.bind("/name").method("PUT").to(new Inflector<Request, Response>() {

            @Override
            public Response apply(@Nullable Request request) {
                name = request.readEntity(String.class);
                return Responses.empty().status(200).build();
            }
        });
        Application application = appBuilder.build();

        final Response response = application.apply(Requests.from("", "name", "PUT").entity("Gaga").type(MediaType.TEXT_PLAIN).build()).get();
        assertEquals(200, response.getStatus());

        assertEquals("Gaga", application.apply(
                Requests.from("", "name", "GET").accept(MediaType.TEXT_PLAIN).build()).get().readEntity(String.class));
    }
}
