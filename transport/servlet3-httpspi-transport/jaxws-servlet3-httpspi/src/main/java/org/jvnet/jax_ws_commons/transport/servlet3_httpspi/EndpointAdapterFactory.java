/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package org.jvnet.jax_ws_commons.transport.servlet3_httpspi;


import javax.xml.transform.Source;
import javax.xml.ws.Endpoint;
import javax.xml.ws.spi.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
/**
 * @author Jitendra Kotamraju
*/
public final class EndpointAdapterFactory {
    private static final Logger LOGGER = Logger.getLogger(EndpointAdapterFactory.class.getName());

    private final EndpointContextImpl appContext;

    public EndpointAdapterFactory() {
        this.appContext = new EndpointContextImpl();
    }

    public EndpointAdapter createAdapter(EndpointInfo endpointInfo) {

        LOGGER.info("Creating Endpoint using JAX-WS 2.2 HTTP SPI");
        InvokerImpl endpointInvoker = new InvokerImpl(endpointInfo.implType);
        Endpoint endpoint = Provider.provider().createEndpoint(endpointInfo.bindingId,
                endpointInfo.implType, endpointInvoker, endpointInfo.features);
        appContext.add(endpoint);
        endpoint.setEndpointContext(appContext);

        // Use DD's service name, port names as WSDL_SERVICE and WSDL_PORT
        if (endpointInfo.portName != null || endpointInfo.serviceName != null) {
            Map<String, Object> props = new HashMap<String, Object>();
            if (endpointInfo.portName != null) {
                props.put(Endpoint.WSDL_PORT, endpointInfo.portName);
            }
            if (endpointInfo.serviceName != null) {
                props.put(Endpoint.WSDL_SERVICE, endpointInfo.serviceName);
            }
            LOGGER.info("Setting Endpoint Properties="+props);
            endpoint.setProperties(props);
        }

        // Set bundle's wsdl, xsd docs as metadata
        if (endpointInfo.metadata != null) {
            endpoint.setMetadata(endpointInfo.metadata);
            List<String> docId = new ArrayList<String>();
            for(Source source : endpointInfo.metadata) {
                docId.add(source.getSystemId());
            }
            LOGGER.info("Setting metadata="+docId);
        }

        // Set DD's handlers
        // endpoint.getBinding().setHandlerChain(binding.getHandlerChain());

        return new EndpointAdapter(endpoint, endpointInfo.urlPattern);
    }

}