package jejusoul.com.github.obd_pids_for_hkmc_evs.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.List;
import java.util.Set;

import jejusoul.com.github.obd_pids_for_hkmc_evs.R;
import jejusoul.com.github.obd_pids_for_hkmc_evs.data.model.PIDData;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.CSVDataManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.TorqueServiceManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.TorquePlugin;

public class PIDDetailsFragment extends Fragment {
    private static final String ARG_CSV_FILE_PATH = "csv_file_path";
    
    private PIDDetailsAdapter adapter;
    private CSVDataManager csvDataManager;
    private TorqueServiceManager torqueServiceManager;

    public static PIDDetailsFragment newInstance(String csvFilePath) {
        PIDDetailsFragment fragment = new PIDDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CSV_FILE_PATH, csvFilePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        csvDataManager = new CSVDataManager(requireContext());
        torqueServiceManager = ((TorquePlugin) requireActivity().getApplication()).getTorqueServiceManager();
        
        // Set up connection listener
        torqueServiceManager.setConnectionListener(new TorqueServiceManager.TorqueConnectionListener() {
            @Override
            public void onTorqueConnected() {
                // Enable import functionality when connected
                if (getView() != null) {
                    getView().findViewById(R.id.importButton).setEnabled(true);
                }
            }

            @Override
            public void onTorqueDisconnected() {
                // Disable import functionality when disconnected
                if (getView() != null) {
                    getView().findViewById(R.id.importButton).setEnabled(false);
                }
            }

            @Override
            public void onTorqueError(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pid_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.pidDetailsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PIDDetailsAdapter();
        recyclerView.setAdapter(adapter);

        MaterialButton importButton = view.findViewById(R.id.importButton);
        importButton.setOnClickListener(v -> importSelectedPids());

        MaterialButton selectAllButton = view.findViewById(R.id.selectAllButton);
        selectAllButton.setOnClickListener(v -> toggleSelectAll());

        loadPIDData();
    }

    private void toggleSelectAll() {
        if (adapter != null) {
            boolean allSelected = adapter.areAllSelected();
            adapter.selectAll(!allSelected);
        }
    }

    private void loadPIDData() {
        String csvFilePath = getArguments().getString(ARG_CSV_FILE_PATH);
        if (csvFilePath != null) {
            try {
                List<PIDData> pidList = csvDataManager.loadPIDDataFromFile(new File(csvFilePath));
                adapter.setPidList(pidList);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error loading PID data: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void importSelectedPids() {
        Set<PIDData> selectedPids = adapter.getSelectedPids();
        if (selectedPids.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_pids_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to bind to Torque service
        if (!torqueServiceManager.bindToTorqueService()) {
            return; // Error message will be shown by the connection listener
        }

        // Check permissions
        if (!torqueServiceManager.checkFullPermissions()) {
            Toast.makeText(requireContext(), R.string.permissions_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Import PIDs using Torque service
        try {
            String[] names = new String[selectedPids.size()];
            String[] shortNames = new String[selectedPids.size()];
            String[] modeAndPIDs = new String[selectedPids.size()];
            String[] equations = new String[selectedPids.size()];
            float[] minValues = new float[selectedPids.size()];
            float[] maxValues = new float[selectedPids.size()];
            String[] units = new String[selectedPids.size()];
            String[] headers = new String[selectedPids.size()];

            int i = 0;
            for (PIDData pid : selectedPids) {
                names[i] = pid.getName();
                shortNames[i] = pid.getShortName();
                modeAndPIDs[i] = pid.getModeAndPID();
                equations[i] = pid.getEquation();
                minValues[i] = pid.getMinValue();
                maxValues[i] = pid.getMaxValue();
                units[i] = pid.getUnit();
                headers[i] = pid.getHeader();
                i++;
            }

            torqueServiceManager.getTorqueService().sendPIDDataV2(
                requireContext().getPackageName(),
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
            Toast.makeText(requireContext(), R.string.pids_imported, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            String errorMessage = getString(R.string.error_importing_pids, e.getMessage());
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
