/**
 * Manages the connection and communication with the Torque Pro service.
 * This class is responsible for all interactions with the Torque Pro app, including:
 * 
 * Core Responsibilities:
 * 1. Service Connection Management
 *    - Binding to Torque Pro service
 *    - Maintaining service connection state
 *    - Handling connection callbacks
 * 
 * 2. PID Management
 *    - Importing PIDs into Torque Pro
 *    - Formatting PID data for transmission
 *    - Handling import success/failure
 * 
 * 3. Error Handling
 *    - Checking Torque Pro installation
 *    - Managing service disconnections
 *    - Providing error feedback
 * 
 * Usage:
 * 1. Create an instance with a Context
 * 2. Set connection listener
 * 3. Call bindToTorqueService() to establish connection
 * 4. Use importPids() to send PID data to Torque
 */
package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import androidx.core.content.ContextCompat;

import org.prowl.torque.remote.ITorqueService;

import java.util.List;

public class TorqueServiceManager {
    private static final String TAG = TorqueServiceManager.class.getSimpleName();
    private static final String TORQUE_PACKAGE = "org.prowl.torque";
    private static final String TORQUE_SERVICE = "org.prowl.torque.remote.TorqueService";

    private final Context context;
    private final Handler handler;
    private ITorqueService torqueService;
    private ServiceConnection serviceConnection;
    private TorqueConnectionListener connectionListener;
    private PermissionListener permissionListener;
    private boolean isConnected = false;

    /**
     * Interface for handling Torque service connection events.
     * Implementers will receive notifications about:
     * - Successful connections
     * - Disconnections
     * - Connection errors
     * - Installation status
     */
    public interface TorqueConnectionListener {
        void onTorqueConnected();
        void onTorqueDisconnected();
        void onTorqueError(String error);
        void onTorqueNotInstalled();
    }

    public interface PermissionListener {
        void onPermissionRequired();
    }

    /**
     * Constructs a new TorqueServiceManager.
     * 
     * @param context Application context for service binding
     */
    public TorqueServiceManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context.getApplicationContext();
        this.handler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "TorqueServiceManager initialized with context: " + this.context);
    }

    public void setConnectionListener(TorqueConnectionListener listener) {
        this.connectionListener = listener;
    }

    public void setPermissionListener(PermissionListener listener) {
        this.permissionListener = listener;
    }

    /**
     * Gets the current Torque service instance.
     * 
     * @return ITorqueService interface or null if not connected
     */
    public ITorqueService getTorqueService() {
        return torqueService;
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Unbinds from the Torque service if currently bound.
     * Should be called when the connection is no longer needed.
     */
    public void unbindFromTorqueService() {
        if (isConnected) {
            try {
                context.unbindService(serviceConnection);
                Log.d(TAG, "Successfully unbound from Torque service");
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding from service", e);
            } finally {
                isConnected = false;
                torqueService = null;
            }
        }
    }

    /**
     * Checks if Torque Pro is installed on the device.
     * 
     * @return true if Torque Pro is installed, false otherwise
     */
    public boolean isTorqueInstalled() {
        if (context == null) {
            Log.e(TAG, "Context is null when checking Torque installation");
            return false;
        }

        PackageManager pm = context.getPackageManager();
        Intent queryIntent = new Intent("org.prowl.torque.ACTIVITY_PLUGIN");
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(queryIntent, PackageManager.MATCH_ALL);

        if (!resolveInfos.isEmpty()) {
            for (ResolveInfo info : resolveInfos) {
                if (info.activityInfo != null && TORQUE_PACKAGE.equals(info.activityInfo.packageName)) {
                    Log.d(TAG, "Found Torque via intent query");
                    return true;
                }
            }
        }

        // Try service intent
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName(TORQUE_PACKAGE, TORQUE_SERVICE));
        List<ResolveInfo> serviceInfos = pm.queryIntentServices(serviceIntent, PackageManager.MATCH_ALL);

        if (!serviceInfos.isEmpty()) {
            Log.d(TAG, "Found Torque via service query");
            return true;
        }

        Log.w(TAG, "Torque not found via any query method");
        return false;
    }

    /**
     * Attempts to bind to the Torque Pro service.
     * Creates an explicit intent for the Torque service and attempts to bind.
     * 
     * @return true if binding process started successfully, false otherwise
     */
    public boolean bindToTorqueService() {
        Log.d(TAG, "Attempting to bind to Torque service");

        if (context == null) {
            Log.e(TAG, "Context is null when binding to service");
            return false;
        }

        if (!isTorqueInstalled()) {
            Log.d(TAG, "Torque is not installed");
            if (connectionListener != null) {
                connectionListener.onTorqueNotInstalled();
            }
            return false;
        }

        return attemptServiceBinding();
    }

    private boolean attemptServiceBinding() {
        if (isConnected) {
            Log.d(TAG, "Already connected to Torque service");
            return true;
        }

        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(TORQUE_PACKAGE, TORQUE_SERVICE));

            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d(TAG, "Service connected");
                    try {
                        torqueService = ITorqueService.Stub.asInterface(service);
                        isConnected = true;
                        if (connectionListener != null) {
                            connectionListener.onTorqueConnected();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to get Torque interface", e);
                        if (connectionListener != null) {
                            connectionListener.onTorqueError("Failed to get Torque interface: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(TAG, "Service disconnected");
                    torqueService = null;
                    isConnected = false;
                    if (connectionListener != null) {
                        connectionListener.onTorqueDisconnected();
                    }
                }
            };

            boolean bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "Bind attempt result: " + bound);
            return bound;

        } catch (Exception e) {
            Log.e(TAG, "Error binding to service", e);
            if (connectionListener != null) {
                connectionListener.onTorqueError("Error binding to service: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Imports PID data into Torque Pro.
     * Formats and sends PID data using the appropriate service method.
     * 
     * @param pids List of PID data to import
     * @return true if import was successful, false otherwise
     * @throws RemoteException if service communication fails
     */
    public boolean importPids(List<PidData> pids) throws RemoteException {
        if (!isConnected) {
            if (connectionListener != null) {
                connectionListener.onTorqueError("Not connected to Torque Pro");
            }
            return false;
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
                String modeAndPID = pid.getModeAndPID();
                // Remove "0x" prefix if present
                modeAndPIDs[i] = modeAndPID.startsWith("0x") ? modeAndPID.substring(2) : modeAndPID;
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
                Log.d(TAG, "Successfully imported " + size + " PIDs");
                if (connectionListener != null) {
                    connectionListener.onTorqueConnected();
                }
            } else {
                String error = "Failed to import PIDs";
                Log.e(TAG, error);
                if (connectionListener != null) {
                    connectionListener.onTorqueError(error);
                }
            }
            return success;
        } catch (Exception e) {
            String error = "Error importing PIDs: " + e.getMessage();
            Log.e(TAG, error, e);
            if (connectionListener != null) {
                connectionListener.onTorqueError(error);
            }
            return false;
        }
    }
}