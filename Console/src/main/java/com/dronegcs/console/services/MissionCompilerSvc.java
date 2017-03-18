package com.dronegcs.console.services;

import com.dronegcs.mavlink.is.drone.mission.Mission;

/**
 * Created by taljmars on 3/16/17.
 */
public interface MissionCompilerSvc
{
    Mission compile(Mission mission);
}
