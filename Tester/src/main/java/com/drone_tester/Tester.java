package com.drone_tester;

import com.drone_tester.func_tests.*;
import com.drone_tester.perf_tests.Test_ReadUpdatePublish_Scale;
import com.dronegcs.console_plugin.remote_services_wrappers.RegistrationSvcRemoteWrapper;
import com.generic_tools.csv.CSV;
import com.generic_tools.csv.CSVFactory;
import com.generic_tools.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class Tester implements ApplicationListener<TestEvent> {

    private @Autowired RegistrationSvcRemoteWrapper registrationSvcRemote;

    private @Autowired Test_Login                   test_login;
    private @Autowired Test_UserRegistration        test_userRegistration;
    private @Autowired Test_DummyObject             test_dummyObject;
    private @Autowired Test_MultiUsers_Simple       test_multiUsers_simple;
    private @Autowired Test_DiscardPublish          test_discardPublish;
    private @Autowired Test_PolylinePerimeters      test_polylinePerimeters;
    private @Autowired Test_CirclePerimeters        test_circlePerimeters;
    private @Autowired Test_MissionObjectCreation   test_missionObjectCreation;
    private @Autowired Test_SingleMissionSingleItem test_singleMissionSingleItem;

    private @Autowired Test_ReadUpdatePublish_Scale test_readUpdatePublish_scale;

    private @Autowired Logger logger;

    private CSV csvReport = null;

    @PostConstruct
    private void init() {
        logger.setConsoleLevel(Logger.Level.WARNING);
        logger.setLevel(Logger.Level.INFO);
    }

    public List<Test> getTestsList() {
        List<Test> lst = new ArrayList<>();

        lst.add(test_userRegistration);
        lst.add(test_login);
        lst.add(test_dummyObject);
        lst.add(test_multiUsers_simple);
        lst.add(test_discardPublish);
        lst.add(test_circlePerimeters);
        lst.add(test_polylinePerimeters);
        lst.add(test_missionObjectCreation);
        lst.add(test_singleMissionSingleItem);
//
        lst.add(test_readUpdatePublish_scale);

        return lst;
    }
    public static void main(String[] args) throws InterruptedException {
        System.out.println("\n\n\n*************  Tester - Start  *************\n");
        System.setProperty("CONF.DIR", args[1]);

        Tester tester = TestSpringConfig.context.getBean(Tester.class);

        boolean res = tester.go();

        System.out.println("\n*************  Tester - Done  *************\n");
        System.exit(res ? 0 : -1);
    }

    private boolean go() {
        csvReport  = CSVFactory.createNew("C:\\Users\\taljmars\\Workspace\\DroneGCS\\TestReport_" + new Date().getTime());
        csvReport.addEntry(Arrays.asList("Test Report for " + new Date().toString()));

        int testsAmount = getTestsList().size();
        logger.LogGeneralMessege("Running %d tests", testsAmount);
        csvReport.addEntry(Arrays.asList("Running " + testsAmount + " tests"));
        csvReport.addEmptyLine();

        int success = 0;
        Throwable throwable = null;
        List entry = null;
        long beginTimestamp = 0, endTimestamp = 0;

        try {
            csvReport.addEntry(Arrays.asList("Test Name", "Results", "Time (MSec)", "Time (Sec)", "Comments"));
            for (Test test : getTestsList()) {
                entry = new ArrayList();
                entry.add(test.getClass().getSimpleName());
                beginTimestamp = new Date().getTime();

                test.publish(new TestEvent(test, Test.Status.BEGIN, test.getClass().getSimpleName(), 0, 0));
                if (test.preTestCheck().equals(Test.Status.FAIL) ||
                        test.test().equals(Test.Status.FAIL) ||
                        test.postTestCleanup().equals(Test.Status.FAIL))
                    break;

                endTimestamp = new Date().getTime();
                entry.add("Passed");
                entry.add(endTimestamp - beginTimestamp);
                entry.add((endTimestamp - beginTimestamp) / 1000);
                csvReport.addEntry(entry);
                List<List<Object>> restLists = test.getDetailsTable();
                if (restLists != null && !restLists.isEmpty()) {
                    csvReport.addEntries(restLists);
                }

                success++;
            }

        }
        catch (Throwable t) {
            throwable = t;
        }

        logger.LogErrorMessege("\nSuccess Rate: %d/%d",success, getTestsList().size());
        if (throwable != null) {
            entry.add("Failed");
            entry.add(endTimestamp - beginTimestamp);
            entry.add((endTimestamp - beginTimestamp) / 1000);
            entry.add(throwable.getMessage());
            csvReport.addEntry(entry);

            logger.LogErrorMessege("Exceptions: %s", throwable);
            throwable.printStackTrace();
        }
        logger.close();

        CSVFactory.closeFile(csvReport);

        return success == getTestsList().size();
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
                    System.out.print(String.join("", Collections.nCopies(markersAmount - lastMarkersAmount, marker)));
                    lastMarkersAmount = markersAmount;
                }
                logger.LogGeneralMessege("%s IN PROGRESS (%d%%) - %s", event.getTest().getClass().getSimpleName(), percentage, event.getMsg());
                break;
            case SUCCESS:
                if (event.getMsgAmount() != event.getMsgId()) {
                    logger.LogErrorMessege("Unexpected amount of stages, found %d, expected %d", event.getMsgId(), event.getMsgAmount());
                }
                String padding = String.join("", Collections.nCopies(MARKER_TOTAL - lastMarkersAmount, marker));

                System.out.println(padding + " | Tested Succeeded - " + event.getTest().getClass().getSimpleName());
                logger.LogGeneralMessege("%s FINISHED successfully ", event.getTest().getClass().getSimpleName());
                break;
            case FAIL:
                System.out.println(marker + " | Tested Failed - " + event.getTest().getClass().getSimpleName());
                logger.LogErrorMessege("%s FINISHED with errors ", event.getTest().getClass().getSimpleName());
                break;
            default:
                logger.LogErrorMessege("What???");
        }
    }
}
