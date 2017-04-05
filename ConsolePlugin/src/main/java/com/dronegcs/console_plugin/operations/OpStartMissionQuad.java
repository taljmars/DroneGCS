package com.dronegcs.console_plugin.operations;

import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.dronegcs.console_plugin.validations.QuadIsArmed;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmModes;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;

@Component
public class OpStartMissionQuad extends OperationHandler {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	@QuadIsArmed
	private Drone drone;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get droneMission")
    private DroneMission droneMission;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get validator")
	private RuntimeValidator runtimeValidator;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
	
	@Override
	public boolean go() throws InterruptedException {
		loggerDisplayerSvc.logGeneral("Start DroneMission");

		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			return false;
		
		drone.getState().changeFlightMode(ApmModes.ROTOR_AUTO);
		
		return super.go();
	}

	public void setDroneMission(DroneMission droneMission) {
		this.droneMission = droneMission;
	}
}
