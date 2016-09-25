package gui.core.springConfig;

import gui.is.interfaces.KeyBoardControler;
import mavlink.core.drone.ClockImpl;
import mavlink.core.drone.HandlerImpl;
import mavlink.core.drone.MyDroneImpl;
import mavlink.core.flightControlers.KeyBoardControlerImpl;
import mavlink.core.location.MyLocationImpl;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.Clock;
import mavlink.is.drone.DroneInterfaces.Handler;
import mavlink.is.location.LocationFinder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean
	public LocationFinder locationFinder() {
		return new MyLocationImpl();
	}
	
	@Bean
	public Clock clock() {
		return new ClockImpl();
	}
	
	@Bean
	public Handler handler() {
		return new HandlerImpl();
	}
	
	@Bean
	public Drone drone() {
		return new MyDroneImpl();
	}
	
	@Bean
	public KeyBoardControler keyBoardControler() {
		return new KeyBoardControlerImpl();
	}
}
