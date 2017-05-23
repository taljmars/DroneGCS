package com.generic_tools.tester;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by oem on 4/5/17.
 */
public class NameNotEmptyValidator implements ConstraintValidator<NameNotEmpty, ObjectWithName> {
    @Override
    public void initialize(NameNotEmpty constraintAnnotation) {

    }

    @Override
    public boolean isValid(ObjectWithName value, ConstraintValidatorContext context) {
        if (value.getName() == null || value.getName().isEmpty())
            return false;

        return true;
    }
}
