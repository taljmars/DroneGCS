package com.dronegcs.console.controllers;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.dronedb.persistence.scheme.apis.DroneDbCrudSvcRemote;
import com.dronedb.persistence.scheme.apis.MissionCrudSvcRemote;
import com.dronedb.persistence.scheme.apis.PerimeterCrudSvcRemote;
import com.dronedb.persistence.scheme.apis.QuerySvcRemote;
import com.dronegcs.mavlink.core.drone.MyDroneImpl;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.console_plugin.operations.OpGCSTerminationHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import com.dronegcs.console.controllers.internalFrames.InternalFrameMap;

@Import({GuiAppConfig.class, InternalFrameMap.class , MyDroneImpl.class , OpGCSTerminationHandler.class})
//@SpringBootApplication(scanBasePackages = "com.dronegcs.console")
@Configuration
@ComponentScan(value = {
		"com.dronegcs.console",
		"com.dronegcs.console_plugin"
})
public class AppConfig {
	
	public static final AppConfig loader = new AppConfig();

	public static final double FRAME_CONTAINER_REDUCE_PRECENTAGE = 0.17;
	public static final String ENV_SYMBOL = "GCSMode";

	public static ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
	
	@Bean @Scope("prototype")
	public DroneMission mission() {
		return new DroneMission();
	}
	
	// DB Access
	
	private static <T> T LoadServices(Class<T> clz) {
		try {
			System.err.println("Got " + clz.getSimpleName());
			URL url = new URL("http://localhost:9999/ws/" + clz.getSimpleName() + "?wsdl");
			QName qName = new QName("http://internal.ws.persistence.dronedb.com/", clz.getSimpleName() + "ImplService");
			Service service = Service.create(url, qName);
			return service.getPort(clz);
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Bean
	public MissionCrudSvcRemote missionCrudSvcRemote() {
		return LoadServices(MissionCrudSvcRemote.class);
	}

	@Bean
	public QuerySvcRemote querySvcRemote() {
		return LoadServices(QuerySvcRemote.class);
	}

	@Bean
	public DroneDbCrudSvcRemote droneDbCrudSvcRemote() {
		return LoadServices(DroneDbCrudSvcRemote.class);
	}

	@Bean
	public PerimeterCrudSvcRemote perimeterCrudSvcRemote() {
		return LoadServices(PerimeterCrudSvcRemote.class);
	}
}
