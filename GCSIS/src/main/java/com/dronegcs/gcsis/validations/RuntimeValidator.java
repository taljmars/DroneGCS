package validations;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.springframework.stereotype.Component;
import static validations.ValidatorResponse.Status.FAILURE;
import static validations.ValidatorResponse.Status.SUCCESS;

@Component
public class RuntimeValidator {
	
	public ValidatorResponse validate(Object obj) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<Object>> constraints = validator.validate(obj);
		if (constraints.isEmpty()) {
			return new ValidatorResponse(SUCCESS);
		}
		
		String error_messege = "";
		for (ConstraintViolation<?> constraint : constraints) {
			error_messege += constraint.getMessage() + "\n";
		}
		
		return new ValidatorResponse(FAILURE, "Runtime validation error:\n" + error_messege);
	}
}
