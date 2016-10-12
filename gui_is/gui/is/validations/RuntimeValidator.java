package gui.is.validations;

import java.util.Set;

import javax.swing.JOptionPane;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class RuntimeValidator {
	
	public static boolean validate(Object obj) {
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
		JOptionPane.showMessageDialog(null, "Runtime validation error:\n" + error_messege);
		
		return false;
	}
}
