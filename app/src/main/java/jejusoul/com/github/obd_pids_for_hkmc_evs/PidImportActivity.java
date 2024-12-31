package jejusoul.com.github.obd_pids_for_hkmc_evs;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.prowl.torque.remote.ITorqueService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.CSVDataManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.TorquePluginApplication;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.TorqueServiceManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PidData;

/**
 * PidImportActivity is the main activity responsible for importing PID (Parameter ID) data into Torque Pro.
 * 
 * This activity provides the following key functionalities:
 * - Displays a list of available PIDs from a CSV file using a RecyclerView
 * - Manages connection with the Torque Pro service
 * - Handles the import process of selected PIDs into Torque Pro
 * - Manages permissions and user interactions for Torque Pro integration
 * 
 * The activity implements TorqueConnectionListener to handle Torque Pro service connection states:
 * - Connected: Enables PID import functionality
 * - Disconnected: Disables import and shows appropriate messages
 * - Error: Handles connection errors and user feedback
 * 
 * Key Components:
 * - RecyclerView with PidAdapter for displaying PID list
 * - FloatingActionButton for initiating PID import
 * - Permission dialog for handling Torque Pro permissions
 * - TorqueServiceManager for managing Torque Pro service connection
 * - CSVDataManager for handling PID data loading from CSV files
 * 
 * @see TorqueServiceManager.TorqueConnectionListener
 * @see PidAdapter
 * @see CSVDataManager
 */
public class PidImportActivity extends AppCompatActivity implements TorqueServiceManager.TorqueConnectionListener {
    private RecyclerView pidRecyclerView;
    private PidAdapter pidAdapter;
    private FloatingActionButton importButton;
    private ITorqueService torqueService;
    private List<PidData> pidList = new ArrayList<>();
    private Dialog permissionDialog;
    private TorqueServiceManager serviceManager;
    private CSVDataManager csvDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pid_import);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize managers and views
        csvDataManager = new CSVDataManager(this);
        pidRecyclerView = findViewById(R.id.pidRecyclerView);
        importButton = findViewById(R.id.importButton);

        // Setup RecyclerView
        pidAdapter = new PidAdapter(pidList);
        pidRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pidRecyclerView.setAdapter(pidAdapter);

        // Setup Torque service
        serviceManager = ((TorquePluginApplication) getApplication()).getTorqueServiceManager();
        serviceManager.setConnectionListener(this);

        // Setup import button
        importButton.setOnClickListener(v -> checkPermissionsAndImport());

        // Load PIDs from the file
        loadPidsFromFile(getIntent().getStringExtra("file_path"));

        // Create permission dialog
        createPermissionDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        importButton.setEnabled(false);  // Disable by default until connection is confirmed
        if (!serviceManager.bindToTorqueService()) {
            Toast.makeText(this, R.string.error_torque_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        serviceManager.unbindFromTorqueService();
    }

    @Override
    public void onTorqueConnected() {
        torqueService = serviceManager.getTorqueService();
        importButton.setEnabled(true);
    }

    @Override
    public void onTorqueDisconnected() {
        torqueService = null;
        importButton.setEnabled(false);
    }

    @Override
    public void onTorqueError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        importButton.setEnabled(false);
    }

    private void createPermissionDialog() {
        permissionDialog = new Dialog(this);
        permissionDialog.setContentView(R.layout.dialog_permission_request);
        permissionDialog.setCancelable(true);

        MaterialButton cancelButton = permissionDialog.findViewById(R.id.cancelButton);
        MaterialButton openTorqueButton = permissionDialog.findViewById(R.id.openTorqueButton);

        cancelButton.setOnClickListener(v -> permissionDialog.dismiss());
        openTorqueButton.setOnClickListener(v -> {
            // Open Torque Pro app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=org.prowl.torque"));
            startActivity(intent);
            permissionDialog.dismiss();
        });
    }

    private void checkPermissionsAndImport() {
        if (torqueService == null) {
            Toast.makeText(this, R.string.error_torque_disconnected, Toast.LENGTH_SHORT).show();
            if (!serviceManager.bindToTorqueService()) {
                Toast.makeText(this, R.string.error_torque_connection, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (!serviceManager.checkFullPermissions()) {
            permissionDialog.show();
            return;
        }

        importPids();
    }

    private void importPids() {
        try {
            List<PidData> selectedPids = pidAdapter.getSelectedPids();
            if (selectedPids.isEmpty()) {
                Toast.makeText(this, R.string.error_no_pids_selected, Toast.LENGTH_SHORT).show();
                return;
            }

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

    private void loadPidsFromFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(this, R.string.error_invalid_file, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(this, R.string.error_file_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Clear existing list and load PIDs using CSVDataManager
            pidList.clear();
            pidList.addAll(csvDataManager.loadPIDDataFromFile(file));
            pidAdapter.notifyDataSetChanged();

            if (pidList.isEmpty()) {
                Toast.makeText(this, R.string.error_no_valid_pids, Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (IOException e) {
            android.util.Log.e("PidImportActivity", "Error reading PID file", e);
            Toast.makeText(this, R.string.error_reading_file, Toast.LENGTH_SHORT).show();
            finish();
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
