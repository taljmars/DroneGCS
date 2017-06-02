package com.dronegcs.console_plugin.services.internal;

import com.dronedb.persistence.scheme.Mission;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.services.MissionCompilerSvc;
import com.dronegcs.console_plugin.services.internal.convertors.DatabaseToMavlinkItemConverter;
import com.dronegcs.console_plugin.services.internal.convertors.MavlinkItemToDatabaseConverter;
import com.dronegcs.console_plugin.services.internal.convertors.MissionCompilationException;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

/**
 * Created by taljmars on 3/18/17.
 */
@Component
public class MissionCompilerSvcImpl implements MissionCompilerSvc {
    private final static Logger LOGGER = LoggerFactory.getLogger(MissionCompilerSvcImpl.class);

    @Autowired
    @NotNull(message = "Internal Error: Failed to get application context")
    private ApplicationContext aplApplicationContext;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get drone")
    private Drone drone;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get mission manager")
    private MissionsManager missionsManager;

    @PostConstruct
    private void init() {
        LOGGER.info("Mission Compiler started");
    }

    @Override
    public DroneMission compile(Mission mission) {
        LOGGER.debug("Compiling Mission Named '{}' with {} mission items", mission.getName(), mission.getMissionItemsUids().size());
        DroneMission droneMission = new DroneMission();
        droneMission.setDrone(drone);
        DatabaseToMavlinkItemConverter databaseToMavlinkItemConverter = aplApplicationContext.getBean(DatabaseToMavlinkItemConverter.class);
        DroneMission res = databaseToMavlinkItemConverter.convert(mission, droneMission);
        LOGGER.debug("Compilation result is mavlink mission with {} mission items", res.getItems().size());
        return res;
    }

    @Override
    public Mission decompile(DroneMission droneMission) throws MissionCompilationException {
        LOGGER.debug("De-Compiling Mission with {} mission items", droneMission.getItems().size());
        Mission mission = new Mission();
        MavlinkItemToDatabaseConverter mavlinkItemToDatabaseConverter = aplApplicationContext.getBean(MavlinkItemToDatabaseConverter.class);
        Mission res = mavlinkItemToDatabaseConverter.convert(droneMission, mission);
        LOGGER.debug("De-Compilation result is mission named '{}' with {} mission items", res.getName(), res.getMissionItemsUids().size());
        return res;
    }
}
