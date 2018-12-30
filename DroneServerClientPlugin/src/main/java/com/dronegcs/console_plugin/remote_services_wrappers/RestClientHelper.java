package com.dronegcs.console_plugin.remote_services_wrappers;

import com.db.persistence.scheme.BaseObject;
import com.generic_tools.Pair.Pair;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONObject;

public interface RestClientHelper {

    <T extends Object> T resolveResponse(ClientResponse response, Class<T> clz) throws Exception;

    void setToken(String token);

    String getToken();

    WebResource.Builder getWebResourceWithAuth(String apiPath, Object... param);

    WebResource.Builder getWebResourceNoAuth(String apiPath, Object... param);

    Pair<Class,? extends Exception> getErrorAndMessage(ClientResponse response) throws Exception;

    <T extends BaseObject> T resolve(JSONObject jsonObject) throws ClassNotFoundException, Exception;

    <T extends Object> T resolve(JSONObject jsonObject, Class<T> classToResolve) throws Exception;

    Pair<Class,? extends Exception> getErrorAndMessageFromJson(JSONObject jsonObject) throws Exception;

    void setUsernamePassword(String userName, String pass);

    void setServerPort(int port);

    void setServerIp(String server);
}
