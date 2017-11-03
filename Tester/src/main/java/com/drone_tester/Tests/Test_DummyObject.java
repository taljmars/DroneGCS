package com.drone_tester.Tests;

import com.db.persistence.scheme.BaseObject;
import com.db.persistence.scheme.DummyBaseObject;
import com.db.persistence.wsSoap.QueryResponseRemote;
import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import com.dronedb.persistence.scheme.Mission;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
import com.geo_tools.Coordinate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;

@Component
public class Test_DummyObject extends Test {

    private int idx = 0;
    private int total = 0;

    @Override
    public Status preTestCheck() {
        return Status.SUCCESS;
    }

    @Override
    public Status test() {
        try {
//            objectCrudSvcRemoteWrapper.userNametest = "aaa";
//            sessionsSvcRemoteWrapper.discard();
            Mission mission = objectCrudSvcRemoteWrapper.create(Mission.class.getCanonicalName());
            mission.setName("tal1.1");
            mission = objectCrudSvcRemoteWrapper.update(mission);
            mission.setName("tal1.2.2");
            mission = objectCrudSvcRemoteWrapper.update(mission);
            QueryResponseRemote queryResponseRemote = querySvcRemoteWrapper.runNativeQuery("select m from Mission m");
            List<? extends BaseObject> list = queryResponseRemote.getResultList();
            for (BaseObject baseObject : list) {
                if (baseObject instanceof Mission) {
                    System.out.println("Object: " + baseObject.getKeyId() + " " + ((Mission) baseObject).getName());
                }
            }
//            sessionsSvcRemoteWrapper.publish();
//            System.out.println("");
//            objectCrudSvcRemoteWrapper.userNametest = "bbb";
//            mission = objectCrudSvcRemoteWrapper.create(Mission.class.getCanonicalName());
//            mission.setName("tal2");
//            mission = objectCrudSvcRemoteWrapper.update(mission);
//            queryResponseRemote = querySvcRemoteWrapper.runNativeQuery("select m from Mission m");
//            list = queryResponseRemote.getResultList();
//            for (BaseObject baseObject : list) {
//                if (baseObject instanceof Mission) {
//                    System.out.println("Object: " + baseObject.getKeyId() + " " + ((Mission) baseObject).getName());
//                }
//            }
//
//
//            System.out.println("");
//            objectCrudSvcRemoteWrapper.userNametest = "aaa";
//            mission = objectCrudSvcRemoteWrapper.create(Mission.class.getCanonicalName());
//            mission.setName("tal1.2");
//            mission = objectCrudSvcRemoteWrapper.update(mission);
//            queryResponseRemote = querySvcRemoteWrapper.runNativeQuery("select m from Mission m");
//            list = queryResponseRemote.getResultList();
//            for (BaseObject baseObject : list) {
//                if (baseObject instanceof Mission) {
//                    System.out.println("Object: " + baseObject.getKeyId() + " " + ((Mission) baseObject).getName());
//                }
//            }
            return Status.SUCCESS;
        }
        catch (Exception e) {
            e.printStackTrace();
            return Status.FAIL;
        }
    }

    @Override
    public Status postTestCleanup() {
        return Status.SUCCESS;
    }

}
