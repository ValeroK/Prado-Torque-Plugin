package jejusoul.com.github.obd_pids_for_hkmc_evs;

import android.app.Application;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.TorqueServiceManager;

/**
 * TorquePluginApplication is the main Application class for the Prado Torque Plugin.
 * 
 * This class serves as the central point for:
 * - Application-wide initialization
 * - Singleton instance management
 * - Global state management
 * - Service manager initialization
 * 
 * Key Responsibilities:
 * - Initializing TorqueServiceManager
 * - Providing global access to service manager
 * - Managing application lifecycle
 * 
 * The class follows the Singleton pattern to ensure:
 * - Single instance of TorqueServiceManager
 * - Consistent state across the application
 * - Efficient resource management
 * 
 * Usage:
 * TorquePluginApplication app = (TorquePluginApplication) getApplication();
 * TorqueServiceManager manager = app.getTorqueServiceManager();
 */
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
