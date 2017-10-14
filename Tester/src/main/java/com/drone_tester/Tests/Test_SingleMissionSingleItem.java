package com.drone_tester.Tests;

import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import com.dronedb.persistence.scheme.Mission;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
import com.geo_tools.Coordinate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;

@Component
public class Test_SingleMissionSingleItem extends Test {

    private int idx = 0;
    private int total = 20;

    @Override
    public Test.Status preTestCheck() {
        Assert.isTrue(missionsManager.getAllMissions().isEmpty());
        publish(new TestEvent(this, Status.IN_PROGRESS, "No mission", ++idx, total));

        Assert.isTrue(missionsManager.getAllModifiedMissions().isEmpty());
        publish(new TestEvent(this, Status.IN_PROGRESS, "No mod mission", ++idx, total));


        publish(new TestEvent(this, Status.IN_PROGRESS, "pre test checked", ++idx, total));
        return Status.SUCCESS;
    }

    @Override
    public Test.Status test() {
        try {
            MissionEditor missionEditor = missionsManager.openMissionEditor("talma");
            publish(new TestEvent(this, Status.IN_PROGRESS, "Creating mission", ++idx, total));
            Mission mission = missionEditor.getModifiedMission();
            mission.setDefaultAlt(100);
            missionEditor.update(mission);
            publish(new TestEvent(this, Status.IN_PROGRESS, "update default height", ++idx, total));

            Assert.isTrue(missionEditor.getModifiedMission().getDefaultAlt() == 100);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify default height", ++idx, total));

            missionEditor.addWaypoint(new Coordinate(11.11, 22.22));
            publish(new TestEvent(this, Status.IN_PROGRESS, "update Waypoint", ++idx, total));

            mission = missionEditor.getModifiedMission();
            Assert.isTrue(mission.getMissionItemsUids().size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify waypoint", ++idx, total));

            Assert.isTrue(missionEditor.getMissionItems().get(0).getLat().equals(11.11));
            Assert.isTrue(missionEditor.getMissionItems().get(0).getLon().equals(22.22));
            publish(new TestEvent(this, Status.IN_PROGRESS, "Coordinate verified", ++idx, total));

            Assert.isTrue(missionsManager.getAllModifiedMissions().size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify one modified mission exist", ++idx, total));

            Assert.isTrue(missionsManager.getAllMissions().size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify no publish mission exist", ++idx, total));


            /// Saving

            ClosingPair<Mission> a = missionsManager.closeMissionEditor(missionEditor, true);
            publish(new TestEvent(this, Status.IN_PROGRESS, "mission editor closed", ++idx, total));

            Assert.isTrue(missionsManager.getAllModifiedMissions().size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify no modified mission exist", ++idx, total));

            Assert.isTrue(missionsManager.getAllMissions().size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify one publish mission exist", ++idx, total));

            publish(new TestEvent(this, Status.IN_PROGRESS, "test core finished", ++idx, total));
            return Status.SUCCESS;
        }
        catch (MissionUpdateException e) {
            e.printStackTrace();
            return Status.FAIL;
        }
    }

    @Override
    public Test.Status postTestCleanup() {
        Collection<ClosingPair<Mission>> a = missionsManager.closeAllMissionEditors(false);
        Assert.isTrue(a.isEmpty());
        publish(new TestEvent(this, Status.IN_PROGRESS, "verify no mission editor are opened", ++idx, total));

        sessionsSvcRemoteWrapper.discard();
        publish(new TestEvent(this, Status.IN_PROGRESS, "discarding changes", ++idx, total));

        Assert.isTrue(missionsManager.getAllModifiedMissions().isEmpty());
        publish(new TestEvent(this, Status.IN_PROGRESS, "verify not mission exist", ++idx, total));

        Assert.isTrue(missionsManager.getAllMissions().isEmpty());
        publish(new TestEvent(this, Status.IN_PROGRESS, "verify not modified mission exist", ++idx, total));

        publish(new TestEvent(this, Status.SUCCESS, "test completed", ++idx, total));
        return Status.SUCCESS;
    }

}
