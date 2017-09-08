package com.dronegcs.console_plugin.services;

import javafx.util.Pair;

public interface DialogManagerSvc {

	int YES_OPTION = 0;
	int NO_OPTION = 1;

	int showConfirmDialog(String text, String title);

	boolean showAlertMessageDialog(String msg);
	
	boolean showErrorMessageDialog(String msg, Throwable throwable);

	int showOptionsDialog(String text, String title, Object object2, String[] options, String initialValue);
	
	int showYesNoDialog(String text, String title, boolean defaultNo) ;

	String showInputDialog(String text, String title, Object object2, Object object3, String initialValue);
	
	Pair<Object, Object> showMultiComboBoxMessageDialog(String labelList1, Object[] list1, Object list1default, String labelList2, Object[] list2, Object list2default);
}
