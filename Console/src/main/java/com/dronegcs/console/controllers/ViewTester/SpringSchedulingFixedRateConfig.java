package com.dronegcs.console.controllers.ViewTester;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ComponentScan("com.dronegcs.console.controllers.ViewTester")
public class SpringSchedulingFixedRateConfig {

    @Scheduled(fixedRate = 100)
    public void scheduleFixedRateTask() {
        System.out.println(
                "Fixed rate task - " + System.currentTimeMillis() / 1000);
    }
}
