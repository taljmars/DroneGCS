package com.dronegcs.console.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Callback;
import com.dronegcs.mavlink.core.drone.MyDroneImpl;
import com.dronegcs.mavlink.is.drone.mission.Mission;

import com.dronegcs.console.operations.OpGCSTerminationHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import com.dronegcs.console.controllers.dashboard.Dashboard;
import com.dronegcs.console.controllers.internalFrames.InternalFrameMap;

@ComponentScan(basePackages = "com.dronegcs.console")
@Import({InternalFrameMap.class , MyDroneImpl.class , OpGCSTerminationHandler.class})
@Configuration
public class AppConfig {
	
	public static final AppConfig loader = new AppConfig();

	public static final double FRAME_CONTAINER_REDUCE_PRECENTAGE = 0.17;
	public static final String ENV_SYMBOL = "GCSMode";
	
	public static boolean DebugMode = false;
	public static final String DEBUG_SYMBOL = "debug";

	public static ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
	
	@Bean
	public Dashboard dashboard() {
		return new Dashboard();
	}
	
	@Bean @Scope("prototype")
	public Mission mission() {
		return new Mission();
	}
	
//	
//	@Bean
//	@Scope("prototype")
//	public LayerMission layerMission() {
//		return new LayerMission("New Mission*");
//	}
	
	private FXMLLoader getFXMLLoaderForUrl(String url) {



		FXMLLoader fxmlloader = new FXMLLoader();
		URL location = getClass().getResource(url);
		fxmlloader.setLocation(location);
		fxmlloader.setControllerFactory(new Callback<Class<?>, Object>() {
			@Override
			public Object call(Class<?> clazz) {
				System.out.print("Fetch bean name '" + clazz + "' ");
				Object obj = context.getBean(clazz);
				if (obj != null)
					System.out.println("[SUCCESS :'" + obj + "']");
				else
					System.err.println("[FAIL]");
				return obj;
			}
		});
		
		return fxmlloader;
	}
	
	public Object load(String url) {
		try {
			InputStream fxmlStream = AppConfig.class.getResourceAsStream(url);
			FXMLLoader fxmlLoader = getFXMLLoaderForUrl(url);
			return fxmlLoader.load(fxmlStream);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Node loadInternalFrame(String internalFrameUrl, double width, double height) {
		try {
			InputStream fxmlStream = AppConfig.class.getResourceAsStream(internalFrameUrl);
			FXMLLoader fxmlLoader = getFXMLLoaderForUrl(internalFrameUrl);
			fxmlLoader.getNamespace().put("prefWidth", width);
			fxmlLoader.getNamespace().put("prefHeight", height);
			return fxmlLoader.load(fxmlStream);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	// DB Access
	
	private static <T> T LoadServices(Class<T> clz) {
		try {
			System.err.println("Got " + clz.getSimpleName());
			URL url = new URL("http://localhost:9999/ws/droneServer?wsdl");
			QName qName = new QName("http://internal.ws.dronedb.com/", clz.getSimpleName() + "ImplService");
			Service service = Service.create(url, qName);
			return service.getPort(clz);
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	@Bean
//	public DroneDbCrudSvcRemote droneDbCrudSvcRemote() {
//		return LoadServices(DroneDbCrudSvcRemote.class);
//	}
}
