package com.dronegcs.console_plugin.remote_services_wrappers;

import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.generic_tools.Pair.Pair;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

@Component
public class ObjectCrudSvcRemoteWrapper {

    @Autowired
    private RestClientHelper restClientHelper;

    private final static Logger LOGGER = LoggerFactory.getLogger(ObjectCrudSvcRemoteWrapper.class);

    public <T extends BaseObject> T create(String clz) throws ObjectInstanceRemoteException {
        try {
//            MultivaluedMap formData = new MultivaluedMapImpl();
//            formData.add("clz", clz);
//            formData.add("token", restClientHelper.getToken());
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("create", "clz", clz);
            ClientResponse response = builder.get(ClientResponse.class);
            ClientResponse.Status status = response.getClientResponseStatus();
            if (!response.hasEntity())
                throw new ObjectInstanceRemoteException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

            switch (status) {
                case OK:
                    break;
                case UNAUTHORIZED:
                    throw new RuntimeException("Unauthorized access, " + status.getReasonPhrase());
                default:
                    Pair<Class, ? extends Exception> pair = restClientHelper.getErrorAndMessage(response);
                    Class cls = pair.getFirst();

                    if (cls.equals(ObjectInstanceRemoteException.class))
                        throw new ObjectInstanceRemoteException(pair.getSecond().getMessage());

                    throw new RuntimeException("Unexpected exception, code: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
            }

            T baseObject = response.getEntity((Class<T>) Class.forName(clz));
//            T baseObject = objectCrudSvcRemote.create(clz);
            return baseObject;
        }
        catch (Exception e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectInstanceRemoteException(e.getMessage());
        }
    }

    public <T extends BaseObject> T update(T obj) throws DatabaseValidationRemoteException, ObjectInstanceRemoteException {
        T baseObject = null;
        try {
//            WebResource.Builder builder = restClientHelper.getWebResource("update", "token", restClientHelper.getToken());
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("update");
            ObjectMapper mapper = new ObjectMapper();

            String objString = mapper.writeValueAsString(obj);
            ClientResponse response = builder.post(ClientResponse.class, objString);
            ClientResponse.Status status = response.getClientResponseStatus();
            if (!response.hasEntity())
                throw new DatabaseValidationRemoteException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

            if (status != ClientResponse.Status.OK) {
                Pair<Class, ? extends Exception> pair = restClientHelper.getErrorAndMessage(response);
                Class cls = pair.getFirst();

                if (cls.equals(DatabaseValidationRemoteException.class))
                    throw new DatabaseValidationRemoteException(pair.getSecond().getMessage());
                if (cls.equals(ObjectInstanceRemoteException.class))
                    throw new ObjectInstanceRemoteException(pair.getSecond().getMessage());

                throw new RuntimeException("Unexpected exception, code: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
            }

            baseObject = response.getEntity((Class<T>) obj.getClass());
//            baseObject = objectCrudSvcRemote.update(obj);
            return baseObject;
        }
        catch (Exception e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectInstanceRemoteException(e.getMessage());
        }
    }

    public <T extends BaseObject> T read(String objId) throws ObjectNotFoundRemoteException {
        try {
//            MultivaluedMap multivaluedMap = new MultivaluedMapImpl();
//            multivaluedMap.add("objId", objId.toString());
//            multivaluedMap.add("token", restClientHelper.getToken());
//            WebResource.Builder builder = restClientHelper.getWebResource("read",multivaluedMap);
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("read","objId", objId.toString());

            ClientResponse response = builder.get(ClientResponse.class);
            ClientResponse.Status status = response.getClientResponseStatus();
            if (!response.hasEntity())
                throw new ObjectNotFoundRemoteException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = response.getEntity(String.class);
            LOGGER.debug("Response: {}", jsonString);
//            System.out.println("Response: " + jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            LOGGER.debug("Response: {}", jsonObject);

            String actualClass = jsonObject.get("clz").toString();
            Class cls = Class.forName(actualClass);

            if (status != ClientResponse.Status.OK) {
                Pair<Class, ? extends Exception> pair = restClientHelper.getErrorAndMessageFromJson(jsonObject);
                cls = pair.getFirst();

                if (cls.equals(ObjectNotFoundRemoteException.class))
                    throw new ObjectNotFoundRemoteException(pair.getSecond().getMessage());

                throw new RuntimeException("Unexpected exception, code: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
            }

            T baseObject = objectMapper.readValue(jsonString, (Class<T>) cls);
//            T baseObject = objectCrudSvcRemote.read(objId);
            return baseObject;
        }
        catch (Exception e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectNotFoundRemoteException(e.getMessage());
        }
    }

    public <T extends BaseObject> T readByClass(String objId, String canonicalName) throws ObjectNotFoundRemoteException {
        try {
//            MultivaluedMap formData = new MultivaluedMapImpl();
//            formData.add("objId", objId.toString());
//            formData.add("clz", canonicalName);
//            formData.add("token", restClientHelper.getToken());
//            WebResource.Builder builder = restClientHelper.getWebResource("readByClass",formData);
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth(
                    "readByClass",
                    "objId", objId.toString(),
                    "clz", canonicalName);
//            System.err.println("readByClass string: " + builder.toString() + ", objId.toString():" + objId.toString());

            ClientResponse response = builder.get(ClientResponse.class);
            ClientResponse.Status status = response.getClientResponseStatus();
            if (!response.hasEntity())
                throw new ObjectNotFoundRemoteException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = response.getEntity(String.class);
            JSONObject jsonObject = new JSONObject(jsonString);
            LOGGER.debug("Response: {}", jsonObject);

            String actualClass = jsonObject.get("clz").toString();
            Class cls = Class.forName(actualClass);

            if (status != ClientResponse.Status.OK) {
                Pair<Class, ? extends Exception> pair = restClientHelper.getErrorAndMessageFromJson(jsonObject);
                cls = pair.getFirst();

                if (cls.equals(ObjectNotFoundRemoteException.class))
                    throw new ObjectNotFoundRemoteException(pair.getSecond().getMessage());

                throw new RuntimeException("Unexpected exception, code: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
            }

            T baseObject = objectMapper.readValue(jsonString, (Class<T>) cls);
//            T baseObject = objectCrudSvcRemote.readByClass(objId, (Class<T>) Class.forName(canonicalName));
            return baseObject;
        }
        catch (Exception e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectNotFoundRemoteException(e.getMessage());
        }
    }

    public <T extends BaseObject> T delete(T obj) throws ObjectInstanceRemoteException, DatabaseValidationRemoteException, ObjectNotFoundRemoteException {
        try {
//            WebResource.Builder builder = restClientHelper.getWebResource("delete", "token", restClientHelper.getToken());
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("delete");
            ObjectMapper objectMapper = new ObjectMapper();

            String objString = objectMapper.writeValueAsString(obj);
            ClientResponse response = builder.post(ClientResponse.class, objString);
            ClientResponse.Status status = response.getClientResponseStatus();
            if (!response.hasEntity())
                throw new ObjectNotFoundRemoteException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

            String jsonString = response.getEntity(String.class);
            JSONObject jsonObject = new JSONObject(jsonString);
            LOGGER.debug("Response: {}", jsonObject);
            String actualClass = jsonObject.get("clz").toString();
            Class cls = Class.forName(actualClass);

            if (status != ClientResponse.Status.OK) {
                Pair<Class, ? extends Exception> pair = restClientHelper.getErrorAndMessageFromJson(jsonObject);
                cls = pair.getFirst();

                if (cls.equals(ObjectNotFoundRemoteException.class))
                    throw new ObjectNotFoundRemoteException(pair.getSecond().getMessage());
                if (cls.equals(DatabaseValidationRemoteException.class))
                    throw new DatabaseValidationRemoteException(pair.getSecond().getMessage());
                if (cls.equals(ObjectInstanceRemoteException.class))
                    throw new ObjectInstanceRemoteException(pair.getSecond().getMessage());

                throw new RuntimeException("Unexpected exception, code: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
            }

//            ObjectMapper objectMapper = new ObjectMapper();

            T baseObject = objectMapper.readValue(jsonString, (Class<T>) cls);
//            T baseObject = objectCrudSvcRemote.delete(obj);
            return baseObject;
        }
        catch (ClassNotFoundException e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectNotFoundRemoteException(e.getMessage());
        }
        catch (Exception e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectInstanceRemoteException(e.getMessage());
        }
    }
}
