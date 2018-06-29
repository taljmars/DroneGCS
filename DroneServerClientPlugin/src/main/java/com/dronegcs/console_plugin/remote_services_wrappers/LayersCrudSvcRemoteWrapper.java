package com.dronegcs.console_plugin.remote_services_wrappers;

import com.db.gui.persistence.scheme.BaseLayer;
import com.db.gui.persistence.scheme.Layer;
import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.generic_tools.Pair.Pair;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LayersCrudSvcRemoteWrapper {

    private final static Logger LOGGER = LoggerFactory.getLogger(LayersCrudSvcRemoteWrapper.class);

    @Autowired
    private RestClientHelper restClientHelper;

    public Layer cloneLayer(BaseLayer layer) throws ObjectNotFoundRemoteException, ObjectInstanceRemoteException, DatabaseValidationRemoteException {
        try {
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("cloneLayer");

            ObjectMapper mapper = new ObjectMapper();
            ClientResponse response = builder.post(ClientResponse.class, mapper.writeValueAsString(layer));
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

            Layer layerResponse = response.getEntity(Layer.class);
            return layerResponse;
        }
        catch (Exception e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectInstanceRemoteException(e.getMessage());
        }
    }

    public Layer createLayer() throws ObjectInstanceRemoteException {
        try {
            WebResource.Builder builder = restClientHelper.getWebResourceWithAuth("createLayer");

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

            Layer layer = response.getEntity(Layer.class);
            return layer;
        }
        catch (Exception e) {
            LOGGER.error("Failed to read object", e);
            throw new ObjectInstanceRemoteException(e.getMessage());
        }

    }
}
