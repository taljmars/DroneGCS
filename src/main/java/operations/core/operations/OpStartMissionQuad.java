package operations.core.operations;

import is.gui.operations.OperationHandler;
import is.gui.services.LoggerDisplayerSvc;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import mavlink.core.validations.MissionCanBeActivated;
import mavlink.core.validations.QuadIsArmed;
import is.mavlink.drone.Drone;
import is.mavlink.drone.mission.Mission;
import is.mavlink.protocol.msg_metadata.ApmModes;
import is.validations.RuntimeValidator;

@ComponentScan("tools.validations")
@ComponentScan("gui.services")
@Component("opStartMissionQuad")
public class OpStartMissionQuad extends OperationHandler {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	@QuadIsArmed
	private Drone drone;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get mission")
	@MissionCanBeActivated
	private Mission mission;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get validator")
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
