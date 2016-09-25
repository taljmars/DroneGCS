package gui.is.interfaces;

public interface KeyBoardControler extends JMapViewerEventListener {

	void HoldIfNeeded();

	void ReleaseIfNeeded();

	void Activate();

	void SetThrust(int eAvg);

	void Deactivate();

}
