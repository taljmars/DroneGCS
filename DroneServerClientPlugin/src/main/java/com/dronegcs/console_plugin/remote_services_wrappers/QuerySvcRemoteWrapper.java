package com.dronegcs.console_plugin.remote_services_wrappers;

import com.db.persistence.scheme.BaseObject;
import com.db.persistence.wsSoap.QueryRequestRemote;
import com.db.persistence.wsSoap.QueryResponseRemote;
import com.db.persistence.wsSoap.QuerySvcRemote;
import com.dronegcs.console_plugin.remote_services_wrappers.internal.RestClientHelper;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

@Component
public class QuerySvcRemoteWrapper {

    private final static Logger LOGGER = LoggerFactory.getLogger(QuerySvcRemoteWrapper.class);

//    @Autowired
    private QuerySvcRemote querySvcRemote;

    @Autowired
    private RestClientHelper restClientHelper;

    public QueryResponseRemote query(QueryRequestRemote queryRequestRemote) {
        try {
            WebResource.Builder builder = restClientHelper.getWebResource("queryForUser", "userName", ObjectCrudSvcRemoteWrapper.userNametest);
            ObjectMapper mapper = new ObjectMapper();

            LOGGER.debug("Request to be send: {} " + mapper.writeValueAsString(queryRequestRemote));
            ClientResponse response = builder.post(ClientResponse.class, mapper.writeValueAsString(queryRequestRemote));
            return resolveResponse(response);
        }
        catch (ConnectException e) {
            LOGGER.error("Connection to server failed", e);
            throw new RuntimeException(e); //TODO: handle connection issue nicely
        }
        catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Failed to read object", e);
            throw new RuntimeException(e);
        }
    }

    public <T extends BaseObject> QueryResponseRemote runNativeQueryWithClass(String queryString, String clz) {
        try {
            //        return querySvcRemote.runNativeQuery(queryString, clz);
            MultivaluedMap multivaluedMap = new MultivaluedMapImpl();
            multivaluedMap.add("queryString", queryString);
            multivaluedMap.add("clz", clz);
            multivaluedMap.add("userName", ObjectCrudSvcRemoteWrapper.userNametest);
            WebResource.Builder builder = restClientHelper.getWebResource("runNativeQueryWithClassForUser", multivaluedMap);
            ClientResponse response = builder.get(ClientResponse.class);
            return resolveResponse(response);
        }
        catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Failed to read object", e);
            throw new RuntimeException(e);
        }
    }

    public <T extends BaseObject> QueryResponseRemote runNativeQuery(String queryString) {
        try {
            //        return querySvcRemote.runNativeQuery(queryString, clz);
            MultivaluedMap multivaluedMap = new MultivaluedMapImpl();
            multivaluedMap.add("queryString", queryString);
            multivaluedMap.add("userName", ObjectCrudSvcRemoteWrapper.userNametest);
            WebResource.Builder builder = restClientHelper.getWebResource("runNativeQueryForUser", multivaluedMap);
            ClientResponse response = builder.get(ClientResponse.class);
            return resolveResponse(response);
        }
        catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Failed to read object", e);
            throw new RuntimeException(e);
        }
    }

    public <T extends BaseObject> QueryResponseRemote runNamedQuery(String queryString, String clz) {
        try {
        //        return querySvcRemote.runNativeQuery(queryString, clz);
            MultivaluedMap multivaluedMap = new MultivaluedMapImpl();
            multivaluedMap.add("queryString", queryString);
            multivaluedMap.add("clz", clz);
            multivaluedMap.add("userName", ObjectCrudSvcRemoteWrapper.userNametest);
            WebResource.Builder builder = restClientHelper.getWebResource("runNamedQueryForUser", multivaluedMap);
            ClientResponse response = builder.get(ClientResponse.class);
            return resolveResponse(response);
        }
        catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Failed to read object", e);
            throw new RuntimeException(e);
        }
    }

    private QueryResponseRemote resolveResponse(ClientResponse response) throws IOException, ClassNotFoundException {
        ClientResponse.Status status = response.getClientResponseStatus();

        if (status != ClientResponse.Status.OK)
            throw new RuntimeException("Failed to run query: " + response.getClientResponseStatus().getReasonPhrase());

        if (!response.hasEntity())
            throw new RuntimeException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

        String jsonString = response.getEntity(String.class);
        LOGGER.debug("Query response: {}", jsonString);

        JSONObject jsonObject = new JSONObject(jsonString);
        List<? extends BaseObject> resultList = new ArrayList<>();
        if (jsonObject.has("resultList")) {
            JSONArray jsonArray = jsonObject.getJSONArray("resultList");
            for (int i = 0; i < jsonArray.length() ; i++) {
                resultList.add(restClientHelper.resolve((JSONObject) jsonArray.get(i)));
            }
        }
        QueryResponseRemote queryResponseRemote = new QueryResponseRemote();
        queryResponseRemote.setResultList(resultList);
        LOGGER.debug("Query response size: {}", queryResponseRemote.getResultList() == null ? 0 : queryResponseRemote.getResultList().size());
        return queryResponseRemote;
    }
}
