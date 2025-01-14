package jejusoul.com.github.obd_pids_for_hkmc_evs;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.prowl.torque.remote.ITorqueService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.TorqueServiceManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PidData;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.CSVDataManager;

/**
 * Activity responsible for importing PIDs (Parameter IDs) into Torque Pro.
 * This activity handles:
 * 1. Loading PID data from CSV files
 * 2. Connecting to the Torque Pro service
 * 3. Importing the PIDs into Torque Pro
 *
 * The import process requires:
 * - Torque Pro to be installed
 * - Storage permissions for reading CSV files
 * - Successful binding to Torque service
 */
public class PidImportActivity extends AppCompatActivity implements TorqueServiceManager.TorqueConnectionListener {
    private static final String TAG = PidImportActivity.class.getSimpleName();
    private static final String[] REQUIRED_PERMISSIONS = {
//        "org.prowl.torque.permission.PLUGIN",
//        "org.prowl.torque.permission.TORQUE_PLUGIN"
    };
    private static final int PERMISSION_REQUEST_CODE = 123;

    private RecyclerView pidRecyclerView;
    private PidAdapter pidAdapter;
    private FloatingActionButton importButton;
    private ITorqueService torqueService;
    private List<PidData> pidList = new ArrayList<>();
    private TorqueServiceManager serviceManager;
    private CSVDataManager csvDataManager;
    private boolean isRequestingPermissions = false;

