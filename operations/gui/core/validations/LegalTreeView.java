package gui.core.validations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import gui.core.validations.internal.LegalTreeViewValidator;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { LegalTreeViewValidator.class })
public @interface LegalTreeView {
	
	String message() default "Tree View is not legal";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
    boolean value() default false;
}
