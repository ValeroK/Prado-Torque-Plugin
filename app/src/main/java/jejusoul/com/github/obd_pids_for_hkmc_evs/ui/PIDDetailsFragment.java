package jejusoul.com.github.obd_pids_for_hkmc_evs.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jejusoul.com.github.obd_pids_for_hkmc_evs.MainViewModel;
import jejusoul.com.github.obd_pids_for_hkmc_evs.R;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PidData;
import jejusoul.com.github.obd_pids_for_hkmc_evs.TorquePluginApplication;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.CSVDataManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.TorqueServiceManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.TorquePluginApplication;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PermissionManager;

public class PIDDetailsFragment extends Fragment implements PermissionManager.PermissionCallback {
    private static final String ARG_CSV_FILE_PATH = "csv_file_path";
    private static final String TAG = "PIDDetailsFragment";
    
    private PIDDetailsAdapter adapter;
    private CSVDataManager csvDataManager;
    private TorqueServiceManager torqueServiceManager;
    private PermissionManager permissionManager;
    private MaterialButton importButton;
    private MaterialButton selectAllButton;
    private MainViewModel viewModel;

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
        torqueServiceManager = ((TorquePluginApplication) requireActivity().getApplication()).getTorqueServiceManager();
        permissionManager = new PermissionManager(requireActivity(), this);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        // Observe Torque service connection
        viewModel.getTorqueService().observe(this, service -> {
            importButton.setEnabled(service != null);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!torqueServiceManager.bindToTorqueService()) {
            Toast.makeText(requireContext(), R.string.error_torque_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        torqueServiceManager.unbindFromTorqueService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pid_details, container, false);

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.pidDetailsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        
        // Add divider between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        
        // Set up adapter
        adapter = new PIDDetailsAdapter();
        recyclerView.setAdapter(adapter);

        // Set up buttons
        importButton = view.findViewById(R.id.importButton);
        importButton.setOnClickListener(v -> importSelectedPids());

        selectAllButton = view.findViewById(R.id.selectAllButton);
        selectAllButton.setOnClickListener(v -> toggleSelectAll());

        // Load data if available
        String csvFilePath = getArguments() != null ? getArguments().getString(ARG_CSV_FILE_PATH) : null;
        if (csvFilePath != null) {
            loadPIDData(csvFilePath);
        }

        return view;
    }

    private void toggleSelectAll() {
        if (adapter != null) {
            adapter.toggleSelectAll();
        }
    }

    private void loadPIDData(String csvFilePath) {
        try {
            List<PidData> pidDataList = csvDataManager.loadPIDDataFromFile(new File(csvFilePath));
            adapter.submitList(pidDataList);
        } catch (IOException e) {
            if (isAdded()) {
                Toast.makeText(requireContext(), 
                    getString(R.string.error_loading_pid_file, e.getMessage()),
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void importSelectedPids() {
        if (!torqueServiceManager.isTorqueInstalled()) {
            Toast.makeText(requireContext(), R.string.torque_not_installed, Toast.LENGTH_LONG).show();
            return;
        }

        Set<PidData> selectedPids = adapter.getSelectedPids();
        if (selectedPids.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_pids_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            boolean success = torqueServiceManager.importPids(new ArrayList<>(selectedPids));
            if (success) {
                Toast.makeText(requireContext(), R.string.pids_imported, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.error_importing_pids, Toast.LENGTH_LONG).show();
            }
        } catch (android.os.RemoteException e) {
            String error = "Error importing PIDs: " + e.getMessage();
            android.util.Log.e(TAG, error, e);
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPermissionsGranted() {
        // Not needed for this fragment
    }

    @Override
    public void onPermissionsDenied(List<String> deniedPermissions) {
        // Not needed for this fragment
    }
}
