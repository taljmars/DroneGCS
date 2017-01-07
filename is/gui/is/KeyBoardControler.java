package gui.is;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public interface KeyBoardControler extends EventHandler<KeyEvent> {

	void HoldIfNeeded();

	void ReleaseIfNeeded();

	void Activate();

	void SetThrust(int eAvg);

	void Deactivate();

}
