package com.dronegcs.console_plugin.remote_services_wrappers;

import com.db.persistence.scheme.KeepAliveResponse;
import com.db.persistence.scheme.LoginRequest;
import com.db.persistence.scheme.LoginResponse;
import com.db.persistence.scheme.LogoutResponse;
import com.dronegcs.console_plugin.plugin_event.ClientPluginEvent;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.ConnectException;

import static com.db.persistence.scheme.LoginLogoutStatus.FAIL;

@Component
public class LoginSvcRemoteWrapper {

    private final static Logger LOGGER = LoggerFactory.getLogger(LoginSvcRemoteWrapper.class);

    @Autowired
    private RestClientHelper restClientHelper;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private boolean keepAliveEnable = false;

    public LoginResponse login(LoginRequest loginRestRequest, String pass, String server, int port) {
        LoginResponse loginResponse;
        try {
            restClientHelper.setServerIp(server);
            restClientHelper.setServerPort(port);
            restClientHelper.setUsernamePassword(loginRestRequest.getUserName(), pass);
            restClientHelper.setToken(null);
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("login");
            ObjectMapper mapper = new ObjectMapper();

            LOGGER.debug("Request to be send: {} " + mapper.writeValueAsString(loginRestRequest));
            ClientResponse response = builder.post(ClientResponse.class, mapper.writeValueAsString(loginRestRequest));
            loginResponse = restClientHelper.resolveResponse(response, LoginResponse.class);
            LOGGER.debug("loginRestResponse: {}", loginResponse.getDate(), loginResponse.getMessage());

            LOGGER.debug("Activate Keep Alive");
            keepAliveEnable = true;
        }
        catch (Exception e) {
            loginResponse = new LoginResponse();
            loginResponse.setReturnCode(FAIL);
            loginResponse.setMessage(e.getMessage());
            return loginResponse;
        }

        return loginResponse;
    }

    public LogoutResponse logout() {
        try {
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("logout");
            ClientResponse response = builder.post(ClientResponse.class);
            LogoutResponse resp =  restClientHelper.resolveResponse(response, LogoutResponse.class);
//            System.out.println("logoutRestResponse: " + resp.getDate() + " " +  resp.getMessage());
            LOGGER.debug("logoutRestResponse: {} {}", resp.getDate(), resp.getMessage());
            return resp;
        }
        catch (ConnectException e) {
            LOGGER.error("Connection to server failed", e);
            throw new RuntimeException(e); //TODO: handle connection issue nicely
        }
        catch (Exception e) {
            LOGGER.error("Failed to read object", e);
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate=30 * 1000) // 30 Seconds
    public void loginKeepAlive() {
        try {
            if (!keepAliveEnable)
                return;
            LOGGER.debug("Tik!");
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("keepAlive");
            ClientResponse response = builder.post(ClientResponse.class);
            KeepAliveResponse resp = restClientHelper.resolveResponse(response, KeepAliveResponse.class);
            LOGGER.debug("Server Time: " + resp.getMessage() + " ," + resp.getServerDate() + " ," + resp.getReturnCode());
            //TODO: Get server time
            applicationEventPublisher.publishEvent(ClientPluginEvent.generate(ClientPluginEvent.TYPE.SERVER_CLOCK).add(resp.getServerDate()));
        }
        catch (Exception e) {
            LOGGER.error("Keep alive failed, terminating communication channel", e);
            keepAliveEnable = false;
            applicationEventPublisher.publishEvent(ClientPluginEvent.generate(ClientPluginEvent.TYPE.SERVER_LOST));
        }
    }

}
