package com.dronegcs.console_plugin.validations;

import com.dronegcs.console_plugin.validations.internal.SwitchToRCValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { SwitchToRCValidator.class })
public @interface SwitchToRC {
	
	String message() default "Verify RC is activated and have some thrust";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
    boolean value() default false;
}
