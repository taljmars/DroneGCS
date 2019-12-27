package com.dronegcs.console_plugin.event_logger;

import java.util.Date;

public interface EventLogManager {

    EventLogBundle getAllEventLogs();

    EventLogBundle getAllEventLogsBetween(Date startDate, Date endDate);

//    EventLogBundle getLastEvents();
}
