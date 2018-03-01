package com.drone_tester;

import com.dronegcs.console_plugin.ConsolePluginConfig;
import com.generic_tools.environment.Environment;
import com.generic_tools.logger.Logger;
import com.generic_tools.validations.RuntimeValidator;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;
import java.net.URISyntaxException;

@Import({/*AppConfig.class, GuiAppConfig.class, InternalFrameMap.class , OpGCSTerminationHandler.class,*/
        ConsolePluginConfig.class})
@ComponentScan(value = {
        "com.drone_tester",
        "com.generic_tools.logger",
        /*"com.dronegcs.console",*/
        "com.dronegcs.console_plugin",
//        "com.dronegcs.console.console_services"
})
@Configurable
public class TestSpringConfig {

    public static ApplicationContext context = new AnnotationConfigApplicationContext(TestSpringConfig.class);

    @Bean
    public Environment environment() {
        try {
            Environment env = new Environment();
            env.setBaseRunningDirectoryByClass("./tester/");
//            LOGGER.debug("Base running environment was set to {}", env.getRunningEnvBaseDirectory());
            return env;
        }
        catch (URISyntaxException e) {
            throw new BeanCreationException(e.getMessage());
        }
    }

    @Bean
    public Logger logger(@Autowired Environment environment) {
        Logger logger = new Logger(environment);
        return logger;
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
        return "dummy";
    }


}
