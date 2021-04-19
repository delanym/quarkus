package org.jboss.resteasy.reactive.client.impl;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import org.jboss.resteasy.reactive.client.handlers.ClientRequestFilterRestHandler;
import org.jboss.resteasy.reactive.client.handlers.ClientResponseCompleteRestHandler;
import org.jboss.resteasy.reactive.client.handlers.ClientResponseFilterRestHandler;
import org.jboss.resteasy.reactive.client.handlers.ClientSendRequestHandler;
import org.jboss.resteasy.reactive.client.handlers.ClientSetResponseEntityRestHandler;
import org.jboss.resteasy.reactive.client.spi.ClientRestHandler;
import org.jboss.resteasy.reactive.common.jaxrs.ConfigurationImpl;

class HandlerChain {

    private static final ClientRestHandler[] EMPTY_REST_HANDLERS = new ClientRestHandler[0];

    private final ClientRestHandler clientSendHandler;
    private final ClientRestHandler clientSetResponseEntityRestHandler;
    private final ClientRestHandler clientResponseCompleteRestHandler;

    public HandlerChain(boolean followRedirects) {
        this.clientSendHandler = new ClientSendRequestHandler(followRedirects);
        this.clientSetResponseEntityRestHandler = new ClientSetResponseEntityRestHandler();
        this.clientResponseCompleteRestHandler = new ClientResponseCompleteRestHandler();
    }

    ClientRestHandler[] toRestHandler(ConfigurationImpl configuration) {
        List<ClientRequestFilter> requestFilters = configuration.getRequestFilters();
        List<ClientResponseFilter> responseFilters = configuration.getResponseFilters();
        if (requestFilters.isEmpty() && responseFilters.isEmpty()) {
            return new ClientRestHandler[] { clientSendHandler, clientSetResponseEntityRestHandler,
                    clientResponseCompleteRestHandler };
        }
        List<ClientRestHandler> result = new ArrayList<>(3 + requestFilters.size() + responseFilters.size());
        for (int i = 0; i < requestFilters.size(); i++) {
            result.add(new ClientRequestFilterRestHandler(requestFilters.get(i)));
        }
        result.add(clientSendHandler);
        result.add(clientSetResponseEntityRestHandler);
        for (int i = 0; i < responseFilters.size(); i++) {
            result.add(new ClientResponseFilterRestHandler(responseFilters.get(i)));
        }
        result.add(clientResponseCompleteRestHandler);
        return result.toArray(EMPTY_REST_HANDLERS);
    }
}
