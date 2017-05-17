package com.dronegcs.console_plugin.services.internal.status;

import com.dronegcs.console_plugin.services.GlobalStatusSvc;
import org.springframework.stereotype.Component;

/**
 * Created by oem on 5/17/17.
 */
@Component
public class GlobalStatusSvcImpl implements GlobalStatusSvc {

    private boolean antennaConnected;
    private boolean detectorConnected;

    @Override
    public boolean isAntennaConnected() {
        return antennaConnected;
    }

    @Override
    public void setAntennaConnection(boolean isConnected) {
        this.antennaConnected = isConnected;
    }

    @Override
    public boolean isDetectorConnected() {
        return detectorConnected;
    }

    @Override
    public void setDetectorConnected(boolean isConnected) {
        this.detectorConnected = isConnected;
    }
}
