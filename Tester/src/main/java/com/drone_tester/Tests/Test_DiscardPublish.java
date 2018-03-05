package com.drone_tester.Tests;

import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import com.dronedb.persistence.scheme.Mission;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Component
public class Test_DiscardPublish extends Test {

    private int idx = 0;
    private int total = 31;

    @Override
    public Status preTestCheck() {
        restClientHelper.setToken(login("tester1", "tester1"));
        System.out.println(restClientHelper.getToken());

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

            Mission mission = objectCrudSvcRemoteWrapper.create(Mission.class.getCanonicalName());
            publish(new TestEvent(this, Status.IN_PROGRESS, "Creating mission", ++idx, total));

            Assert.isTrue(missionsManager.getAllMissions().isEmpty());
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify no publish mission exist", ++idx, total));

            mission.setName("talma1");
            mission = objectCrudSvcRemoteWrapper.update(mission);
            publish(new TestEvent(this, Status.IN_PROGRESS, "updating mission", ++idx, total));

            Assert.isTrue(missionsManager.getAllModifiedMissions().size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify no modified mission exist", ++idx, total));

            Assert.isTrue(missionsManager.getAllMissions().size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify no publish mission exist", ++idx, total));

            sessionsSvcRemoteWrapper.discard();
            publish(new TestEvent(this, Status.IN_PROGRESS, "discard changes", ++idx, total));

            Assert.isTrue(missionsManager.getAllModifiedMissions().isEmpty());
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify no modified mission exist", ++idx, total));

            Assert.isTrue(missionsManager.getAllMissions().isEmpty());
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify no publish mission exist", ++idx, total));


            // creating + updating and publishing mission
            mission = objectCrudSvcRemoteWrapper.create(Mission.class.getCanonicalName());
            publish(new TestEvent(this, Status.IN_PROGRESS, "Creating mission", ++idx, total));

            mission.setName("talma2");
            mission = objectCrudSvcRemoteWrapper.update(mission);
            publish(new TestEvent(this, Status.IN_PROGRESS, "updating mission", ++idx, total));

            Assert.isTrue(missionsManager.getAllModifiedMissions().size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify one modified mission exist", ++idx, total));

            Assert.isTrue(missionsManager.getAllMissions().size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify no publish mission exist", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publishing changes", ++idx, total));

            Assert.isTrue(missionsManager.getAllModifiedMissions().isEmpty());
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify no modified mission exist", ++idx, total));

            Assert.isTrue(missionsManager.getAllMissions().size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify one publish mission exist", ++idx, total));


            // Delete and discard
            objectCrudSvcRemoteWrapper.delete(mission);
            publish(new TestEvent(this, Status.IN_PROGRESS, "delete mission", ++idx, total));

            Assert.isTrue(missionsManager.getAllModifiedMissions().isEmpty());
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify no modified mission exist", ++idx, total));

            Assert.isTrue(missionsManager.getAllMissions().isEmpty());
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify one publish mission exist", ++idx, total));

            sessionsSvcRemoteWrapper.discard();
            publish(new TestEvent(this, Status.IN_PROGRESS, "discard delete mission", ++idx, total));

            Assert.isTrue(missionsManager.getAllModifiedMissions().isEmpty());
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify no modified mission exist", ++idx, total));

            Assert.isTrue(missionsManager.getAllMissions().size() == 1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "verify one publish mission exist", ++idx, total));

            publish(new TestEvent(this, Status.IN_PROGRESS, "test core finished", ++idx, total));
            return Status.SUCCESS;
        }
        catch (ObjectInstanceRemoteException | ObjectNotFoundRemoteException | DatabaseValidationRemoteException e) {
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
