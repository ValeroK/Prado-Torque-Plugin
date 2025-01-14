package jejusoul.com.github.obd_pids_for_hkmc_evs;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.prowl.torque.remote.ITorqueService;

import java.util.List;

import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PermissionManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.TorqueServiceManager;

/**
 * Main entry point for the Prado Torque Plugin.
 * This activity serves as the primary interface between the plugin and Torque Pro.
 * 
 * Core Responsibilities:
 * 1. Plugin Initialization
 *    - Setting up service managers
 *    - Initializing permission handlers
 *    - Managing activity lifecycle
 * 
 * 2. Service Connection
 *    - Establishing connection with Torque Pro
 *    - Handling connection state changes
 *    - Managing service lifecycle
 * 
 * 3. User Interface
 *    - Providing feedback about connection status
 *    - Handling permission-related UI
 *    - Managing navigation
 * 
 * Lifecycle:
 * 1. Activity starts
 * 2. Checks and requests permissions if needed
 * 3. Attempts to bind to Torque service
 * 4. Maintains connection until destroyed
 */
public class PluginActivity extends AppCompatActivity implements TorqueServiceManager.TorqueConnectionListener, PermissionManager.PermissionCallback {
    private static final String TAG = PluginActivity.class.getSimpleName();

    private TorqueServiceManager serviceManager;
    private PermissionManager permissionManager;

    /**
     * Initializes the activity.
     * Sets up:
     * - Service manager
     * - Permission manager
     * - UI components
     * 
     * @param savedInstanceState Saved instance state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin);

        serviceManager = new TorqueServiceManager(this);
        serviceManager.setConnectionListener(this);
        permissionManager = new PermissionManager(this, this);

        if (permissionManager.areStoragePermissionsGranted()) {
            serviceManager.bindToTorqueService();
        } else {
            permissionManager.checkAndRequestStoragePermissions();
        }
    }

    /**
     * Cleans up resources when activity is destroyed.
     * Unbinds from Torque service to prevent leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceManager.unbindFromTorqueService();
    }

    /**
     * Callback when Torque service connection is established.
     * Updates UI and notifies user of successful connection.
     */
    @Override
    public void onTorqueConnected() {
        runOnUiThread(() -> {
            Toast.makeText(this, R.string.torque_connected, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Callback when Torque service is disconnected.
     * Updates UI and notifies user of disconnection.
     */
    @Override
    public void onTorqueDisconnected() {
        runOnUiThread(() -> {
            Toast.makeText(this, R.string.torque_disconnected, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Handles Torque service errors.
     * Displays error message to user and logs the error.
     * 
     * @param error Error message from service
     */
    @Override
    public void onTorqueError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Callback when Torque service is not installed.
     * Displays error message to user and logs the error.
     */
    @Override
    public void onTorqueNotInstalled() {
        runOnUiThread(() -> {
            Toast.makeText(this, R.string.torque_not_installed, Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Callback when permissions are granted.
     * Binds to Torque service.
     */
    @Override
    public void onPermissionsGranted() {
        serviceManager.bindToTorqueService();
    }

    /**
     * Callback when permissions are denied.
     * Displays error message to user and finishes activity.
     * 
     * @param deniedPermissions List of denied permissions
     */
    @Override
    public void onPermissionsDenied(List<String> deniedPermissions) {
        Toast.makeText(this, R.string.permissions_required, Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Handles request permission result.
     * Passes result to permission manager.
     * 
     * @param requestCode Request code
     * @param permissions Permissions array
     * @param grantResults Grant results array
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
