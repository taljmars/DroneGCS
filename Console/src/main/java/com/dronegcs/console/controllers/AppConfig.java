package com.dronegcs.console.controllers;

import com.dronegcs.console.controllers.internalFrames.InternalFrameMap;
import com.dronegcs.console_plugin.ConsolePluginConfig;
import com.dronegcs.console_plugin.operations.OpGCSTerminationHandler;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;
import java.net.URISyntaxException;

@Import({GuiAppConfig.class, InternalFrameMap.class , OpGCSTerminationHandler.class,
		ConsolePluginConfig.class})
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

	private static final String STYLE_FILE = "/com/dronegcs/console/application.css";

	@Bean
	public Environment environment() {
		try {
			Environment env = new Environment();
			env.setBaseRunningDirectoryByClass(".");
			LOGGER.debug("Base running environment was set to {}", env.getRunningEnvBaseDirectory());
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
	public Validator validator() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	public RuntimeValidator runtimeValidator(@Autowired Validator validator) {
		RuntimeValidator rtv = new RuntimeValidator();
		rtv.setValidator(validator);
		return rtv;
	}

	@Bean(name = "GuiCSS")
	public String getGuiCSS() {
		return STYLE_FILE;
	}

}
