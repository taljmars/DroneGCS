package is.mavlink.drone;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import is.mavlink.drone.DroneInterfaces.DroneEventsType;
import is.mavlink.drone.DroneInterfaces.Handler;
import is.mavlink.drone.DroneInterfaces.OnDroneListener;
import is.validations.RuntimeValidator;

@ComponentScan("mavlink.core.drone")
@ComponentScan("is.validations")
@Component
public class DroneEvents extends DroneVariable {

	private final ConcurrentLinkedQueue<DroneEventsType> eventsQueue = new ConcurrentLinkedQueue<DroneEventsType>();

	@Autowired @NotNull( message = "Internal Error: Failed to get event handler" )
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
            }while(!eventsQueue.isEmpty());
		}
	};

	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		if (!runtimeValidator.validate(this))
			throw new RuntimeException("Validation failed");
		else
			System.err.println("Validation Succeeded for instance of " + getClass()); 
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
