package com.dronegcs.console_plugin.services.internal.status;

import com.dronegcs.console_plugin.services.GlobalStatusSvc;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by taljmars on 5/17/17.
 */
@Component
public class GlobalStatusSvcImpl implements GlobalStatusSvc {

    private Map<Component, Boolean> componentBooleanMap;

    @PostConstruct
    private void init() {
        componentBooleanMap = new HashMap<>(Component.values().length);
        for (Component component : Component.values())
            setComponentStatus(component, false);
    }

    @Override
    public boolean isComponentOn(Component component) {
        return componentBooleanMap.get(component).equals(true);
    }

    @Override
    public void setComponentStatus(Component component, boolean isConnected) {
        componentBooleanMap.put(component, isConnected);
    }
}
