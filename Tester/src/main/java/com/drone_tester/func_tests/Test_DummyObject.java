package com.drone_tester.func_tests;

import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.DummyBaseObject;
import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class Test_DummyObject extends Test {

    private int idx = 0;
    private int total = 15;

    private DummyBaseObject baseObject;

    @Override
    public Status preTestCheck() {
        restClientHelper.setToken(login(tester1, tester1));
//        System.out.println(restClientHelper.getToken());
        return Status.SUCCESS;
    }

    @Override
    public Status test() {
        try {
//            System.out.println(restClientHelper.getToken());
            baseObject = objectCrudSvcRemoteWrapper.create(DummyBaseObject.class.getCanonicalName());
            baseObject.setName("tal1");
            baseObject = objectCrudSvcRemoteWrapper.update(baseObject);
            Assert.isTrue(baseObject.getName().equals("tal1"));
            publish(new TestEvent(this, Status.IN_PROGRESS, "update for the first time in private", ++idx, total));

            baseObject.setName("tal2");
            baseObject = objectCrudSvcRemoteWrapper.update(baseObject);
            Assert.isTrue(baseObject.getName().equals("tal2"), "Name not as expected");
            publish(new TestEvent(this, Status.IN_PROGRESS, "update for the second time in private", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publishing", ++idx, total));

//            System.out.println(baseObject.getKeyId().getObjId());
            baseObject = objectCrudSvcRemoteWrapper.read(baseObject.getKeyId().getObjId());
            int fromRevision = baseObject.getFromRevision();
            int toRevision = baseObject.getKeyId().getToRevision();
            Assert.isTrue(baseObject != null && baseObject.getName().equals("tal2"));
            publish(new TestEvent(this, Status.IN_PROGRESS, "modify after publish for the first time", ++idx, total));

            baseObject.setName("tal3");
            baseObject = objectCrudSvcRemoteWrapper.update(baseObject);
            Assert.isTrue(baseObject.getName().equals("tal3"));
            publish(new TestEvent(this, Status.IN_PROGRESS, "modify after publish for the second time", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publish same object once again", ++idx, total));

            baseObject = objectCrudSvcRemoteWrapper.read(baseObject.getKeyId().getObjId());
            Assert.isTrue(baseObject != null && baseObject.getName().equals("tal3"));
            Assert.isTrue(baseObject.getFromRevision() > fromRevision, "'from' revision was changed after publish");
            Assert.isTrue(baseObject.getKeyId().getToRevision() == toRevision, "'to' revision was not changed after publish");
            publish(new TestEvent(this, Status.IN_PROGRESS, "modify after publish for the 1st time", ++idx, total));

            baseObject.setName("tal4");
            baseObject = objectCrudSvcRemoteWrapper.update(baseObject);
            Assert.isTrue(baseObject.getName().equals("tal4"));
            publish(new TestEvent(this, Status.IN_PROGRESS, "modify after publish for the 2nd time", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publish same object once again", ++idx, total));

            baseObject = objectCrudSvcRemoteWrapper.read(baseObject.getKeyId().getObjId());
            Assert.isTrue(baseObject != null && baseObject.getName().equals("tal4"));
            Assert.isTrue(baseObject.getFromRevision() > fromRevision, "'from' revision was changed after publish");
            Assert.isTrue(baseObject.getKeyId().getToRevision() == toRevision, "'to' revision was not changed after publish");
            publish(new TestEvent(this, Status.IN_PROGRESS, "modify after publish for the 1st time", ++idx, total));

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
            Assert.isTrue(baseObject != null, "Object is null");

            baseObject = objectCrudSvcRemoteWrapper.read(baseObject.getKeyId().getObjId());
            Assert.isTrue(baseObject != null, "Fail to get base object from DB");
            publish(new TestEvent(this, Status.IN_PROGRESS, "getting object to clean", ++idx, total));

            objectCrudSvcRemoteWrapper.delete(baseObject);
            publish(new TestEvent(this, Status.IN_PROGRESS, "Object was cleaned", ++idx, total));

            sessionsSvcRemoteWrapper.publish();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publish same object once again", ++idx, total));

            try {
                baseObject = objectCrudSvcRemoteWrapper.read(baseObject.getKeyId().getObjId());
                Assert.isTrue(false, "Object should not be available");
            }
            catch (ObjectNotFoundRemoteException e) {
                publish(new TestEvent(this, Status.IN_PROGRESS, "Object deleted", ++idx, total));
            }

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
