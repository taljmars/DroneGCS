package com.dronegcs.console.validations.internal;

import gui.core.mapTreeObjects.Layer;
import javafx.scene.control.TreeItem;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.dronegcs.console.validations.LegalTreeView;

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
		if (name.endsWith(OperationalViewTree.EDIT_SUFFIX))
			name = name.substring(0, name.length() - OperationalViewTree.EDIT_SUFFIX.length());
		
		if (name.startsWith(OperationalViewTree.UPLOADED_PREFIX))
			name = name.substring(OperationalViewTree.UPLOADED_PREFIX.length(), name.length());
		
		if (nameList.contains(name))
			return name;

		nameList.add(name);
		
		for (TreeItem<Layer> item : treeItem.getChildren()) {
			String res = verifyUniqueLayerNames(item, nameList);
			if (res != null)
				return res;
		}
		
		return null;
	}

}