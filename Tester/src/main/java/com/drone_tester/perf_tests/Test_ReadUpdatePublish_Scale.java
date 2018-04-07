package com.drone_tester.perf_tests;

import com.db.persistence.scheme.DummyBaseObject;
import com.drone_tester.Test;
import com.drone_tester.TestEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class Test_ReadUpdatePublish_Scale extends Test {

    private static final int OBJECTS_AMOUNT = 500;

    private int idx = 0;
    private int total = 4 * OBJECTS_AMOUNT + 3;

    private DummyBaseObject baseObject;

    private List<String> objIds = new ArrayList();

    @Override
    public Status preTestCheck() {
        restClientHelper.setToken(login("tester1", "tester1"));
        detailedResult = new ArrayList<>();
        detailedResult.add(Arrays.asList("","Details"));
        detailedResult.add(Arrays.asList("", "Timestamp", "Existing objects", "Create(MSec)", "Update(MSec)","Read(MSec)", "ReadByClass(MSec)" ));
        startClock(this);
        return Status.SUCCESS;
    }

    @Override
    public Status test() {
        try {
            Long durationCreate = null, durationUpdate = null, durationRead = null, durationReadByClass = null;;
            int interval = 10;
            for (int i = 0 ; i < OBJECTS_AMOUNT ; i++) {
                if (i != 0 && i % interval == 0) startClock();

                baseObject = objectCrudSvcRemoteWrapper.create(DummyBaseObject.class.getCanonicalName());
                baseObject.setName("tal" + i);
                publish(new TestEvent(this, Status.IN_PROGRESS, "Create new Object", ++idx, total));
                if (i != 0 && i % interval == 0) durationCreate = stopClock();

                baseObject = objectCrudSvcRemoteWrapper.update(baseObject);
                Assert.isTrue(baseObject.getName().equals("tal" + i));
                publish(new TestEvent(this, Status.IN_PROGRESS, "update for the first time in private", ++idx, total));
                if (i != 0 && i % interval == 0) durationUpdate = stopClock();

                baseObject = objectCrudSvcRemoteWrapper.read(baseObject.getKeyId().getObjId());
                Assert.isTrue(baseObject.getName().equals("tal" + i));
                publish(new TestEvent(this, Status.IN_PROGRESS, "read object", ++idx, total));
                if (i != 0 && i % interval == 0) durationRead = stopClock();

                baseObject = objectCrudSvcRemoteWrapper.readByClass(baseObject.getKeyId().getObjId(), DummyBaseObject.class.getCanonicalName());
                Assert.isTrue(baseObject.getName().equals("tal" + i));
                publish(new TestEvent(this, Status.IN_PROGRESS, "read by class object", ++idx, total));
                if (i != 0 && i % interval == 0) durationReadByClass = stopClock();

                objIds.add(baseObject.getKeyId().getObjId());

                if (i != 0 && i % interval == 0) {
                    detailedResult.add(Arrays.asList("",pickClock(this),i-interval,durationCreate,durationUpdate,durationRead,durationReadByClass));
                }
            }

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

            startClock();
            sessionsSvcRemoteWrapper.discard();
            publish(new TestEvent(this, Status.IN_PROGRESS, "publish same object once again", ++idx, total));
            long duration = stopClock();

            detailedResult.add(Arrays.asList("","Discarding",OBJECTS_AMOUNT, "objects",duration + " MSec", (duration / 1000) + " Sec"));

            startClock();
            logout();
            duration = stopClock();
            detailedResult.add(Arrays.asList("","Logout","time", duration+" MSec",(duration/1000) + " Sec"));

            publish(new TestEvent(this, Status.SUCCESS, "test completed", ++idx, total));
            return Status.SUCCESS;
        }
        catch (Exception e) {
            e.printStackTrace();
            return Status.FAIL;
        }
    }

}
