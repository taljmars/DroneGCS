package com.dronegcs.console_plugin.services;

import com.dronedb.persistence.scheme.Mission;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;

/**
 * Created by taljmars on 3/16/17.
 */
public interface MissionCompilerSvc
{
    DroneMission compile(Mission mission);

    Mission decompile(DroneMission mission);
}
