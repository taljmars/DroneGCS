package gui.is;

public interface KeyBoardControler {

	void HoldIfNeeded();

	void ReleaseIfNeeded();

	void Activate();

	void SetThrust(int eAvg);

	void Deactivate();

}
