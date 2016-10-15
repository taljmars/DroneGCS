package gui.core.Main;

import gui.core.dashboard.Dashboard;
import gui.core.springConfig.AppConfig;
import gui.is.validations.RuntimeValidator;

import java.awt.EventQueue;

import javax.swing.JOptionPane;

public class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					System.out.println("Start Dashboard");
					Dashboard dashboard = (Dashboard) AppConfig.context.getBean("dashboard");
					if (!RuntimeValidator.validate(dashboard)) {
						JOptionPane.showMessageDialog(null, "Critical error occur, failed to find running path");
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
