package mavlink.is.drone.mission.survey;

import java.util.ArrayList;
import java.util.List;

import gui.is.Coordinate;
import gui.is.shapes.Polygon;
import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.MissionItem;
import mavlink.is.drone.mission.MissionItemType;
import mavlink.is.drone.mission.commands.CameraTrigger;
import mavlink.is.drone.mission.survey.grid.Grid;
import mavlink.is.drone.mission.survey.grid.GridBuilder;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.protocol.msg_metadata.enums.MAV_FRAME;

public class Survey extends MissionItem {

	public Polygon polygon = new Polygon();
	public SurveyData surveyData = new SurveyData();
	public Grid grid;
	
	public Survey(Survey survey) {
		super(survey);
		this.polygon = new Polygon(survey.polygon);
		this.surveyData = new SurveyData(survey.surveyData);
		this.grid = new Grid(survey.grid);
	}

	public Survey(Mission mission, List<Coordinate> points) {
		super(mission);
		polygon.addPoints(points);
	}

	public void update(double angle, double altitude, double overlap, double sidelap) {
		surveyData.update(angle, altitude, overlap, sidelap);
		mission.notifyMissionUpdate();
	}

	public void setCameraInfo(CameraInfo camera) {
		surveyData.setCameraInfo(camera);
		mission.notifyMissionUpdate();
	}

	public void build() throws Exception {
		// TODO find better point than (0,0) to reference the grid
		grid = null;
		GridBuilder gridBuilder = new GridBuilder(polygon, surveyData, new Coordinate(0, 0));
		polygon.checkIfValid();
		grid = gridBuilder.generate(true);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		try {
			List<msg_mission_item> list = new ArrayList<msg_mission_item>();
			build();

			list.addAll((new CameraTrigger(mission, surveyData.getLongitudinalPictureDistance())).packMissionItem());
			packGridPoints(list);
			list.addAll((new CameraTrigger(mission, 0).packMissionItem()));
			
			return list;
		} catch (Exception e) {
			return new ArrayList<msg_mission_item>();
		}
	}

	private void packGridPoints(List<msg_mission_item> list) {
		for (Coordinate point : grid.gridPoints) {
			msg_mission_item mavMsg = packSurveyPoint(point,surveyData.getAltitude());
			list.add(mavMsg);
		}
	}

	public static msg_mission_item packSurveyPoint(Coordinate point, double altitude) {
		msg_mission_item mavMsg = new msg_mission_item();
		mavMsg.autocontinue = 1;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		mavMsg.x = (float) point.getX();
		mavMsg.y = (float) point.getY();
		mavMsg.z = (float) altitude;
		mavMsg.param1 = 0f;
		mavMsg.param2 = 0f;
		mavMsg.param3 = 0f;
		mavMsg.param4 = 0f;
		return mavMsg;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		// TODO Auto-generated method stub

	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.SURVEY;
	}

	@Override
	public Survey clone(Mission mission) {
		Survey survey = new Survey(this);
		survey.setMission(mission);
		return survey;
	}

}
