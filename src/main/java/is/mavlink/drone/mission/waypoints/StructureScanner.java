package is.mavlink.drone.mission.waypoints;

import java.util.ArrayList;
import java.util.List;

import gui.is.shapes.Polygon;
import is.mavlink.drone.mission.Mission;
import is.mavlink.drone.mission.MissionItemType;
import is.mavlink.drone.mission.survey.CameraInfo;
import is.mavlink.drone.mission.survey.Survey;
import is.mavlink.drone.mission.survey.SurveyData;
import is.mavlink.drone.mission.survey.grid.GridBuilder;
import is.mavlink.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import is.mavlink.protocol.msg_metadata.enums.MAV_CMD;
import tools.geoTools.Coordinate;
import tools.geoTools.GeoTools;

public class StructureScanner extends SpatialCoordItem {
	
	private double radius = 10.0;
	private double heightStep = 5;
	private int numberOfSteps = 2;
	private boolean crossHatch = false;
	SurveyData survey = new SurveyData();

	public StructureScanner(Mission mission, Coordinate coord) {
		super(mission,coord);
	}

	public StructureScanner(StructureScanner structureScanner) {
		super(structureScanner);
		this.radius = structureScanner.radius;
		this.heightStep = structureScanner.heightStep;
		this.numberOfSteps = structureScanner.numberOfSteps;
		this.crossHatch = structureScanner.crossHatch;
		this.survey = new SurveyData(survey);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = new ArrayList<msg_mission_item>();
		packROI(list);
		packCircles(list);
		if (crossHatch) {
			packHatch(list);			
		}
		return list;
	}

	private void packROI(List<msg_mission_item> list) {
		RegionOfInterest roi = new RegionOfInterest(mission, new Coordinate(coordinate, 0.0));
		list.addAll(roi.packMissionItem());
	}

	private void packCircles(List<msg_mission_item> list) {
		for (double altitude = coordinate.getAltitude(); altitude <= getTopHeight(); altitude += heightStep) {
			Circle circle = new Circle(mission, new Coordinate(coordinate,	altitude));
			circle.setRadius(radius);
			list.addAll(circle.packMissionItem());
		}
	}

	private void packHatch(List<msg_mission_item> list) {
		Polygon polygon = new Polygon();
		for (double angle = 0; angle <= 360; angle += 10) {
			polygon.addPoint(GeoTools.newCoordFromBearingAndDistance(coordinate, angle, radius));
		}

		Coordinate corner = GeoTools.newCoordFromBearingAndDistance(coordinate, -45, radius * 2);
		
		survey.setAltitude(getTopHeight());
		
		try {
			survey.update(0.0, survey.getAltitude(), survey.getOverlap(), survey.getSidelap());
			GridBuilder grid = new GridBuilder(polygon, survey, corner);
			for (Coordinate point : grid.generate(false).gridPoints) {
				list.add(Survey.packSurveyPoint(point, getTopHeight()));
			}
			
			survey.update(90.0, survey.getAltitude(), survey.getOverlap(), survey.getSidelap());
			GridBuilder grid2 = new GridBuilder(polygon, survey, corner);
			for (Coordinate point : grid2.generate(false).gridPoints) {
				list.add(Survey.packSurveyPoint(point, getTopHeight()));
			}
		} catch (Exception e) { // Should never fail, since it has good polygons
		}

	}

	public List<Coordinate> getPath() {
		List<Coordinate> path = new ArrayList<Coordinate>();
		for (msg_mission_item msg_mission_item : packMissionItem()) {
			if (msg_mission_item.command == MAV_CMD.MAV_CMD_NAV_WAYPOINT) {
				path.add(new Coordinate(msg_mission_item.x, msg_mission_item.y));
			}
			if (msg_mission_item.command == MAV_CMD.MAV_CMD_NAV_LOITER_TURNS) {
				for (double angle = 0; angle <= 360; angle += 12) {
					path.add(GeoTools.newCoordFromBearingAndDistance(coordinate,angle, radius));
				}
			}
			
		}
		return path;

	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CYLINDRICAL_SURVEY;
	}
	


	private double getTopHeight() {
		return coordinate.getAltitude() + (numberOfSteps - 1) * heightStep;
	}

	public double getEndAltitude() {
		return heightStep;
	}

	public int getNumberOfSteps() {
		return numberOfSteps;
	}

	public double getRadius() {
		return radius;
	}

	public Coordinate getCenter() {
		return coordinate;
	}

	public void setRadius(int newValue) {
		radius = newValue;
	}

	public void enableCrossHatch(boolean isEnabled) {
		crossHatch = isEnabled;
	}

	public boolean isCrossHatchEnabled() {
		return crossHatch;
	}

	public void setAltitudeStep(int newValue) {
		heightStep = newValue;		
	}

	public void setNumberOfSteps(int newValue) {
		numberOfSteps = newValue;	
	}

	public void setCamera(CameraInfo cameraInfo) {
		survey.setCameraInfo(cameraInfo);
	}

	public String getCamera() {
		return survey.getCameraName();
	}
	
	@Override
	public StructureScanner clone(Mission mission) {
		StructureScanner structureScanner = new StructureScanner(this);
		structureScanner.setMission(mission);
		return structureScanner;
	}

}
