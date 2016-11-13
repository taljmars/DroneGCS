package gui.core.operations;

import gui.is.operations.OperationHandler;
import gui.is.services.LoggerDisplayerSvc;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import mavlink.core.validations.MissionCanBeActivated;
import mavlink.core.validations.QuadIsArmed;
import mavlink.is.drone.Drone;
import mavlink.is.drone.mission.Mission;
import mavlink.is.protocol.msg_metadata.ApmModes;
import tools.validations.RuntimeValidator;

@ComponentScan("tools.validations")
@Component("opStartMissionQuad")
public class OpStartMissionQuad extends OperationHandler {
	
	@Resource(name = "loggerDisplayerSvc")
	@NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "drone")
	@NotNull(message = "Internal Error: Failed to get drone")
	@QuadIsArmed
	private Drone drone;
	
	@NotNull(message = "Internal Error: Failed to get mission")
	@MissionCanBeActivated
	private Mission mission;
	
	@Resource(name = "validator")
	@NotNull(message = "Internal Error: Failed to get validator")
	private RuntimeValidator validator;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
	
	@Override
	public boolean go() throws InterruptedException {
		loggerDisplayerSvc.logGeneral("Start Mission");
	
		if (!validator.validate(this))
			return false;
		
		drone.getState().changeFlightMode(ApmModes.ROTOR_AUTO);
		
		return super.go();
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}
}
