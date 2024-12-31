package jejusoul.com.github.obd_pids_for_hkmc_evs;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import org.prowl.torque.remote.ITorqueService;

import java.util.List;

import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PermissionManager;

public class PluginActivity extends AppCompatActivity implements PermissionManager.PermissionCallback {
    private static final String TAG = "PluginActivity";
    private static final int SERVICE_TIMEOUT_MS = 5000; // 5 seconds timeout
    
    private ITorqueService torqueService;
    private MainViewModel viewModel;
    private PermissionManager permissionManager;
    private NavController navController;
    private Handler timeoutHandler;
    private boolean isServiceBound = false;

    private final Runnable serviceTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isServiceBound) {
                Log.e(TAG, "Torque service connection timeout");
                Toast.makeText(PluginActivity.this, 
                    R.string.error_torque_connection_timeout, 
                    Toast.LENGTH_LONG).show();
            }
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            Log.d(TAG, "Service connected");
            isServiceBound = true;
            timeoutHandler.removeCallbacks(serviceTimeoutRunnable);
            torqueService = ITorqueService.Stub.asInterface(service);
            viewModel.setTorqueService(torqueService);
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service disconnected");
            isServiceBound = false;
            torqueService = null;
            viewModel.setTorqueService(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        permissionManager = new PermissionManager(this, this);
        timeoutHandler = new Handler(Looper.getMainLooper());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Bind to the torque service
        Intent intent = new Intent();
        intent.setClassName("org.prowl.torque", "org.prowl.torque.remote.TorqueService");
        if (!bindService(intent, connection, BIND_AUTO_CREATE)) {
            Log.e(TAG, "Failed to bind to Torque service");
            Toast.makeText(this, R.string.error_bind_service, Toast.LENGTH_LONG).show();
        } else {
            timeoutHandler.postDelayed(serviceTimeoutRunnable, SERVICE_TIMEOUT_MS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(connection);
            isServiceBound = false;
        }
        timeoutHandler.removeCallbacks(serviceTimeoutRunnable);
    }

    @Override
    public void onPermissionsGranted() {
        Toast.makeText(this, R.string.permissions_granted, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(List<String> deniedPermissions) {
        Toast.makeText(this, R.string.permissions_required, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
