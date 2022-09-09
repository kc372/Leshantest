/*******************************************************************************
 * Copyright (c) 2022 Sierra Wireless and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.client.californium.endpoint.coap;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.util.List;

import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.CoapEndpoint.Builder;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.EndpointContext;
import org.eclipse.californium.elements.EndpointContextMatcher;
import org.eclipse.californium.elements.UDPConnector;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.leshan.client.californium.CaliforniumConnectionController;
import org.eclipse.leshan.client.californium.CaliforniumIdentityExtractor;
import org.eclipse.leshan.client.californium.enpoint.CaliforniumEndpointFactory;
import org.eclipse.leshan.client.endpoint.ClientEndpointToolbox;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.client.servers.ServerInfo;
import org.eclipse.leshan.core.endpoint.Protocol;
import org.eclipse.leshan.core.request.Identity;

public class CoapEndpointFactory implements CaliforniumEndpointFactory {

    private final String loggingTag = null;
    protected EndpointContextMatcher unsecuredContextMatcher = null;
    protected InetSocketAddress addr = null;

    public CoapEndpointFactory(InetSocketAddress addr) {
        this.addr = addr;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.COAP;
    }

    @Override
    public Endpoint createEndpoint(Configuration defaultConfiguration, ServerInfo serverInfo,
            boolean clientInitiatedOnly, List<Certificate> trustStore, ClientEndpointToolbox toolbox) {
        return createUnsecuredEndpointBuilder(addr, serverInfo, defaultConfiguration).build();
    }

    /**
     * This method is intended to be overridden.
     *
     * @param address the IP address and port, if null the connector is bound to an ephemeral port on the wildcard
     *        address.
     * @param coapConfig the CoAP config used to create this endpoint.
     * @return the {@link Builder} used for unsecured communication.
     */
    protected CoapEndpoint.Builder createUnsecuredEndpointBuilder(InetSocketAddress addr, ServerInfo serverInfo,
            Configuration coapConfig) {
        CoapEndpoint.Builder builder = new CoapEndpoint.Builder();
        builder.setConnector(createUnsecuredConnector(addr, coapConfig));
        builder.setConfiguration(coapConfig);
        if (loggingTag != null) {
            builder.setLoggingTag("[" + loggingTag + "-coap://]");
        } else {
            builder.setLoggingTag("[coap://]");
        }
        if (unsecuredContextMatcher != null) {
            builder.setEndpointContextMatcher(unsecuredContextMatcher);
        }
        return builder;
    }

    /**
     * By default create an {@link UDPConnector}.
     * <p>
     * This method is intended to be overridden.
     *
     * @param address the IP address and port, if null the connector is bound to an ephemeral port on the wildcard
     *        address
     * @param coapConfig the Configuration
     * @return the {@link Connector} used for unsecured {@link CoapEndpoint}
     */
    protected Connector createUnsecuredConnector(InetSocketAddress address, Configuration coapConfig) {
        return new UDPConnector(address, coapConfig);
    }

    @Override
    public CaliforniumIdentityExtractor createIdentityExtractor() {
        return new CaliforniumIdentityExtractor() {
            @Override
            public Identity getIdentity(CoapExchange exchange) {
                EndpointContext context = getForeignPeerContext(exchange);
                InetSocketAddress peerAddress = context.getPeerAddress();
                return Identity.unsecure(peerAddress);
            }
        };
    }

    @Override
    public CaliforniumConnectionController createConnectionController() {
        return new CaliforniumConnectionController() {
            @Override
            public void forceReconnection(Endpoint endpoint, ServerIdentity identity, boolean resume) {
                // no connection in coap, so nothing to do;
            }
        };
    }
}
