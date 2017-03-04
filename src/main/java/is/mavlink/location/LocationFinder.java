package mavlink.location;

public interface LocationFinder {
	
	public void enableLocationUpdates();

	public void disableLocationUpdates();

	public void addLocationListener(LocationReceiver receiver);
	
	public void removeLocationListener(LocationReceiver receiver);
}
