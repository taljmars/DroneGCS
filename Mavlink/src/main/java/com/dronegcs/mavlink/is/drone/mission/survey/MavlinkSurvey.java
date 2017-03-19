package com.dronegcs.mavlink.is.drone.mission.survey;

import java.util.ArrayList;
import java.util.List;

import com.dronegcs.mavlink.is.drone.mission.ConvertMavlinkVisitor;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.drone.mission.commands.MavlinkCameraTrigger;
import com.gui.is.shapes.Polygon;
import com.dronegcs.mavlink.is.drone.mission.DroneMissionItem;
import com.dronegcs.mavlink.is.drone.mission.MissionItemType;
import com.dronegcs.mavlink.is.drone.mission.survey.grid.Grid;
import com.dronegcs.mavlink.is.drone.mission.survey.grid.GridBuilder;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ardupilotmega.msg_mission_item;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_CMD;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_FRAME;
import com.geo_tools.Coordinate;

public class MavlinkSurvey extends DroneMissionItem {

	public Polygon polygon = new Polygon();
	public SurveyData surveyData = new SurveyData();
	public Grid grid;
	
	public MavlinkSurvey(MavlinkSurvey mavlinkSurvey) {
		super(mavlinkSurvey);
		this.polygon = new Polygon(mavlinkSurvey.polygon);
		this.surveyData = new SurveyData(mavlinkSurvey.surveyData);
		this.grid = new Grid(mavlinkSurvey.grid);
	}

	public MavlinkSurvey(DroneMission droneMission, List<Coordinate> points) {
		super(droneMission);
		polygon.addPoints(points);
	}

	public void update(double angle, double altitude, double overlap, double sidelap) {
		surveyData.update(angle, altitude, overlap, sidelap);
		droneMission.notifyMissionUpdate();
	}

	public void setCameraInfo(CameraInfo camera) {
		surveyData.setCameraInfo(camera);
		droneMission.notifyMissionUpdate();
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

			list.addAll((new MavlinkCameraTrigger(droneMission, surveyData.getLongitudinalPictureDistance())).packMissionItem());
			packGridPoints(list);
			list.addAll((new MavlinkCameraTrigger(droneMission, 0).packMissionItem()));
			
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
	public MavlinkSurvey clone(DroneMission droneMission) {
		MavlinkSurvey mavlinkSurvey = new MavlinkSurvey(this);
		mavlinkSurvey.setDroneMission(droneMission);
		return mavlinkSurvey;
	}

	@Override
	public void accept(ConvertMavlinkVisitor convertMavlinkVisitor) {
		convertMavlinkVisitor.visit(this);
	}
}
