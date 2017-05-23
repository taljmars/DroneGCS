package com.generic_tools.validations;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class RuntimeValidator {

    public ValidatorResponse validate(Object obj) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> constraints = validator.validate(obj);
        if (constraints.isEmpty()) {
            return new ValidatorResponse(ValidatorResponse.Status.SUCCESS, "Validated successfully");
        }

        StringBuilder error_messege = new StringBuilder();
        for (ConstraintViolation<?> constraint : constraints) {
            error_messege.append(constraint.getMessage()).append("\n");
        }

        return new ValidatorResponse(ValidatorResponse.Status.FAILURE, error_messege.toString());
    }
}
