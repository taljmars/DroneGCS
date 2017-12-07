package com.drone_tester;

import com.drone_tester.Tests.*;
import com.generic_tools.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class Tester implements ApplicationListener<TestEvent> {

    @Autowired Test_SingleMissionSingleItem test_singleMissionSingleItem;
    @Autowired Test_DiscardPublish          test_discardPublish;
    @Autowired Test_MissionObjectCreation   test_missionObjectCreation;
    @Autowired Test_PolylinePerimeters      test_polylinePerimeters;
    @Autowired Test_CirclePerimeters        test_circlePerimeters;
    @Autowired Test_DummyObject             test_dummyObject;

    @Autowired Logger logger;

    @PostConstruct
    private void init() {
        logger.setConsoleLevel(Logger.Level.WARNING);
        logger.setLevel(Logger.Level.INFO);
    }

    public List<Test> getTestList() {
        List<Test> lst = new ArrayList<>();

        lst.add(test_dummyObject);
        lst.add(test_circlePerimeters);
        lst.add(test_polylinePerimeters);
        lst.add(test_missionObjectCreation);
        lst.add(test_discardPublish);
        lst.add(test_singleMissionSingleItem);

        return lst;
    }


    public static void main(String[] args){
        System.err.println("\n\n\n*************  Tester - Start  *************\n");
        System.setProperty("CONF.DIR", args[1]);

        Tester tester = TestSpringConfig.context.getBean(Tester.class);
        tester.go();

        System.err.println("\n*************  Tester - Done  *************\n");
        System.exit(0);
    }

    private void go() {
        logger.LogGeneralMessege("Running %d tests", getTestList().size());

        int success = 0;
        Throwable throwable = null;

        try {
            for (Test test : getTestList()) {
                test.publish(new TestEvent(test, Test.Status.BEGIN, test.getClass().getSimpleName(), 0, 0));
                if (test.preTestCheck().equals(Test.Status.FAIL) ||
                        test.test().equals(Test.Status.FAIL) ||
                        test.postTestCleanup().equals(Test.Status.FAIL))
                    break;

                success++;
            }

        }
        catch (Throwable t) {
            throwable = t;
        }

        logger.LogErrorMessege("\nSuccess Rate: %d/%d",success, getTestList().size());
        if (throwable != null) {
            logger.LogErrorMessege("Exceptions: %s", throwable);
            throwable.printStackTrace();
        }
        logger.close();
    }

    @Override
    public void onApplicationEvent(TestEvent event) {
        handleProgressOutput(event);
    }

    private int lastMarkersAmount = 0;
    private static final int MARKER_TOTAL = 30;
    private void handleProgressOutput(TestEvent event) {
        String marker = "#";
        int markersAmount = (int) ((1.0 * event.getMsgId()/event.getMsgAmount()) * MARKER_TOTAL);
        int percentage = (int) ((1.0 * event.getMsgId()/event.getMsgAmount()) * 100);
        switch (event.getStatus()) {
            case BEGIN:
                lastMarkersAmount = 0;
                logger.LogGeneralMessege("%s STARTED", event.getMsg());
                break;
            case IN_PROGRESS:
                if (lastMarkersAmount != markersAmount) {
                    System.err.print(String.join("", Collections.nCopies(markersAmount - lastMarkersAmount, marker)));
                    lastMarkersAmount = markersAmount;
                }
                logger.LogGeneralMessege("%s IN PROGRESS (%d%%) - %s", event.getTest().getClass().getSimpleName(), percentage, event.getMsg());
                break;
            case SUCCESS:
                if (event.getMsgAmount() != event.getMsgId()) {
                    logger.LogErrorMessege("Unexpected amount of stages, found %d, expected %d", event.getMsgId(), event.getMsgAmount());
                }
                String padding = String.join("", Collections.nCopies(MARKER_TOTAL - lastMarkersAmount, marker));

                System.err.println(padding + " | Tested Succeeded - " + event.getTest().getClass().getSimpleName());
                logger.LogGeneralMessege("%s FINISHED successfully ", event.getTest().getClass().getSimpleName());
                break;
            case FAIL:
                System.err.println(marker + " | Tested Failed - " + event.getTest().getClass().getSimpleName());
                logger.LogErrorMessege("%s FINISHED with errors ", event.getTest().getClass().getSimpleName());
                break;
            default:
                logger.LogErrorMessege("What???");
        }
    }
}
