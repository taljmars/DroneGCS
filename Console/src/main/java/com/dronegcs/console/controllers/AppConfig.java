package com.dronegcs.console.controllers;

import com.dronegcs.console.controllers.internalFrames.InternalFrameMap;
import com.dronegcs.console_plugin.operations.OpGCSTerminationHandler;
import com.dronegcs.mavlink.spring.MavlinkSpringConfig;
import com.generic_tools.environment.Environment;
import com.generic_tools.logger.Logger;
import com.generic_tools.validations.RuntimeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

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
}
