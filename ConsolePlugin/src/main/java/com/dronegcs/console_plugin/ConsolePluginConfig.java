package com.dronegcs.console_plugin;

import com.dronegcs.mavlink.spring.MavlinkSpringConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by taljmars on 7/10/2017.
 */
@Import({MavlinkSpringConfig.class, DroneServerClientPluginConfig.class})
@Configuration
public class ConsolePluginConfig {
}
