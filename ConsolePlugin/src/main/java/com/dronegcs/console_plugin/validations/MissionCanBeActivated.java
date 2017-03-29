package com.dronegcs.console_plugin.validations;

import com.dronegcs.console_plugin.validations.internal.MissionCanBeActivatedValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { MissionCanBeActivatedValidator.class })
public @interface MissionCanBeActivated {
	
	String message() default "DroneMission cannot be activated";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
    boolean value() default false;
}
