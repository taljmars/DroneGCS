package springConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Callback;
import mavlink.core.drone.MyDroneImpl;
import mavlink.drone.mission.Mission;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import controllers.dashboard.Dashboard;
import controllers.internalFrames.InternalFrameMap;
import core.operations.OpGCSTerminationHandler;

@ComponentScan("controllers.droneEye")
@ComponentScan("controllers.internalPanels")
@ComponentScan("controllers.internalFrames")
@ComponentScan("gui.core.operations")
@ComponentScan("gui.services")
@ComponentScan("mavlink.core.gcs")
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
	
	@Bean
	@Scope("prototype")
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
}
