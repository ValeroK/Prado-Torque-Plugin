package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.prowl.torque.remote.ITorqueService;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PidData;

import java.util.List;

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
        return PermissionManager.isTorquePermissionGranted(context);
    }

    public boolean isConnected() {
        return isBound && torqueService != null;
    }

    public void importPids(List<PidData> pids) {
        if (!isConnected()) {
            if (connectionListener != null) {
                connectionListener.onTorqueError("Not connected to Torque Pro");
            }
            return;
        }

        try {
            int size = pids.size();
            String[] names = new String[size];
            String[] shortNames = new String[size];
            String[] modeAndPIDs = new String[size];
            String[] equations = new String[size];
            float[] minValues = new float[size];
            float[] maxValues = new float[size];
            String[] units = new String[size];
            String[] headers = new String[size];

            for (int i = 0; i < size; i++) {
                PidData pid = pids.get(i);
                names[i] = pid.getName();
                shortNames[i] = pid.getShortName();
                modeAndPIDs[i] = pid.getModeAndPID();
                equations[i] = pid.getEquation();
                minValues[i] = pid.getMinValue();
                maxValues[i] = pid.getMaxValue();
                units[i] = pid.getUnit();
                headers[i] = pid.getHeader();
            }

            boolean success = torqueService.sendPIDDataPrivate(
                "jejusoul.com.github.obd_pids_for_hkmc_evs",
                names,
                shortNames,
                modeAndPIDs,
                equations,
                minValues,
                maxValues,
                units,
                headers
            );

            if (success) {
                if (connectionListener != null) {
                    connectionListener.onTorqueConnected(); // Refresh UI state
                }
            } else {
                if (connectionListener != null) {
                    connectionListener.onTorqueError("Failed to import PIDs");
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error importing PIDs", e);
            if (connectionListener != null) {
                connectionListener.onTorqueError("Failed to import PIDs: " + e.getMessage());
            }
        }
    }
}