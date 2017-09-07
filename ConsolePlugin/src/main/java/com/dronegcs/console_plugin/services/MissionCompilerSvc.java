package com.dronegcs.console_plugin.services;

import com.dronedb.persistence.scheme.Mission;
import com.dronegcs.console_plugin.services.internal.convertors.MissionCompilationException;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;

/**
 * Created by taljmars on 3/16/17.
 */
public interface MissionCompilerSvc
{
    DroneMission compile(Mission mission) throws MissionCompilationException;

    Mission decompile(DroneMission mission) throws MissionCompilationException;
}
