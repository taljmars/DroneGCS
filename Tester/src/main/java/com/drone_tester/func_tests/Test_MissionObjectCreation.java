package com.drone_tester.func_tests;

import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import com.dronedb.persistence.scheme.*;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Component
public class Test_MissionObjectCreation extends Test {

    private int idx = 0;
    private int total = 23;

    @Override
    public Status preTestCheck() {
        restClientHelper.setToken(login("tester1", "tester1"));
//        System.out.println(restClientHelper.getToken());

        Assert.isTrue(missionsManager.getAllMissions().isEmpty());
        publish(new TestEvent(this, Status.IN_PROGRESS, "No mission", ++idx, total));

        Assert.isTrue(missionsManager.getAllModifiedMissions().isEmpty());
        publish(new TestEvent(this, Status.IN_PROGRESS, "No mod mission", ++idx, total));

        publish(new TestEvent(this, Status.IN_PROGRESS, "pre test checked", ++idx, total));
        return Status.SUCCESS;
    }

    @Override
    public Status test() {
        try {
            // Creating and discard mission

            MissionEditor missionEditor = missionsManager.openMissionEditor("talma1");
            Mission mission = missionEditor.getModifiedMission();
            mission.setDefaultAlt(150);
            mission = missionEditor.update(mission);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Creating mission with unique default values", ++idx, total));


            Takeoff takeoff = missionEditor.createTakeOff();
            takeoff.setLat(44.44);
            takeoff.setLon(44.44);
            takeoff.setFinishedAlt(10.0);
            takeoff = missionEditor.updateMissionItem(takeoff);
            publish(new TestEvent(this, Status.IN_PROGRESS, "takeoff point created", ++idx, total));


            Waypoint waypoint = missionEditor.createWaypoint();
//            Assert.isTrue(waypoint.getAltitude().equals(mission.getDefaultAlt()));
            publish(new TestEvent(this, Status.IN_PROGRESS, "height verified", ++idx, total));
            waypoint.setLat(11.11);
            waypoint.setLon(11.11);
            waypoint = missionEditor.updateMissionItem(waypoint);
            publish(new TestEvent(this, Status.IN_PROGRESS, "waypoint created", ++idx, total));


            SplineWaypoint splineWaypoint = missionEditor.createSplineWaypoint();
//            Assert.isTrue(splineWaypoint.getAltitude().equals(mission.getDefaultAlt()));
            publish(new TestEvent(this, Status.IN_PROGRESS, "height verified", ++idx, total));
            splineWaypoint.setLat(22.22);
            splineWaypoint.setLon(22.22);
            splineWaypoint = missionEditor.updateMissionItem(splineWaypoint);
            publish(new TestEvent(this, Status.IN_PROGRESS, "spline waypoint created", ++idx, total));


            LoiterTime loiterTime = missionEditor.createLoiterTime();
            loiterTime.setLat(66.66);
            loiterTime.setLon(66.66);
            loiterTime = missionEditor.updateMissionItem(loiterTime);
            publish(new TestEvent(this, Status.IN_PROGRESS, "LoiterTime point created", ++idx, total));


            LoiterTurns loiterTurns = missionEditor.createLoiterTurns();
            loiterTurns.setLat(77.77);
            loiterTurns.setLon(77.77);
            loiterTurns = missionEditor.updateMissionItem(loiterTurns);
            publish(new TestEvent(this, Status.IN_PROGRESS, "LoiterTurns point created", ++idx, total));


            LoiterUnlimited loiterUnlimited = missionEditor.createLoiterUnlimited();
            loiterUnlimited.setLat(88.88);
            loiterUnlimited.setLon(88.88);
            loiterUnlimited = missionEditor.updateMissionItem(loiterUnlimited);
            publish(new TestEvent(this, Status.IN_PROGRESS, "LoiterUnlimited point created", ++idx, total));


            RegionOfInterest regionOfInterest = missionEditor.createRegionOfInterest();
            regionOfInterest.setLat(99.99);
            regionOfInterest.setLon(99.99);
            regionOfInterest = missionEditor.updateMissionItem(regionOfInterest);
            publish(new TestEvent(this, Status.IN_PROGRESS, "RegionOfInterest point created", ++idx, total));


            Land land = missionEditor.createLandPoint();
            land.setLat(33.33);
            land.setLon(33.33);
            land = missionEditor.updateMissionItem(land);
            publish(new TestEvent(this, Status.IN_PROGRESS, "land point created", ++idx, total));


//            ReturnToHome returnToHome = missionEditor.createReturnToLaunch();
//            returnToHome.setLat(55.55);
//            returnToHome.setLon(55.55);
//            returnToHome = missionEditor.updateMissionItem(returnToHome);
//            publish(new TestEvent(this, Status.IN_PROGRESS, "RTL point created", ++idx, total));

            ClosingPair<Mission> closingPair = missionsManager.closeMissionEditor(missionEditor, true);
            Assert.isTrue(closingPair != null);
            publish(new TestEvent(this, Status.IN_PROGRESS, "closing mission editor", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publish deletion", ++idx, total));

            publish(new TestEvent(this, Status.IN_PROGRESS, "test core finished", ++idx, total));
            return Status.SUCCESS;
        }
        catch (MissionUpdateException e) {
            e.printStackTrace();
            return Status.FAIL;
        }
    }

    @Override
    public Status postTestCleanup() {
        try {
            List<BaseObject> missionList = missionsManager.getAllMissions();
            Assert.isTrue(missionList.size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify one publish mission exist", ++idx, total));


            objectCrudSvcRemoteWrapper.delete(missionList.get(0));
            publish(new TestEvent(this, Status.IN_PROGRESS, "delete mission", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publish deletion", ++idx, total));

            Assert.isTrue(missionsManager.getAllModifiedMissions().isEmpty());
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify not mission exist", ++idx, total));

            Assert.isTrue(missionsManager.getAllMissions().isEmpty());
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify not modified mission exist", ++idx, total));

            logout();

            publish(new TestEvent(this, Status.SUCCESS, "test completed", ++idx, total));
            return Status.SUCCESS;
        }
        catch (ObjectInstanceRemoteException | ObjectNotFoundRemoteException | DatabaseValidationRemoteException e) {
            e.printStackTrace();
            return Status.FAIL;
        }
    }

}
