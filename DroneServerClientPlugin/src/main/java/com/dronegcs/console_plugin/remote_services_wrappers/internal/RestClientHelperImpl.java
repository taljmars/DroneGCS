package com.dronegcs.console_plugin.remote_services_wrappers.internal;

import com.db.persistence.scheme.BaseObject;
import com.dronegcs.console_plugin.remote_services_wrappers.RestClientHelper;
import com.generic_tools.Pair.Pair;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

@Component
public class RestClientHelperImpl implements RestClientHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestClientHelperImpl.class);

    private static final String SERVER_URL = "http://localhost:8080/";
//    private static final String SERVER_URL = "http://localhost:8081/ServerCore-1.5.8.RELEASE/";

    private Client client;
    private String token;
    private String hashedUsernamePassword;

    @PostConstruct
    public void init() {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJsonProvider.class);
        client = Client.create(config);
    }

//    public WebResource.Builder getWebResource(String path) {
//        WebResource.Builder builder = getWebResource(path, null, null);
//        return build(builder);
//    }

//    public WebResource.Builder getWebResource(String path, String s, Object... objs) {
    @Override
    public WebResource.Builder getWebResource(String path, Object... objs) {
        UriBuilder uriBuilder  = UriBuilder.fromUri(SERVER_URL + path);
        WebResource webResource = client.resource(uriBuilder.build());
        WebResource.Builder builder;
        if (objs.length % 2 != 0) {
            throw new RuntimeException("Unexpected amount of paramteres");
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
//        UriBuilder uriBuilder  = UriBuilder.fromUri(SERVER_URL + path);
//        if (s != null && !s.isEmpty() && objs != null)
//            uriBuilder.queryParam(s, objs);
//        System.out.println("Address: " + uriBuilder.build());
//        LOGGER.debug("Address: " + uriBuilder.build());
//        WebResource.Builder builder = client.resource(uriBuilder.build()).getRequestBuilder();
        builder.type(MediaType.APPLICATION_JSON_TYPE);
        return build(builder);
    }

//    public WebResource.Builder getWebResource(String path, MultivaluedMap queries) {
//        UriBuilder uriBuilder  = UriBuilder.fromUri(SERVER_URL + path);
//        WebResource.Builder builder = client.resource(uriBuilder.build()).queryParams(queries).getRequestBuilder();
//        builder.type(MediaType.APPLICATION_JSON_TYPE);
//        return build(builder);
//    }

    private WebResource.Builder build(WebResource.Builder builder) {
        builder
//                .header(CONTENT_TYPE, "application/json")
                .header(ACCEPT, "application/json")
                .header(AUTHORIZATION, "Basic " + hashedUsernamePassword)
                .header("token", getToken());
        return builder;
    }

    @Override
    public <T extends BaseObject> T resolve(JSONObject jsonObject) throws Exception {
        LOGGER.debug("resolving - {}", jsonObject);
        if (!jsonObject.has("clz"))
            return null;

        Object val = jsonObject.get("clz");
//        ObjectMapper mapper = new ObjectMapper();
//        T obj = mapper.readValue(jsonObject.toString(), (Class<T>) Class.forName(val.toString()));
//        return obj;
        return resolve(jsonObject, (Class<T>) Class.forName(val.toString()));
    }

    @Override
    public <T> T resolve(JSONObject jsonObject, Class<T> clz) throws Exception {
        LOGGER.debug("resolving - {} - {}", jsonObject, clz.getSimpleName());
        ObjectMapper mapper = new ObjectMapper();
        T obj = mapper.readValue(jsonObject.toString(), clz);
        return obj;
    }

    @Override
    public Pair<Class, ? extends Exception> getErrorAndMessage(ClientResponse response) throws Exception {
        String jsonString = response.getEntity(String.class);
        JSONObject jsonObject;
        try { jsonObject = new JSONObject(jsonString); }
        catch (JSONException e) {
            LOGGER.error("Failed to parse JSON:\n" + jsonString);
            throw new IOException(e);
        }
        return getErrorAndMessageFromJson(jsonObject);
    }

    @Override
    public Pair<Class, ? extends Exception> getErrorAndMessageFromJson(JSONObject jsonObject) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String actualClass = (String) jsonObject.remove("clz");
        Class cls = Class.forName(actualClass);
        Exception exception = mapper.readValue(jsonObject.toString(), Exception.class);
        return new Pair<>(cls, exception);
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
    public void setHashedUsernamePassword(String hashedUsernamePassword) {
        this.hashedUsernamePassword = hashedUsernamePassword;
    }

    public String getHashedUsernamePassword() {
        return hashedUsernamePassword;
    }
}
