package validations;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gui.is.services.DialogManagerSvc;

@Component("validator")
public class RuntimeValidator {
	
	@Autowired
	private DialogManagerSvc dialogManagerSvc;
	
	public boolean validate(Object obj) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = (Validator) factory.getValidator();
		Set<ConstraintViolation<Object>> constraints = validator.validate(obj);
		if (constraints.isEmpty()) {
			return true;
		}
		
		String error_messege = "";
		for (ConstraintViolation<?> constraint : constraints) {
			error_messege += constraint.getMessage() + "\n";
		}
		dialogManagerSvc.showAlertMessageDialog("Runtime validation error:\n" + error_messege);
		
		return false;
	}
}
