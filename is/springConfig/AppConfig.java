package springConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javafx.fxml.FXMLLoader;
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
import gui.core.operations.OpGCSTerminationHandler;

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
	
	public Object load(String url) {
		try (InputStream fxmlStream = AppConfig.class.getResourceAsStream(url)) 
		{
			FXMLLoader loader = new FXMLLoader();
			URL location = getClass().getResource(url);
            loader.setLocation(location);
			loader.setControllerFactory(new Callback<Class<?>, Object>() {
				@Override
				public Object call(Class<?> clazz) {
					System.err.println("Try to get bean name " + clazz );
					return context.getBean(clazz);
				}
			});
			
			return loader.load(fxmlStream);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}
}
