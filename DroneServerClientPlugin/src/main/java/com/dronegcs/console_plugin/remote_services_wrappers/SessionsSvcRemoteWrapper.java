package com.dronegcs.console_plugin.remote_services_wrappers;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionsSvcRemoteWrapper {

    private final static Logger LOGGER = LoggerFactory.getLogger(SessionsSvcRemoteWrapper.class);


    @Autowired
    private RestClientHelper restClientHelper;

    public void publish() {
        LOGGER.debug("PUBLISH Start");
        WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("publish");
        ClientResponse response = builder.post(ClientResponse.class);
        ClientResponse.Status status = response.getClientResponseStatus();

        if (response.getClientResponseStatus() != ClientResponse.Status.OK)
            throw new RuntimeException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

        LOGGER.debug("PUBLISH End");
    }

    public void discard() {
        LOGGER.debug("DISCARD Start");
        WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("discard");
        ClientResponse response = builder.post(ClientResponse.class);
        ClientResponse.Status status = response.getClientResponseStatus();

        if (response.getClientResponseStatus() != ClientResponse.Status.OK)
            throw new RuntimeException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

        LOGGER.debug("DISCARD End");
    }
}
