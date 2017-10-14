package com.dronegcs.console_plugin.remote_services_wrappers;

import com.db.persistence.wsSoap.SessionsSvcRemote;
import com.dronegcs.console_plugin.remote_services_wrappers.internal.RestClientHelper;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionsSvcRemoteWrapper {

//    @Autowired
    private SessionsSvcRemote sessionsSvcRemote;

    @Autowired
    private RestClientHelper restClientHelper;

    public void publish() {
        WebResource.Builder builder = restClientHelper.getWebResource("publish");
        ClientResponse response = builder.post(ClientResponse.class);
        ClientResponse.Status status = response.getClientResponseStatus();

        if (response.getClientResponseStatus() != ClientResponse.Status.OK)
            throw new RuntimeException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

//        sessionsSvcRemote.publish();
    }

    public void discard() {
        WebResource.Builder builder = restClientHelper.getWebResource("discard");
        ClientResponse response = builder.post(ClientResponse.class);
        ClientResponse.Status status = response.getClientResponseStatus();

        if (response.getClientResponseStatus() != ClientResponse.Status.OK)
            throw new RuntimeException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

        //        sessionsSvcRemote.discard();
    }
}
