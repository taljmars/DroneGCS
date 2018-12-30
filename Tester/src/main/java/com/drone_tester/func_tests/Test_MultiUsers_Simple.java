package com.drone_tester.func_tests;

import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.db.persistence.scheme.DummyBaseObject;
import com.db.persistence.scheme.QueryRequestRemote;
import com.db.persistence.scheme.QueryResponseRemote;
import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Component
public class Test_MultiUsers_Simple extends Test {

    private int idx = 0;
    private int total = 11;

    private String user1Token;
    private String user2Token;
    private DummyBaseObject baseObjectUser1;
    private DummyBaseObject baseObjectUser2;

    @Override
    public Status preTestCheck() {
        return Status.SUCCESS;
    }

    @Override
    public Status test() {
        try {
            user1Token = login(tester1, tester1);
            restClientHelper.setToken(user1Token);

            baseObjectUser1 = objectCrudSvcRemoteWrapper.create(DummyBaseObject.class.getCanonicalName());
            baseObjectUser1.setName("user1_bObj");
            baseObjectUser1 = objectCrudSvcRemoteWrapper.update(baseObjectUser1);
            Assert.isTrue(baseObjectUser1.getName().equals("user1_bObj"));
            publish(new TestEvent(this, Status.IN_PROGRESS, "update for the first time in private", ++idx, total));
            logout();

            user2Token = login(tester2, tester2);
            restClientHelper.setToken(user2Token);

            baseObjectUser2 = objectCrudSvcRemoteWrapper.create(DummyBaseObject.class.getCanonicalName());
            baseObjectUser2.setName("user2_bObj");
            baseObjectUser2 = objectCrudSvcRemoteWrapper.update(baseObjectUser2);
            Assert.isTrue(baseObjectUser2.getName().equals("user2_bObj"));
            publish(new TestEvent(this, Status.IN_PROGRESS, "update for the first time in private", ++idx, total));

            QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
            queryRequestRemote.setQuery("GetAllDummyBaseObject");
            queryRequestRemote.setClz(DummyBaseObject.class.getCanonicalName());
            QueryResponseRemote res = querySvcRemoteWrapper.query(queryRequestRemote);
            List<BaseObject> lst = res.getResultList();
            Assert.isTrue(lst.size() == 1);

            Assert.isTrue(baseObjectUser2.getName().equals(((DummyBaseObject)lst.get(0)).getName()));

//            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publishing", ++idx, total));

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

            user1Token = login(tester1, tester1);
            restClientHelper.setToken(user1Token);

            Assert.isTrue(baseObjectUser1 != null, "Object is null");
            Assert.isTrue(baseObjectUser2 != null, "Object is null");

            baseObjectUser1 = objectCrudSvcRemoteWrapper.read(baseObjectUser1.getKeyId().getObjId());
            Assert.isTrue(baseObjectUser1 != null, "Fail to get base object from DB");
            publish(new TestEvent(this, Status.IN_PROGRESS, "getting object to clean", ++idx, total));

            objectCrudSvcRemoteWrapper.delete(baseObjectUser1);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Object was cleaned", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publish same object once again", ++idx, total));

            try {
                baseObjectUser2 = objectCrudSvcRemoteWrapper.read(baseObjectUser2.getKeyId().getObjId());
                Assert.isTrue(false, "Object should not be available");
            }
            catch (ObjectNotFoundRemoteException e) {
                publish(new TestEvent(this, Status.IN_PROGRESS, "Object deleted", ++idx, total));
            }
            logout();

            user2Token = login(tester2, tester2);
            restClientHelper.setToken(user2Token);

            baseObjectUser2 = objectCrudSvcRemoteWrapper.read(baseObjectUser2.getKeyId().getObjId());
            Assert.isTrue(baseObjectUser1 != null, "Fail to get base object from DB");
            publish(new TestEvent(this, Status.IN_PROGRESS, "getting object to clean", ++idx, total));

            objectCrudSvcRemoteWrapper.delete(baseObjectUser2);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Object was cleaned", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publish same object once again", ++idx, total));

            logout();

            publish(new TestEvent(this, Status.SUCCESS, "test completed", ++idx, total));
            return Status.SUCCESS;
        }
        catch (Exception e) {
            e.printStackTrace();
            return Status.FAIL;
        }
    }

}
