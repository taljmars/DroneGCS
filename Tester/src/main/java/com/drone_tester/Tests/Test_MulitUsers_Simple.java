package com.drone_tester.Tests;

import com.db.persistence.scheme.BaseObject;
import com.db.persistence.scheme.DummyBaseObject;
import com.db.persistence.wsSoap.QueryResponseRemote;
import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Component
public class Test_MulitUsers_Simple extends Test {

    private int idx = 0;
    private int total = 15;

    private DummyBaseObject baseObjectUser1;
    private DummyBaseObject baseObjectUser2;

    @Override
    public Status preTestCheck() {
        return Status.SUCCESS;
    }

    @Override
    public Status test() {
        try {
            restClientHelper.setToken(login("tester1", "tester1"));
            baseObjectUser1 = objectCrudSvcRemoteWrapper.create(DummyBaseObject.class.getCanonicalName());
            baseObjectUser1.setName("user1_bObj");
            baseObjectUser1 = objectCrudSvcRemoteWrapper.update(baseObjectUser1);
            Assert.isTrue(baseObjectUser1.getName().equals("user1_bObj"));
            publish(new TestEvent(this, Status.IN_PROGRESS, "update for the first time in private", ++idx, total));
            logout();

            restClientHelper.setToken(login("tester2", "tester2"));
            baseObjectUser2 = objectCrudSvcRemoteWrapper.create(DummyBaseObject.class.getCanonicalName());
            baseObjectUser2.setName("user2_bObj");
            baseObjectUser2 = objectCrudSvcRemoteWrapper.update(baseObjectUser2);
            Assert.isTrue(baseObjectUser2.getName().equals("user2_bObj"));
            publish(new TestEvent(this, Status.IN_PROGRESS, "update for the first time in private", ++idx, total));

            QueryResponseRemote res = querySvcRemoteWrapper.runNativeQueryWithClass("select * from dummyBaseObject", DummyBaseObject.class.getName());
            List<BaseObject> lst = res.getResultList();
            for (BaseObject b : lst)
                System.out.println(b);

//            sessionsSvcRemoteWrapper.publish();
//            publish(new TestEvent(this, Status.IN_PROGRESS, "publishing", ++idx, total));

            logout();

            return Status.SUCCESS;
        }
        catch (Exception e) {
            e.printStackTrace();
            return Status.FAIL;
        }
    }

    @Override
    public Status postTestCleanup() {
        try {
//            Assert.isTrue(baseObject != null, "Object is null");
//
//            baseObject = objectCrudSvcRemoteWrapper.read(baseObject.getKeyId().getObjId());
//            Assert.isTrue(baseObject != null, "Fail to get base object from DB");
//            publish(new TestEvent(this, Status.IN_PROGRESS, "getting object to clean", ++idx, total));
//
//            objectCrudSvcRemoteWrapper.delete(baseObject);
//            publish(new TestEvent(this, Status.IN_PROGRESS, "Object was cleaned", ++idx, total));
//
//            sessionsSvcRemoteWrapper.publish();
//            publish(new TestEvent(this, Status.IN_PROGRESS, "publish same object once again", ++idx, total));
//
//            try {
//                baseObject = objectCrudSvcRemoteWrapper.read(baseObject.getKeyId().getObjId());
//                Assert.isTrue(false, "Object should not be available");
//            }
//            catch (ObjectNotFoundRemoteException e) {
//                publish(new TestEvent(this, Status.IN_PROGRESS, "Object deleted", ++idx, total));
//            }
//
//            publish(new TestEvent(this, Status.SUCCESS, "test completed", ++idx, total));
            return Status.SUCCESS;
        }
        catch (Exception e) {
            e.printStackTrace();
            return Status.FAIL;
        }
    }

}
