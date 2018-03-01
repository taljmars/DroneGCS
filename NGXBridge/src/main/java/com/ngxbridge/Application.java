package com.ngxbridge;

import com.dronegcs.mavlink.spring.MavlinkSpringConfig;
import com.generic_tools.environment.Environment;
import com.generic_tools.logger.Logger;
import com.generic_tools.validations.RuntimeValidator;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.net.URISyntaxException;

@Import({MavlinkSpringConfig.class})
@ComponentScans({
        @ComponentScan("com.dronegcs.console_plugin"),
        @ComponentScan("com.ngxbridge.remote_svc"),
        @ComponentScan("com.ngxbridge.svc")
})
@EnableAutoConfiguration
@SpringBootApplication
public class Application {

    private final static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(Application.class);

    @Bean
    public Environment environment1() {
        try {
            Environment env = new Environment();
            env.setBaseRunningDirectoryByClass(".");
            LOGGER.debug("Base running environment was set to " + env.getRunningEnvBaseDirectory());
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
    public Validator validator(final AutowireCapableBeanFactory autowireCapableBeanFactory) {
        ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
                .configure().constraintValidatorFactory(new SpringConstraintValidatorFactory(autowireCapableBeanFactory))
                .buildValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        return validator;
    }

    @Bean
    public RuntimeValidator runtimeValidator(@Autowired Validator validator) {
        RuntimeValidator rtv = new RuntimeValidator();
        rtv.setValidator(validator);
        return rtv;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}