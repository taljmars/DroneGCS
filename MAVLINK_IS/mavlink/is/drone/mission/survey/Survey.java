package mavlink.is.drone.mission.survey;

import java.util.ArrayList;
import java.util.List;

import mavlink.is.drone.mission.Mission;
import mavlink.is.drone.mission.MissionItem;
import mavlink.is.drone.mission.MissionItemType;
import mavlink.is.drone.mission.commands.CameraTrigger;
import mavlink.is.drone.mission.survey.grid.Grid;
import mavlink.is.drone.mission.survey.grid.GridBuilder;
import mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import mavlink.is.protocol.msg_metadata.enums.MAV_FRAME;
import mavlink.is.shapes.polygon.Polygon;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.units.Altitude;
import mavlink.is.utils.units.Length;

public class Survey extends MissionItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1014690250849152028L;
	public Polygon polygon = new Polygon();
	public SurveyData surveyData = new SurveyData();
	public Grid grid;

	public Survey(Mission mission, List<Coord2D> points) {
		super(mission);
		polygon.addPoints(points);
	}

	public void update(double angle, Altitude altitude, double overlap, double sidelap) {
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
		GridBuilder gridBuilder = new GridBuilder(polygon, surveyData, new Coord2D(0, 0));
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
			list.addAll((new CameraTrigger(mission, new Length(0.0)).packMissionItem()));
			
			return list;
		} catch (Exception e) {
			return new ArrayList<msg_mission_item>();
		}
	}

	private void packGridPoints(List<msg_mission_item> list) {
		for (Coord2D point : grid.gridPoints) {
			msg_mission_item mavMsg = packSurveyPoint(point,surveyData.getAltitude());
			list.add(mavMsg);
		}
	}

	public static msg_mission_item packSurveyPoint(Coord2D point, Length altitude) {
		msg_mission_item mavMsg = new msg_mission_item();
		mavMsg.autocontinue = 1;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		mavMsg.x = (float) point.getX();
		mavMsg.y = (float) point.getY();
		mavMsg.z = (float) altitude.valueInMeters();
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

}
