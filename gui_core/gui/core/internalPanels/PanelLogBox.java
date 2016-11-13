package gui.core.internalPanels;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import gui.is.events.logevents.LogAbstractDisplayerEvent;
import tools.logger.Logger;
import tools.logger.Logger.Type;

@Component("areaLogBox")
public class PanelLogBox extends Pane {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 7668860997050137469L;
	
	@Resource(name = "logger")
	@NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;
	
	private TextFlow logTextBox;

	public PanelLogBox() {
		setPrefHeight(Screen.getPrimary().getBounds().getHeight()*0.25);
		setPrefWidth(Screen.getPrimary().getBounds().getWidth());
		
		BorderPane pnlLogbox = new BorderPane();
		pnlLogbox.prefHeightProperty().bind(prefHeightProperty());
		pnlLogbox.prefWidthProperty().bind(prefWidthProperty());
		
		VBox pnlLogToolBox = new VBox();
        pnlLogbox.setRight(pnlLogToolBox);
        
        ToggleButton areaLogLockTop = new ToggleButton("Top");        
        //areaLogLockTop.setOnAction(e -> {if (areaLogLockTop.isSelected()) logbox.getVerticalScrollBar().setValue(0);});
        pnlLogToolBox.getChildren().add(areaLogLockTop);
        
        Button areaLogClear = new Button("CLR");
        areaLogClear.setOnAction(e -> logTextBox.getChildren().removeAll(logTextBox.getChildren()));
        pnlLogToolBox.getChildren().add(areaLogClear);
		
        logTextBox = new TextFlow();
        logTextBox.prefHeightProperty().bind(pnlLogbox.prefHeightProperty());
        logTextBox.setPrefWidth(pnlLogbox.getPrefWidth() * 0.95);
        //logTextBox.setPrefHeight(Screen.getPrimary().getBounds().getHeight()*0.5);
        //logTextBox.setPrefWidth(pnlLogbox.getWidth() - pnlLogToolBox.getWidth());
        ScrollPane logbox = new ScrollPane(logTextBox);
        logbox.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        
        pnlLogbox.setCenter(logbox);
        
        getChildren().add(pnlLogbox);
	}
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
	
	private void addGeneralMessegeToDisplay(String cmd) {
		System.out.println(cmd);
		addMessegeToDisplay(cmd, Type.GENERAL);
	}
	
	private void addErrorMessegeToDisplay(String cmd) {
		System.err.println(cmd);
		addMessegeToDisplay(cmd, Type.ERROR);
	}
	
	private void addOutgoingMessegeToDisplay(String cmd) {
		System.out.println(cmd);
		addMessegeToDisplay(cmd, Type.OUTGOING);
	}
	
	private void addIncommingMessegeToDisplay(String cmd) {
		System.out.println(cmd);
		addMessegeToDisplay(cmd, Type.INCOMING);
	}
	
	private void addMessegeToDisplay(String cmd, Type t) {
		addMessegeToDisplay(cmd, t, false);
	}
	
	private synchronized void addMessegeToDisplay(String cmd, Type t, boolean no_date) {
		logger.LogDesignedMessege(Logger.generateDesignedMessege(cmd, t, no_date));
		
		Text newcontent = generateDesignedMessege(cmd, t, no_date);
		logTextBox.getChildren().add(0, newcontent);
	}
	
	public static Text generateDesignedMessege(String cmd, Type t, boolean no_date)
	{
		
		String ts_string = "";
		if (!no_date)
			ts_string = "[" + LocalDateTime.now().toLocalTime() + "] ";

		/*
		 * Currently i am converting NL char to space and comma sep.
		 */
		cmd = cmd.replace("\n", ",");
		cmd = cmd.trim();
		
		Text newcontent = new Text(ts_string + cmd + "\n");

		switch (t) {
			case GENERAL:
				newcontent.setStyle("-fx-font-size: 14; -fx-fill: black;");
				break;
			case OUTGOING:
				newcontent.setStyle("-fx-font-size: 14; -fx-fill: blue;");
				break;
			case INCOMING:
				newcontent.setStyle("-fx-font-size: 14; -fx-fill: green;");
				break;
			case ERROR:
				newcontent.setStyle("-fx-font-size: 14; -fx-fill: red;");
				break;
			default:
				newcontent.setStyle("-fx-font-size: 14; -fx-fill: red;");
				break;
		}
		
		return newcontent;
	}
	
	@EventListener
	public void onLogDisplayerEvent(LogAbstractDisplayerEvent event) {
		Platform.runLater( () -> {
			switch (event.getType()) {
			case ERROR:
				addErrorMessegeToDisplay(event.getEntry());
				break;
			case GENERAL:
				addGeneralMessegeToDisplay(event.getEntry());
				break;
			case INCOMING:
				addIncommingMessegeToDisplay(event.getEntry());
				break;
			case OUTGOING:
				addOutgoingMessegeToDisplay(event.getEntry());
				break;
			}
		});
	}
}
