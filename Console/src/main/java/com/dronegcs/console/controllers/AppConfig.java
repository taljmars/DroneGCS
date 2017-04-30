package com.dronegcs.console.controllers;

import com.dronedb.persistence.ws.internal.*;
import com.dronegcs.console.controllers.internalFrames.InternalFrameMap;
import com.dronegcs.console_plugin.operations.OpGCSTerminationHandler;
import com.dronegcs.mavlink.spring.MavlinkSpringConfig;
import com.generic_tools.environment.Environment;
import com.generic_tools.logger.Logger;
import com.generic_tools.validations.RuntimeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

@Import({GuiAppConfig.class, InternalFrameMap.class , OpGCSTerminationHandler.class,
		MavlinkSpringConfig.class})
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

//	@Bean @Scope("prototype")
//	public DroneMission mission() {
//		return new DroneMission();
//	}

	@Bean
	public Environment environment() {
		return new Environment();
	}

	@Bean
	public Logger logger(@Autowired Environment environment) {
		return new Logger(environment);
	}

	@Bean
	public RuntimeValidator runtimeValidator() {
		return new RuntimeValidator();
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

	@Bean
	public SessionsSvcRemote sessionsSvcRemote() {
		return LoadServices(SessionsSvcRemote.class);
	}
}
