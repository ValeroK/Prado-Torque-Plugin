package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jejusoul.com.github.obd_pids_for_hkmc_evs.R;

public class PermissionManager {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String TORQUE_PERMISSION = "org.prowl.torque.permission.HANDSHAKE";
    
    private static final Map<String, Integer> PERMISSION_DESCRIPTIONS = new HashMap<String, Integer>() {{
        put(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_storage);
        put(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage);
        put(TORQUE_PERMISSION, R.string.permission_torque);
        put(Manifest.permission.INTERNET, R.string.permission_internet);
    }};

    private final String[] STORAGE_PERMISSIONS;
    private final Activity activity;
    private final PermissionCallback callback;

    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied(List<String> deniedPermissions);
    }

    public PermissionManager(Activity activity, PermissionCallback callback) {
        this.activity = activity;
        this.callback = callback;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            STORAGE_PERMISSIONS = new String[] {
                Manifest.permission.INTERNET
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            STORAGE_PERMISSIONS = new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
            };
        } else {
            STORAGE_PERMISSIONS = new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
            };
        }
    }

    public boolean areStoragePermissionsGranted() {
        for (String permission : STORAGE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean isTorquePermissionGranted() {
        return ContextCompat.checkSelfPermission(activity, TORQUE_PERMISSION) 
                == PackageManager.PERMISSION_GRANTED;
    }

    public void checkAndRequestStoragePermissions() {
        List<String> missingPermissions = new ArrayList<>();
        
        for (String permission : STORAGE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            showMissingPermissionsDialog(missingPermissions);
        } else {
            callback.onPermissionsGranted();
        }
    }

    public void requestTorquePermission() {
        if (!isTorquePermissionGranted()) {
            List<String> permissions = new ArrayList<>();
            permissions.add(TORQUE_PERMISSION);
            showMissingPermissionsDialog(permissions);
        }
    }

    private void showMissingPermissionsDialog(List<String> permissions) {
        StringBuilder message = new StringBuilder();
        for (String permission : permissions) {
            Integer descriptionId = PERMISSION_DESCRIPTIONS.get(permission);
            if (descriptionId != null) {
                if (message.length() > 0) {
                    message.append("\n");
                }
                message.append("- ").append(activity.getString(descriptionId));
            }
        }

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_permissions, null);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        messageView.setText(activity.getString(R.string.missing_permissions_message, message.toString()));

        new AlertDialog.Builder(activity)
                .setTitle(R.string.missing_permissions_title)
                .setView(dialogView)
                .setPositiveButton(R.string.continue_anyway, (dialog, which) -> {
                    ActivityCompat.requestPermissions(activity,
                            permissions.toArray(new String[0]),
                            PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                    callback.onPermissionsDenied(permissions);
                })
                .setCancelable(false)
                .show();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            
            if (deniedPermissions.isEmpty()) {
                callback.onPermissionsGranted();
            } else {
                callback.onPermissionsDenied(deniedPermissions);
            }
        }
    }
}
