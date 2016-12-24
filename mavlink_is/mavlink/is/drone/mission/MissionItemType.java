package mavlink.is.drone.mission;

public enum MissionItemType {
	WAYPOINT("Waypoint"), SPLINE_WAYPOINT("Spline Waypoint"), TAKEOFF("Takeoff"), RTL(
			"Return to Launch"), LAND("Land"), CIRCLE("Circle"), ROI("Region of Interest"), SURVEY(
			"Survey"), CYLINDRICAL_SURVEY("Structure Scan"), CHANGE_SPEED("Change Speed"), CAMERA_TRIGGER("Camera Trigger"), EPM_GRIPPER("EPM");

	private final String name;

	private MissionItemType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

//	public MissionItem getNewItem(MissionItem referenceItem) throws IllegalArgumentException {
//		switch (this) {
//		case WAYPOINT:
//			return new Waypoint(referenceItem);
//		case SPLINE_WAYPOINT:
//			return new SplineWaypoint(referenceItem);
//		case TAKEOFF:
//			return new Takeoff(referenceItem);
//		case CHANGE_SPEED:
//			return new ChangeSpeed(referenceItem);
//		case CAMERA_TRIGGER:
//			return new CameraTrigger(referenceItem);
//		case EPM_GRIPPER:
//			return new EpmGripper(referenceItem);
//		case RTL:
//			return new ReturnToHome(referenceItem);
//		case LAND:
//			return new Land(referenceItem);
//		case CIRCLE:
//			return new Circle(referenceItem);
//		case ROI:
//			return new RegionOfInterest(referenceItem);
//		case SURVEY:
//			return new Survey(referenceItem.getMission(), Collections.<Coord2D> emptyList());
//		case CYLINDRICAL_SURVEY:
//			return new StructureScanner(referenceItem);
//		default:
//			throw new IllegalArgumentException("Unrecognized mission item type (" + name + ")"
//					+ ".");
//		}
//	}
}
