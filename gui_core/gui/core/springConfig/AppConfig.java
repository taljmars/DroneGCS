package gui.core.springConfig;

import java.awt.Dimension;
import gui.core.dashboard.Dashboard;
import gui.core.mapTreeObjects.LayerMission;
import javafx.scene.layout.Pane;
import mavlink.core.drone.MyDroneImpl;
import mavlink.is.drone.mission.Mission;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.AbstractApplicationContext;

@ComponentScan("gui.core.internalPanels")
@ComponentScan("gui.is.services")
@ComponentScan("mavlink.core.gcs")
@Import(MyDroneImpl.class)
@Configuration
public class AppConfig {
	
	public static final double FRAME_CONTAINER_REDUCE_PRECENTAGE = 0.17;
	public static final String DEBUG_SYMBOL = "debug";
	public static final String ENV_SYMBOL = "GCSMode";

	public static AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
	public static boolean DebugMode = false;
	
	@Bean
	public Pane frameContainer() {
		return new Pane();
	}
	
	@Bean
	public Dimension dimention() {
		 return new Dimension(1200, 150);
	}
	
	@Bean
	@Scope("prototype")
	public Pane emptyPanel() {
		return new Pane();
	}
	
	@Bean
	public Dashboard dashboard() {
		return new Dashboard();
	}
	
	@Bean
	@Scope("prototype")
	public Mission mission() {
		return new Mission();
	}
	
	@Bean
	@Scope("prototype")
	public LayerMission layerMission() {
		return new LayerMission("New Mission*");
	}
}
