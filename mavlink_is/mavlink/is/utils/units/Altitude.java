package mavlink.is.utils.units;

import java.io.Serializable;

public class Altitude extends Length implements Serializable /* TALMA serializble*/  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1253757177177174809L;

	public Altitude(double heightInMeters) {
		super(heightInMeters);
	}

	public Altitude(Altitude altitude) {
		super(altitude);
	}

	public Length subtract(Altitude toSubtract) {
		return new Length(this.valueInMeters() - toSubtract.valueInMeters());
	}
}
