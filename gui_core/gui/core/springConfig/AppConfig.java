package gui.core.springConfig;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import gui.core.dashboard.Dashboard;
import gui.core.internalFrames.AbstractJInternalFrame;
import gui.core.internalFrames.JInternalFrameActualPWM;
import gui.core.internalFrames.JInternalFrameMap;
import gui.core.internalPanels.JPanelConfigurationBox;
import gui.core.internalPanels.JPanelLogBox;
import gui.core.internalPanels.JPanelMissionBox;
import gui.core.mapObjects.LayerMission;
import gui.core.mapTree.JMapViewerTree;
import gui.core.mapViewer.JMapViewer;
import gui.is.interfaces.KeyBoardControler;
import gui.is.services.JMVEventPublisher;
import gui.is.services.LoggerDisplayerSvc;
import gui.is.services.TextNotificationPublisher;
import mavlink.core.connection.DroneUpdateListener;
import mavlink.core.drone.ClockImpl;
import mavlink.core.drone.HandlerImpl;
import mavlink.core.drone.MyDroneImpl;
import mavlink.core.flightControlers.KeyBoardControlerImpl;
import mavlink.core.location.MyLocationImpl;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.Clock;
import mavlink.is.drone.DroneInterfaces.Handler;
import mavlink.is.drone.mission.Mission;
import mavlink.is.location.LocationFinder;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.AbstractApplicationContext;

import communication_device.TwoWaySerialComm;

@ComponentScan("gui.core.internalPanels")
@ComponentScan("gui.is.services")
@ComponentScan("mavlink.core.gcs")
@Import(MyDroneImpl.class)
@Configuration
public class AppConfig {
	
	public static AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
	
	@Bean TwoWaySerialComm twoWaySerialComm() {
		return new TwoWaySerialComm();
	}
	
	@Bean
	public DroneUpdateListener droneUpdateListener() {
		return new DroneUpdateListener();
	}
	
	@Bean
	public LoggerDisplayerSvc loggerDisplayerSvc(){
		return new LoggerDisplayerSvc();
	}
	
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
	
	@Bean
	public JDesktopPane frameContainer() {
		return new JDesktopPane();
	}
	
	@Bean
	public JPanelLogBox areaLogBox() {
		return new JPanelLogBox(new GridBagLayout());
	}
	
	private Dimension dimention() {
		 return new Dimension(1200, 150);
	}

	@Bean
	public JPanelMissionBox areaMission() {
		return new JPanelMissionBox(null, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, dimention());	
	}
	
	@Bean
	public JPanelConfigurationBox areaConfiguration() {
		return new JPanelConfigurationBox(new JPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, dimention());
	}
	
	@Bean
	public Dashboard dashboard() {
		return new Dashboard();
	}
	
	@Bean
	public AbstractJInternalFrame internalFrameMap() {
		return new JInternalFrameMap();
	}
	
	@Bean
	public AbstractJInternalFrame internalFrameActualPWM() {
		return new JInternalFrameActualPWM();
	}
	
	@Bean
	public JMapViewer map() {
		return new JMapViewer();
	}
	
	@Bean
	public JMapViewerTree treeMap() {
		return new JMapViewerTree();
	}
	
	@Bean
	public JMVEventPublisher jMVEventPublisher() {
		return new JMVEventPublisher();
	}
	
	@Bean
	public TextNotificationPublisher textNotificationPublisher() {
		return new TextNotificationPublisher();
	}
	
	@Bean
	@Scope("prototype")
	public Mission mission() {
		return new Mission();
	}
	
	@Bean
	@Scope("prototype")
	public LayerMission layerMission() {
		return new LayerMission();
	}
}