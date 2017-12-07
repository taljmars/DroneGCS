package com.dronegcs.console_plugin.remote_services_wrappers.internal;

import com.db.persistence.scheme.BaseObject;
import com.generic_tools.Pair.Pair;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
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

@Component
public class RestClientHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestClientHelper.class);

    private static final String SERVER_URL = "http://localhost:8080/";
//    private static final String SERVER_URL = "http://localhost:8081/ServerCore-1.5.8.RELEASE/";

    private Client client;

    @PostConstruct
    public void init() {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJsonProvider.class);
        client = Client.create(config);
    }

    public WebResource.Builder getWebResource(String path) {
        return getWebResource(path, null, null);
    }

    public WebResource.Builder getWebResource(String path, String s, Object... objs) {
        UriBuilder uriBuilder  = UriBuilder.fromUri(SERVER_URL + path);
        if (s != null && !s.isEmpty() && objs != null)
            uriBuilder.queryParam(s, objs);
        WebResource webResource = client.resource(uriBuilder.build());
        return webResource.type(MediaType.APPLICATION_JSON_TYPE);
    }

    public WebResource.Builder getWebResource(String path, MultivaluedMap queries) {
        UriBuilder uriBuilder  = UriBuilder.fromUri(SERVER_URL + path);
        WebResource webResource = client.resource(uriBuilder.build());
        webResource = webResource.queryParams(queries);
        return webResource.type(MediaType.APPLICATION_JSON_TYPE);
    }

    public <T extends BaseObject> T resolve(JSONObject jsonObject) throws ClassNotFoundException, IOException {
        LOGGER.debug("resolving - {}", jsonObject);
        if (!jsonObject.has("clz"))
            return null;

        Object val = jsonObject.get("clz");
        ObjectMapper mapper = new ObjectMapper();
        T obj = mapper.readValue(jsonObject.toString(), (Class<T>) Class.forName(val.toString()));
        return obj;
    }

    public Pair<Class, ? extends Exception> getErrorAndMessage(ClientResponse response) throws ClassNotFoundException, IOException {
        String jsonString = response.getEntity(String.class);
        JSONObject jsonObject;
        try { jsonObject = new JSONObject(jsonString); }
        catch (JSONException e) {
            System.out.println("Failed to parse JSON:\n" + jsonString);
            throw new IOException(e);
        }
        return getErrorAndMessageFromJson(jsonObject);
    }

    public Pair<Class, ? extends Exception> getErrorAndMessageFromJson(JSONObject jsonObject) throws ClassNotFoundException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String actualClass = (String) jsonObject.remove("clz");
        Class cls = Class.forName(actualClass);
        Exception exception = mapper.readValue(jsonObject.toString(), Exception.class);
        return new Pair<>(cls, exception);
    }

}
