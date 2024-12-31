package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.prowl.torque.remote.ITorqueService;

public class TorqueServiceManager {
    private static final String TAG = "TorqueServiceManager";
    private Context context;
    private ITorqueService torqueService;
    private boolean isBound = false;
    private TorqueConnectionListener connectionListener;

    public interface TorqueConnectionListener {
        void onTorqueConnected();
        void onTorqueDisconnected();
        void onTorqueError(String error);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            torqueService = ITorqueService.Stub.asInterface(service);
            isBound = true;
            
            if (connectionListener != null) {
                connectionListener.onTorqueConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            torqueService = null;
            isBound = false;
            if (connectionListener != null) {
                connectionListener.onTorqueDisconnected();
            }
        }
    };

    public TorqueServiceManager(Context context) {
        this.context = context;
    }

    public void setConnectionListener(TorqueConnectionListener listener) {
        this.connectionListener = listener;
    }

    public boolean bindToTorqueService() {
        if (!isBound) {
            try {
                // Check if Torque is installed
                PackageManager pm = context.getPackageManager();
                try {
                    pm.getPackageInfo("org.prowl.torque", PackageManager.GET_ACTIVITIES);
                } catch (PackageManager.NameNotFoundException e) {
                    if (connectionListener != null) {
                        connectionListener.onTorqueError("Torque Pro is not installed. Please install it first.");
                    }
                    return false;
                }

                // Create the service intent
                Intent intent = new Intent();
                intent.setClassName("org.prowl.torque", "org.prowl.torque.remote.TorqueService");
                
                // Try to start the service first
                try {
                    context.startService(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to start Torque service", e);
                }

                // Attempt to bind with BIND_AUTO_CREATE flag
                boolean successfulBind = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                
                if (!successfulBind) {
                    Log.e(TAG, "Failed to bind to Torque service");
                    if (connectionListener != null) {
                        connectionListener.onTorqueError("Failed to connect to Torque Pro. Please ensure Torque Pro is running and try again.");
                    }
                }
                
                return successfulBind;
            } catch (Exception e) {
                Log.e(TAG, "Error binding to Torque service", e);
                if (connectionListener != null) {
                    connectionListener.onTorqueError("Error connecting to Torque Pro: " + e.getMessage());
                }
                return false;
            }
        }
        return true;
    }

    public void unbindFromTorqueService() {
        if (isBound) {
            context.unbindService(serviceConnection);
            isBound = false;
            torqueService = null;
        }
    }

    public ITorqueService getTorqueService() {
        return torqueService;
    }

    public boolean checkFullPermissions() {
        if (torqueService != null) {
            try {
                return torqueService.hasFullPermissions();
            } catch (RemoteException e) {
                Log.e(TAG, "Error checking Torque permissions", e);
                if (connectionListener != null) {
                    connectionListener.onTorqueError("Failed to check Torque permissions: " + e.getMessage());
                }
            }
        }
        return false;
    }
}