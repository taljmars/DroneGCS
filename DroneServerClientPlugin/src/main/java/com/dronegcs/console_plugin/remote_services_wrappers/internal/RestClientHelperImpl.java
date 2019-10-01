package com.dronegcs.console_plugin.remote_services_wrappers.internal;

import com.dronegcs.console_plugin.remote_services_wrappers.RestClientHelper;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;

@Component
public class RestClientHelperImpl implements RestClientHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestClientHelperImpl.class);

//    private static final String SERVER_URL = "http://localhost:8080/";
//    private static final String SERVER_URL = "http://localhost:8081/ServerCore-1.5.8.RELEASE/";

    private Client client;
    private String token;
    private String userName;
    private String password;
    private int port;
    private String ip;

    @PostConstruct
    public void init() {
        token = null;
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJsonProvider.class);
        client = Client.create(config);
    }

    @Override
    public WebResource.Builder getWebResourceNoAuth(String path, Object... objs) throws InactiveRestClient{
        if (getToken() == null)
            throw new InactiveRestClient("Token not initialized");

        return getWebResource(false, path, objs);
    }

    @Override
    public WebResource.Builder getWebResourceWithAuth(String path, Object... objs) throws InactiveRestClient{
        if (getToken() == null)
            throw new InactiveRestClient("Token not initialized");

        return getWebResource(true, path, objs);
    }

    public WebResource.Builder getWebResource(boolean auth, String path, Object... objs) {
        UriBuilder uriBuilder  = UriBuilder.fromUri("http://" + ip + ":" + port + "/" + path);
        WebResource webResource = client.resource(uriBuilder.build());
        WebResource.Builder builder;
        if (objs.length % 2 != 0) {
            throw new RuntimeException("Unexpected amount of parameters");
        }
        if (objs.length != 0) {
            MultivaluedMap multivaluedMap = new MultivaluedMapImpl();
            for (int i = 0 ; i < objs.length ; i = i + 2)
                multivaluedMap.add(objs[i], objs[i+1]);
            builder = webResource.queryParams(multivaluedMap).getRequestBuilder();
        }
        else {
            builder = webResource.getRequestBuilder();
        }

        builder.type(MediaType.APPLICATION_JSON_TYPE);
        return build(builder, auth);
    }

    private WebResource.Builder build(WebResource.Builder builder, boolean auth) {
        builder
//                .header(CONTENT_TYPE, "application/json")
                .header(ACCEPT, "application/json");

        if (auth) {
            if (getToken() == null) {
//            builder.header(AUTHORIZATION, "Basic " + hashedUsernamePassword);
                builder.header("X-Auth-Username", userName);
                builder.header("X-Auth-Password", password);
            }
            else {
                builder.header("X-Auth-Token", getToken());
            }
        }
//        System.out.println("Header: " + AUTHORIZATION + " Basic " + hashedUsernamePassword + " token" + getToken());
        return builder;
    }

    @Override
    public <T extends Object> T resolveResponse(ClientResponse response, Class<T> clz) throws Exception {
        ClientResponse.Status status = response.getClientResponseStatus();

        switch (status) {
            case OK:
                break;
            default:
                throw new RuntimeException(status.getReasonPhrase() + "(" + status.getStatusCode() + ")");
        }

        if (!response.hasEntity())
            throw new RuntimeException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

        String jsonString = response.getEntity(String.class);

        JSONObject jsonObject = new JSONObject(jsonString);
        T resp = RestClientHelper.resolve(jsonObject, clz);
        return resp;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setUsernamePassword(String userName, String pass) {
        this.userName = userName;
        this.password = pass;
    }

    @Override
    public void setServerPort(int port) {
        this.port = port;
    }

    @Override
    public void setServerIp(String server) {
        this.ip = server;
    }
}
