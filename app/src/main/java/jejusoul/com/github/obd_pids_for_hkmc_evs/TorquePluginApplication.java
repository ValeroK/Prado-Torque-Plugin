package jejusoul.com.github.obd_pids_for_hkmc_evs;

import android.app.Application;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.TorqueServiceManager;

public class TorquePluginApplication extends Application {
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
