package com.dronegcs.console_plugin.services.internal;

import com.dronedb.persistence.scheme.Mission;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
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
    private ApplicationContext applicationContext;

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
    public DroneMission compile(Mission mission) throws MissionCompilationException {
        LOGGER.debug("Compiling Mission Named '{}' with {} mission items", mission.getName(), mission.getMissionItemsUids().size());
        DroneMission droneMission = new DroneMission();
        droneMission.setDrone(drone);
        DatabaseToMavlinkItemConverter databaseToMavlinkItemConverter = applicationContext.getBean(DatabaseToMavlinkItemConverter.class);
        DroneMission res = databaseToMavlinkItemConverter.convert(mission, droneMission);
        LOGGER.debug("Compilation result is mavlink mission with {} mission items", res.getItems().size());

        if (droneMission.getItems().size() != mission.getMissionItemsUids().size()) {
            throw new MissionCompilationException(String.format("Converted mission item's (%s) are not equal to the origin (%s)",
                    mission.getMissionItemsUids().size(), droneMission.getItems().size()));
        }

        return res;
    }

    @Override
    public Mission decompile(DroneMission droneMission) throws MissionCompilationException {
        LOGGER.debug("De-Compiling Mission with {} mission items", droneMission.getItems().size());
        MissionEditor missionEditor = missionsManager.openMissionEditor("OnBoardMission_" + System.currentTimeMillis());
        MavlinkItemToDatabaseConverter mavlinkItemToDatabaseConverter = applicationContext.getBean(MavlinkItemToDatabaseConverter.class);
        missionEditor = mavlinkItemToDatabaseConverter.convert(droneMission, missionEditor);
        Mission res = missionEditor.getMission();
        LOGGER.debug("De-Compilation result is mission named '{}' with {} mission items", res.getName(), res.getMissionItemsUids().size());

        if (droneMission.getItems().size() != res.getMissionItemsUids().size()) {
            LOGGER.error("De-Compilation failed due to gaps between items amount, closing editor");
//                ClosingPair<Mission> a = missionsManager.closeMissionEditor(missionEditor, false);
            throw new MissionCompilationException(String.format("Converted mission item's (%s) are not equal to the origin (%s)",
                    droneMission.getItems().size(), res.getMissionItemsUids().size()));
        }
        return res;
    }
}
