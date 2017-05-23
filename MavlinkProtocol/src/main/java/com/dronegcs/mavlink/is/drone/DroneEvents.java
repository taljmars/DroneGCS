package com.dronegcs.mavlink.is.drone;

import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.Handler;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnDroneListener;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class DroneEvents extends DroneVariable {
    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DroneEvents.class);

    private final ConcurrentLinkedQueue<DroneEventsType> eventsQueue = new ConcurrentLinkedQueue<DroneEventsType>();

    @Autowired
    @NotNull(message = "Internal Error: Failed to get event handler")
    private Handler handler;

    @Autowired
    private RuntimeValidator runtimeValidator;

    private final Runnable eventsDispatcher = new Runnable() {
        @Override
        public void run() {
            do {
                handler.removeCallbacks(this);
                final DroneEventsType event = eventsQueue.poll();
                if (event != null && !droneListeners.isEmpty()) {
                    for (OnDroneListener listener : droneListeners) {
                        listener.onDroneEvent(event, drone);
                    }
                }
            } while (!eventsQueue.isEmpty());
        }
    };

    private static int called;

    @PostConstruct
    private void init() {
        if (called++ > 1)
            throw new RuntimeException("Not a Singletone");

        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        LOGGER.info("Validation Succeeded for instance of {}", getClass());
    }

    private final ConcurrentLinkedQueue<OnDroneListener> droneListeners = new ConcurrentLinkedQueue<OnDroneListener>();

    public void addDroneListener(OnDroneListener listener) {
        if (listener != null && !droneListeners.contains(listener)) {
            droneListeners.add(listener);
        }
    }

    public void removeDroneListener(OnDroneListener listener) {
        if (listener != null && droneListeners.contains(listener))
            droneListeners.remove(listener);
    }

    public void notifyDroneEvent(DroneEventsType event) {
        eventsQueue.offer(event);
        handler.post(eventsDispatcher);
    }
}
