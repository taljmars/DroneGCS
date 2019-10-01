package com.dronegcs.console_plugin.remote_services_wrappers;

import com.db.persistence.scheme.BaseObject;
import com.generic_tools.Pair.Pair;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public interface RestClientHelper {

    class InactiveRestClient extends Exception {

        public InactiveRestClient(String error_message) {
            super(error_message);
        }

        public InactiveRestClient() {
            super("Rest Client wasn't initialized");
        }

    }

    final static Logger LOGGER = LoggerFactory.getLogger(RestClientHelper.class);

    <T extends Object> T resolveResponse(ClientResponse response, Class<T> clz) throws Exception;

    void setToken(String token);

    String getToken();

    WebResource.Builder getWebResourceWithAuth(String apiPath, Object... param) throws InactiveRestClient;

    WebResource.Builder getWebResourceNoAuth(String apiPath, Object... param) throws InactiveRestClient;

    static Pair<Class, ? extends Exception> getErrorAndMessage(ClientResponse response) throws Exception {
        String jsonString = response.getEntity(String.class);
        JSONObject jsonObject;
        try { jsonObject = new JSONObject(jsonString); }
        catch (JSONException e) {
            LOGGER.error("Failed to parse JSON:\n" + jsonString);
            throw new IOException(e);
        }
        return getErrorAndMessageFromJson(jsonObject);
    }

    static  <T extends BaseObject> T resolve(JSONObject jsonObject) throws Exception {
        LOGGER.debug("resolving - {}", jsonObject);
        if (!jsonObject.has("clz"))
            return null;

        Object val = jsonObject.get("clz");
//        ObjectMapper mapper = new ObjectMapper();
//        T obj = mapper.readValue(jsonObject.toString(), (Class<T>) Class.forName(val.toString()));
//        return obj;
        return resolve(jsonObject, (Class<T>) Class.forName(val.toString()));
    }

    static <T extends Object> T resolve(JSONObject jsonObject, Class<T> classToResolve) throws Exception  {
        LOGGER.debug("resolving - {} - {}", jsonObject, classToResolve.getSimpleName());
        ObjectMapper mapper = new ObjectMapper();
        T obj = mapper.readValue(jsonObject.toString(), classToResolve);
        return obj;
    }

    static Pair<Class, ? extends Exception> getErrorAndMessageFromJson(JSONObject jsonObject) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String actualClass = (String) jsonObject.remove("clz");
        Class cls = Class.forName(actualClass);
        Exception exception = mapper.readValue(jsonObject.toString(), Exception.class);
        return new Pair<>(cls, exception);
    }

    void setUsernamePassword(String userName, String pass);

    void setServerPort(int port);

    void setServerIp(String server);
}
