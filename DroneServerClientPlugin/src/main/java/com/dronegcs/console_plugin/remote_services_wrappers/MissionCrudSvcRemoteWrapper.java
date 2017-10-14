package com.dronegcs.console_plugin.remote_services_wrappers;

import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.dronedb.persistence.scheme.Mission;
import com.dronedb.persistence.scheme.MissionItem;
import com.dronedb.persistence.ws.MissionCrudSvcRemote;
import com.dronegcs.console_plugin.remote_services_wrappers.internal.RestClientHelper;
import com.generic_tools.Pair.Pair;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MissionCrudSvcRemoteWrapper {

    private final static Logger LOGGER = LoggerFactory.getLogger(MissionCrudSvcRemoteWrapper.class);

//    @Autowired
    private MissionCrudSvcRemote missionCrudSvcRemote;

    @Autowired
    private RestClientHelper restClientHelper;


    public <T extends MissionItem> T createMissionItem(String clz) throws ObjectInstanceRemoteException {
        try {
            //        return missionCrudSvcRemote.createMissionItem(clz);
            WebResource.Builder builder = restClientHelper.getWebResource("createMissionItem", "clz", clz);

            ClientResponse response = builder.get(ClientResponse.class);
            ClientResponse.Status status = response.getClientResponseStatus();
            if (!response.hasEntity())
                throw new ObjectInstanceRemoteException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

            if (status != ClientResponse.Status.OK) {
                Pair<Class, ? extends Exception> pair = restClientHelper.getErrorAndMessage(response);
                Class cls = pair.getFirst();

                if (cls.equals(ObjectInstanceRemoteException.class))
                    throw new ObjectInstanceRemoteException(pair.getSecond().getMessage());

                throw new RuntimeException("Unexpected exception, code: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
            }

            T baseObject = response.getEntity((Class<T>) Class.forName(clz));
            return baseObject;
        }
        catch (ClassNotFoundException | IOException e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectInstanceRemoteException(e.getMessage());
        }
    }

    public Mission cloneMission(Mission mission) throws ObjectNotFoundRemoteException, ObjectInstanceRemoteException, DatabaseValidationRemoteException {
        try {
            //        return missionCrudSvcRemote.cloneMission(mission);
            WebResource.Builder builder = restClientHelper.getWebResource("cloneMission");

            ObjectMapper mapper = new ObjectMapper();
            ClientResponse response = builder.post(ClientResponse.class, mapper.writeValueAsString(mission));
            ClientResponse.Status status = response.getClientResponseStatus();
            if (!response.hasEntity())
                throw new ObjectInstanceRemoteException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

            if (status != ClientResponse.Status.OK) {
                Pair<Class, ? extends Exception> pair = restClientHelper.getErrorAndMessage(response);
                Class cls = pair.getFirst();

                if (cls.equals(ObjectInstanceRemoteException.class))
                    throw new ObjectInstanceRemoteException(pair.getSecond().getMessage());
                if (cls.equals(ObjectNotFoundRemoteException.class))
                    throw new ObjectNotFoundRemoteException(pair.getSecond().getMessage());
                if (cls.equals(DatabaseValidationRemoteException.class))
                    throw new DatabaseValidationRemoteException(pair.getSecond().getMessage());

                throw new RuntimeException("Unexpected exception, code: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
            }

            Mission missionResponse = response.getEntity(Mission.class);
            return missionResponse;
        }
        catch (ClassNotFoundException | IOException e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectInstanceRemoteException(e.getMessage());
        }
    }

    public Mission createMission() throws ObjectInstanceRemoteException {
        try {
            WebResource.Builder builder = restClientHelper.getWebResource("createMission");

            ClientResponse response = builder.get(ClientResponse.class);
            ClientResponse.Status status = response.getClientResponseStatus();
            if (!response.hasEntity())
                throw new ObjectInstanceRemoteException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

            if (status != ClientResponse.Status.OK) {
                Pair<Class, ? extends Exception> pair = restClientHelper.getErrorAndMessage(response);
                Class cls = pair.getFirst();

                if (cls.equals(ObjectInstanceRemoteException.class))
                    throw new ObjectInstanceRemoteException(pair.getSecond().getMessage());

                throw new RuntimeException("Unexpected exception, code: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
            }

            Mission mission = response.getEntity(Mission.class);
            return mission;
//        return missionCrudSvcRemote.createMission();
        }
        catch (ClassNotFoundException | IOException e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectInstanceRemoteException(e.getMessage());
        }

    }
}
