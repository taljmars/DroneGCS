package com.tester;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application
{
	@Override
	public void start(Stage primaryStage) {
		try
		{
			BorderPane root = (BorderPane) FXMLLoader.load(getClass().getClassLoader().getResource("com/tester/View.fxml"));
			root.setStyle("-fx-background-color: whitesmoke;");
			Scene scene = new Scene(root, 800, 650);
			scene.getStylesheets().add(getClass().getClassLoader().getResource("com/tester/application.css").toExternalForm());
			primaryStage.setTitle("com/tester");
			primaryStage.setScene(scene);
			primaryStage.show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}