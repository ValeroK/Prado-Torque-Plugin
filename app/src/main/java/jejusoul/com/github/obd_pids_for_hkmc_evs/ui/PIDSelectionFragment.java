package jejusoul.com.github.obd_pids_for_hkmc_evs.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jejusoul.com.github.obd_pids_for_hkmc_evs.R;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.CSVDataManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PermissionManager;

public class PIDSelectionFragment extends Fragment {
    private PIDFileAdapter adapter;
    private CSVDataManager csvDataManager;
    private PermissionManager permissionManager;
    private Button updateButton;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        csvDataManager = new CSVDataManager(requireContext());
        adapter = new PIDFileAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pid_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        permissionManager = new PermissionManager(requireActivity(), new PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                loadPidFiles();
            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                Toast.makeText(requireContext(),
                        getString(R.string.permissions_required),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Initialize views
        RecyclerView recyclerView = view.findViewById(R.id.pid_list);
        updateButton = view.findViewById(R.id.update_button);
        progressBar = view.findViewById(R.id.progress_bar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter.setOnFileSelectedListener(file -> {
            PIDSelectionFragmentDirections.ActionPidSelectionToPidDetails action =
                    PIDSelectionFragmentDirections.actionPidSelectionToPidDetails(file.getAbsolutePath());
            Navigation.findNavController(view).navigate(action);
        });
        recyclerView.setAdapter(adapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshPidFiles);

        // Setup update button
        updateButton.setOnClickListener(v -> refreshPidFiles());

        // Check storage permissions first
        if (!permissionManager.areStoragePermissionsGranted()) {
            permissionManager.checkAndRequestStoragePermissions();
        } else {
            loadPidFiles();
        }
    }

    private void refreshPidFiles() {
        progressBar.setVisibility(View.VISIBLE);
        updateButton.setEnabled(false);

        executorService.execute(() -> {
            try {
                // First try to download new files
                csvDataManager.downloadPidFiles();
                mainHandler.post(() -> {
                    // Show available files
                    List<File> pidFiles = csvDataManager.getPidFiles();
                    adapter.setFiles(pidFiles);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    updateButton.setEnabled(true);

                    if (pidFiles.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "No PID files found. Pull to refresh to download.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(),
                            "Failed to download new PID files: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    updateButton.setEnabled(true);
                });
            }
        });
    }

    private void loadPidFiles() {
        List<File> pidFiles = csvDataManager.getPidFiles();
        adapter.setFiles(pidFiles);

        if (pidFiles.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No PID files found. Pull to refresh to download.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
