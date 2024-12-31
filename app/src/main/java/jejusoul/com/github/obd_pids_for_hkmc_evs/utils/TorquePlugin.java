package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

import android.app.Application;

public class TorquePlugin extends Application {
    private TorqueServiceManager torqueServiceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        torqueServiceManager = new TorqueServiceManager(this);
    }

    public TorqueServiceManager getTorqueServiceManager() {
        return torqueServiceManager;
    }
}
