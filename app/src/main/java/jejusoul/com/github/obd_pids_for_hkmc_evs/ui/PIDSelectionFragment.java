package jejusoul.com.github.obd_pids_for_hkmc_evs.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.util.List;

import jejusoul.com.github.obd_pids_for_hkmc_evs.MainViewModel;
import jejusoul.com.github.obd_pids_for_hkmc_evs.R;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.CSVDataManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.GitHubDownloadManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PermissionManager;

public class PIDSelectionFragment extends Fragment implements PermissionManager.PermissionCallback {
    private PIDFileAdapter adapter;
    private CSVDataManager csvDataManager;
    private PermissionManager permissionManager;
    private MainViewModel viewModel;
    private Button updateButton;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        csvDataManager = new CSVDataManager(requireContext());
        permissionManager = new PermissionManager(requireActivity(), this);
        adapter = new PIDFileAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pid_selection, container, false);

        // Initialize views
        RecyclerView recyclerView = view.findViewById(R.id.pid_list);
        updateButton = view.findViewById(R.id.update_button);
        progressBar = view.findViewById(R.id.progress_bar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter.setOnFileSelectedListener(file -> {
            Bundle args = new Bundle();
            args.putString("csv_file_path", file.getAbsolutePath());
            Navigation.findNavController(view).navigate(R.id.action_pidSelection_to_pidDetails, args);
        });
        recyclerView.setAdapter(adapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshPidFiles);

        // Setup update button
        updateButton.setOnClickListener(v -> refreshPidFiles());

        // Observe download status
        viewModel.getDownloadStatus().observe(getViewLifecycleOwner(), status -> {
            switch (status.state) {
                case DOWNLOADING:
                case EXTRACTING:
                    updateButton.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    emptyStateText.setVisibility(View.GONE);
                    break;
                case COMPLETED:
                    updateButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    loadPidFiles();
                    break;
                case ERROR:
                    updateButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(requireContext(), R.string.error_downloading_files,
                            Toast.LENGTH_SHORT).show();
                    showEmptyState(R.string.error_downloading_files);
                    break;
            }
        });

        // Observe CSV files
        viewModel.getCsvFiles().observe(getViewLifecycleOwner(), files -> {
            if (files != null && !files.isEmpty()) {
                adapter.submitList(files);
                emptyStateText.setVisibility(View.GONE);
            } else {
                showEmptyState(R.string.no_pid_files_found);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load initial data only if we have permissions
        if (permissionManager.areStoragePermissionsGranted()) {
            List<File> existingFiles = csvDataManager.getPidFiles();
            if (!existingFiles.isEmpty()) {
                adapter.submitList(existingFiles);
                emptyStateText.setVisibility(View.GONE);
            } else {
                showEmptyState(R.string.no_pid_files_found);
            }
        } else {
            showEmptyState(R.string.storage_permission_required);
            permissionManager.checkAndRequestStoragePermissions();
        }
    }

    private void showEmptyState(@StringRes int messageResId) {
        emptyStateText.setText(messageResId);
        emptyStateText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void refreshPidFiles() {
        if (!permissionManager.areStoragePermissionsGranted()) {
            permissionManager.checkAndRequestStoragePermissions();
            return;
        }

        updateButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        viewModel.downloadPidFiles();
    }

    private void loadPidFiles() {
        List<File> pidFiles = csvDataManager.getPidFiles();
        adapter.submitList(pidFiles);
        if (pidFiles.isEmpty()) {
            showEmptyState(R.string.no_pid_files_found);
        } else {
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPermissionsGranted() {
        loadPidFiles();
    }

    @Override
    public void onPermissionsDenied(List<String> deniedPermissions) {
        Toast.makeText(requireContext(), R.string.permissions_required,
                Toast.LENGTH_LONG).show();
        swipeRefreshLayout.setRefreshing(false);
        showEmptyState(R.string.storage_permission_required);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
