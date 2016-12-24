package mavlink.is.drone;

import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.Handler;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@ComponentScan("mavlink.core.drone")
@Component("events")
public class DroneEvents extends DroneVariable {

	private static final long serialVersionUID = 7458904266838861886L;

	private final ConcurrentLinkedQueue<DroneEventsType> eventsQueue = new ConcurrentLinkedQueue<DroneEventsType>();

	@Resource(name = "handler")
	private Handler handler;

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

	public DroneEvents() {
	}

	private final ConcurrentLinkedQueue<OnDroneListener> droneListeners = new ConcurrentLinkedQueue<OnDroneListener>();

	public void addDroneListener(OnDroneListener listener) {
		if (listener != null & !droneListeners.contains(listener))
			droneListeners.add(listener);
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
