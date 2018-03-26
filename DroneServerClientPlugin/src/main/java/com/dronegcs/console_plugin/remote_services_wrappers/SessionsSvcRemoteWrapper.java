package com.dronegcs.console_plugin.remote_services_wrappers;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionsSvcRemoteWrapper {

    @Autowired
    private RestClientHelper restClientHelper;

    public void publish() {
//        MultivaluedMap formData = new MultivaluedMapImpl();
//        formData.add("token", restClientHelper.getToken());
//        WebResource.Builder builder = restClientHelper.getWebResource("publish", formData);
        WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("publish");
        ClientResponse response = builder.post(ClientResponse.class);
        ClientResponse.Status status = response.getClientResponseStatus();

        if (response.getClientResponseStatus() != ClientResponse.Status.OK)
            throw new RuntimeException(status.getReasonPhrase() + ", status:" + status.getStatusCode());
    }

    public void discard() {
//        MultivaluedMap formData = new MultivaluedMapImpl();
//        formData.add("token", restClientHelper.getToken());
//        WebResource.Builder builder = restClientHelper.getWebResource("discard", formData);
        WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("discard");
        ClientResponse response = builder.post(ClientResponse.class);
        ClientResponse.Status status = response.getClientResponseStatus();

        if (response.getClientResponseStatus() != ClientResponse.Status.OK)
            throw new RuntimeException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

        //        sessionsSvcRemote.discard();
    }
}
