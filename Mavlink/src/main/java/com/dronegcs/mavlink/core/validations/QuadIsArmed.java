package com.dronegcs.mavlink.core.mavlink.validations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.dronegcs.mavlink.core.mavlink.validations.internal.QuadIsArmedValidator;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { QuadIsArmedValidator.class })
public @interface QuadIsArmed {
	
	String message() default "Quad is not armed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
    boolean value() default false;
}
