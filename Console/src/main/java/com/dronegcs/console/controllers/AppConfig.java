package com.dronegcs.console.controllers;

import com.dronegcs.console.controllers.internalFrames.InternalFrameMap;
import com.dronegcs.console_plugin.operations.OpGCSTerminationHandler;
import com.dronegcs.mavlink.spring.MavlinkSpringConfig;
import com.generic_tools.environment.Environment;
import com.generic_tools.logger.Logger;
import com.generic_tools.validations.RuntimeValidator;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.URISyntaxException;

@Import({GuiAppConfig.class, InternalFrameMap.class , OpGCSTerminationHandler.class,
		MavlinkSpringConfig.class})
//@SpringBootApplication(scanBasePackages = "com.dronegcs.console")
@Configuration
@ComponentScan(value = {
        "com.dronegcs.console",
        "com.dronegcs.console_plugin"
})
public class AppConfig {

	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);
	public static final double FRAME_CONTAINER_REDUCE_PRECENTAGE = 0.17;
	public static final String ENV_SYMBOL = "GCSMode";

	@Bean
	public Environment environment() {
		try {
			Environment env = new Environment();
			env.setBaseRunningDirectoryByClass("Drone_GCS");
			LOGGER.debug("Base running environment was set to {}", env.getRunningEnvBaseDirectory());
            System.err.println("Base running environment was set to " +  env.getRunningEnvBaseDirectory());
			return env;
		}
		catch (URISyntaxException e) {
			throw new BeanCreationException(e.getMessage());
		}
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
