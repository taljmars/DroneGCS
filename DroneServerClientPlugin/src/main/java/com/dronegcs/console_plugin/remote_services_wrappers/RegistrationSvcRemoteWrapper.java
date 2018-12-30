package com.dronegcs.console_plugin.remote_services_wrappers;

import com.db.persistence.scheme.RegistrationRequest;
import com.db.persistence.scheme.RegistrationResponse;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegistrationSvcRemoteWrapper {

    private final static Logger LOGGER = LoggerFactory.getLogger(RegistrationSvcRemoteWrapper.class);

    @Autowired
    private RestClientHelper restClientHelper;

    public RegistrationResponse registerNewUser(RegistrationRequest registrationRequest, String server, int port) {
        RegistrationResponse registrationResponse;
        try {
            restClientHelper.setServerIp(server);
            restClientHelper.setServerPort(port);
            WebResource.Builder builder = restClientHelper.getWebResourceNoAuth("registerNewUser");
            ObjectMapper mapper = new ObjectMapper();
            LOGGER.debug("Request to be send: {} " + mapper.writeValueAsString(registrationRequest));
            ClientResponse response = builder.post(ClientResponse.class, mapper.writeValueAsString(registrationRequest));
            registrationResponse = restClientHelper.resolveResponse(response, RegistrationResponse.class);
            LOGGER.debug("RegistrationRestResponse: {}", registrationResponse.getDate(), registrationResponse.getMessage());
        }
        catch (Exception e) {
            registrationResponse = new RegistrationResponse();
            registrationResponse.setReturnCode(-1);
            registrationResponse.setMessage(e.getMessage());
            return registrationResponse;
        }

        return registrationResponse;
    }

}