    private final ActivityResultLauncher<Intent> appSettingsLauncher = 
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), 
            result -> checkPermissionsAndConnect());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pid_import);

        setupViews();
        setupTorqueService();
        csvDataManager = new CSVDataManager(this);
        loadPidsFromFile(getIntent().getStringExtra("file_path"));
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        pidRecyclerView = findViewById(R.id.pidRecyclerView);
        importButton = findViewById(R.id.importButton);
        
        pidAdapter = new PidAdapter(pidList);
        pidRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pidRecyclerView.setAdapter(pidAdapter);

        importButton.setOnClickListener(v -> checkPermissionsAndImport());
        importButton.setEnabled(false);
    }

    private void setupTorqueService() {
        serviceManager = ((TorquePluginApplication) getApplication()).getTorqueServiceManager();
        serviceManager.setConnectionListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isRequestingPermissions) {
            checkPermissionsAndConnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        serviceManager.unbindFromTorqueService();
    }

    private void checkPermissionsAndConnect() {
        if (!serviceManager.isTorqueInstalled()) {
            showTorqueNotInstalledDialog();
            return;
        }

        List<String> missingPermissions = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            isRequestingPermissions = true;
            ActivityCompat.requestPermissions(this, 
                missingPermissions.toArray(new String[0]), 
                PERMISSION_REQUEST_CODE);
        } else {
            connectToTorque();
        }
    }

    private void connectToTorque() {
        if (!serviceManager.bindToTorqueService()) {
            Toast.makeText(this, R.string.error_torque_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        isRequestingPermissions = false;
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                connectToTorque();
            } else {
                showPermissionExplanationDialog();
            }
        }
    }

    private void showPermissionExplanationDialog() {
        Dialog explanationDialog = new Dialog(this);
        explanationDialog.setContentView(R.layout.dialog_permission_explanation);
        explanationDialog.setCancelable(false);

        MaterialButton settingsButton = explanationDialog.findViewById(R.id.settingsButton);
        MaterialButton cancelButton = explanationDialog.findViewById(R.id.cancelButton);

        settingsButton.setOnClickListener(v -> {
            explanationDialog.dismiss();
            openAppSettings();
        });

        cancelButton.setOnClickListener(v -> {
            explanationDialog.dismiss();
            finish();
        });

        explanationDialog.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        appSettingsLauncher.launch(intent);
    }

    /**
     * Shows a dialog when Torque Pro is not installed.
     * Provides options to:
     * 1. Install Torque Pro from Play Store
     * 2. Cancel and close the activity
     */
    private void showTorqueNotInstalledDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_torque_not_installed);
        dialog.setCancelable(false);

        MaterialButton installButton = dialog.findViewById(R.id.installButton);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancelButton);

        installButton.setOnClickListener(v -> {
            dialog.dismiss();
            openPlayStore();
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    /**
     * Opens the Play Store to Torque Pro's page.
     * Falls back to browser if Play Store app is not available.
     */
    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, 
                Uri.parse("market://details?id=org.prowl.torque")));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://play.google.com/store/apps/details?id=org.prowl.torque")));
        }
    }

    @Override
    public void onTorqueConnected() {
        torqueService = serviceManager.getTorqueService();
        importButton.setEnabled(true);
        Toast.makeText(this, R.string.torque_connected, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTorqueDisconnected() {
        torqueService = null;
        importButton.setEnabled(false);
        Toast.makeText(this, R.string.torque_disconnected, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTorqueError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onTorqueNotInstalled() {
        runOnUiThread(() -> {
            showTorqueNotInstalledDialog();
        });
    }

    /**
     * Checks necessary conditions before importing PIDs:
     * 1. Verifies if Torque Pro is installed
     * 2. Checks if service connection is active
     * If any check fails, shows appropriate error message and handles the situation
     */
    private void checkPermissionsAndImport() {
        if (!serviceManager.isTorqueInstalled()) {
            checkPermissionsAndConnect();
            return;
        }

        if (torqueService == null) {
            Toast.makeText(this, R.string.error_torque_disconnected, Toast.LENGTH_SHORT).show();
            connectToTorque();
            return;
        }

        importPids();
    }

    /**
     * Imports PIDs into Torque Pro using the connected service.
     * The method:
     * 1. Prepares arrays of PID data (names, equations, units, etc.)
     * 2. Sends data to Torque Pro via service connection
     * 3. Handles success/failure scenarios
     * 4. Provides user feedback via Toast messages
     *
     * @throws RemoteException if communication with Torque service fails
     */
    private void importPids() {
        List<PidData> selectedPids = pidAdapter.getSelectedPids();
        if (selectedPids.isEmpty()) {
            Toast.makeText(this, R.string.error_no_pids_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String[] names = new String[selectedPids.size()];
            String[] shortNames = new String[selectedPids.size()];
            String[] modeAndPIDs = new String[selectedPids.size()];
            String[] equations = new String[selectedPids.size()];
            float[] minValues = new float[selectedPids.size()];
            float[] maxValues = new float[selectedPids.size()];
            String[] units = new String[selectedPids.size()];
            String[] headers = new String[selectedPids.size()];

            for (int i = 0; i < selectedPids.size(); i++) {
                PidData pid = selectedPids.get(i);
                names[i] = pid.getName();
                shortNames[i] = pid.getShortName();
                modeAndPIDs[i] = pid.getModeAndPID();
                equations[i] = pid.getEquation();
                minValues[i] = pid.getMinValue();
                maxValues[i] = pid.getMaxValue();
                units[i] = pid.getUnit();
                headers[i] = pid.getHeader();
            }

            boolean success = torqueService.sendPIDDataV2(
                getPackageName(),
                names,
                shortNames,
                modeAndPIDs,
                equations,
                minValues,
                maxValues,
                units,
                headers,
                null,  // No start diagnostic commands
                null   // No stop diagnostic commands
            );

            if (success) {
                Toast.makeText(this, R.string.success_pids_imported, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.error_importing_pids, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            String error = getString(R.string.error_importing_pids_with_reason, e.getMessage());
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads PID data from a CSV file into memory.
     * @param filePath path to the CSV file containing PID definitions
     */
    private void loadPidsFromFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(this, "Invalid file path", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(this, "File not found: " + filePath, Toast.LENGTH_SHORT).show();
                return;
            }

            pidList.clear();
            pidList.addAll(csvDataManager.loadPIDDataFromFile(file));
            pidAdapter.notifyDataSetChanged();

        } catch (IOException e) {
            String errorMessage = getString(R.string.error_loading_pid_file, e.getMessage());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
