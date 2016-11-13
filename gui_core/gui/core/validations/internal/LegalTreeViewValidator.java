package gui.core.validations.internal;

import gui.core.mapTree.OperationalViewTree;
import gui.core.mapTreeObjects.Layer;
import gui.core.validations.LegalTreeView;
import javafx.scene.control.TreeItem;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LegalTreeViewValidator implements ConstraintValidator<LegalTreeView, OperationalViewTree> {
	
	@Override
	public void initialize(LegalTreeView arg0) {
		
	}

	@Override
	public boolean isValid(OperationalViewTree operationalViewTree, ConstraintValidatorContext arg1) {
		Set<String> lstLayerNames = new HashSet<>();	
		String res = verifyUniqueLayerNames(operationalViewTree.getRoot(), lstLayerNames);
		if (res != null) {
			//disable existing violation message
			arg1.disableDefaultConstraintViolation();
		    //build new violation message and add it
			arg1.buildConstraintViolationWithTemplate("Duplicated name found '" + res + "'").addConstraintViolation();
			return false;
		}
			
		return true;
	}
	
	public String verifyUniqueLayerNames(TreeItem<Layer> treeItem, Set<String> nameList) {
		String name = treeItem.getValue().getName();
		if (name.endsWith("*"))
			name = name.substring(0, name.length() - 1);
		
		if (nameList.contains(name))
			return name;

		nameList.add(name);
		
		System.err.println("TALMA " + name + " " + nameList);
		
		for (TreeItem<Layer> item : treeItem.getChildren()) {
			String res = verifyUniqueLayerNames(item, nameList);
			if (res != null)
				return res;
		}
		
		return null;
	}

}