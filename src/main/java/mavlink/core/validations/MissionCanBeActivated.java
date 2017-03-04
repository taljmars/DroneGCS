package main.java.mavlink_core.mavlink.core.validations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import main.java.mavlink_core.mavlink.core.validations.internal.MissionCanBeActivatedValidator;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { MissionCanBeActivatedValidator.class })
public @interface MissionCanBeActivated {
	
	String message() default "Mission cannot be activated";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
    boolean value() default false;
}
