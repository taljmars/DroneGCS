package com.dronegcs.console_plugin.remote_services_wrappers;

import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronedb.persistence.scheme.Point;
import com.generic_tools.Pair.Pair;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PerimeterCrudSvcRemoteWrapper {

    private final static Logger LOGGER = LoggerFactory.getLogger(PerimeterCrudSvcRemoteWrapper.class);

    @Autowired
    private RestClientHelper restClientHelper;

    public <P extends Perimeter> P clonePerimeter(P perimeter) throws ObjectNotFoundRemoteException, ObjectInstanceRemoteException, DatabaseValidationRemoteException {
        try {
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("clonePerimeter");

            ObjectMapper mapper = new ObjectMapper();
            ClientResponse response = builder.post(ClientResponse.class, mapper.writeValueAsString(perimeter));
            ClientResponse.Status status = response.getClientResponseStatus();
            if (!response.hasEntity())
                throw new ObjectInstanceRemoteException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

            if (status != ClientResponse.Status.OK) {
                Pair<Class, ? extends Exception> pair = RestClientHelper.getErrorAndMessage(response);
                Class cls = pair.getFirst();

                if (cls.equals(ObjectInstanceRemoteException.class))
                    throw new ObjectInstanceRemoteException(pair.getSecond().getMessage());
                if (cls.equals(ObjectNotFoundRemoteException.class))
                    throw new ObjectNotFoundRemoteException(pair.getSecond().getMessage());
                if (cls.equals(DatabaseValidationRemoteException.class))
                    throw new DatabaseValidationRemoteException(pair.getSecond().getMessage());

                throw new RuntimeException("Unexpected exception, code: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
            }

            P perimeterResponse = response.getEntity((Class<P>) perimeter.getClass());
//          P perimeterResponse = perimeterCrudSvcRemote.clonePerimeter(perimeter);
            return perimeterResponse;
        }
        catch (Exception e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectInstanceRemoteException(e.getMessage());
        }
    }

    public Point createPoint() throws ObjectInstanceRemoteException {
        try {
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("createPoint");

            ObjectMapper mapper = new ObjectMapper();
            ClientResponse response = builder.post(ClientResponse.class);
            ClientResponse.Status status = response.getClientResponseStatus();
            if (!response.hasEntity())
                throw new ObjectInstanceRemoteException(status.getReasonPhrase() + ", status:" + status.getStatusCode());

            if (status != ClientResponse.Status.OK) {
                Pair<Class, ? extends Exception> pair = RestClientHelper.getErrorAndMessage(response);
                Class cls = pair.getFirst();

                if (cls.equals(ObjectInstanceRemoteException.class))
                    throw new ObjectInstanceRemoteException(pair.getSecond().getMessage());

                throw new RuntimeException("Unexpected exception, code: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
            }

            Point point = response.getEntity(Point.class);
    //            Point point = perimeterCrudSvcRemote.createPoint();
            return point;
        }
        catch (Exception e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectInstanceRemoteException(e.getMessage());
        }
    }

}
