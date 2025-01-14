package jejusoul.com.github.obd_pids_for_hkmc_evs;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.prowl.torque.remote.ITorqueService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.CSVDataManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.GitHubDownloadManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PidData;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MainViewModel";
    private final MutableLiveData<List<PidData>> pidData = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<ITorqueService> torqueService = new MutableLiveData<>();
    private final CSVDataManager csvManager;
    private final GitHubDownloadManager downloadManager;

    public MainViewModel(Application application) {
        super(application);
        csvManager = new CSVDataManager(application);
        downloadManager = new GitHubDownloadManager(application);
        
        // Observe download status to manage files
        downloadManager.getDownloadStatus().observeForever(status -> {
            if (status.state == GitHubDownloadManager.DownloadState.COMPLETED) {
                handleDownloadComplete();
            }
        });
    }

    public void setTorqueService(ITorqueService service) {
        torqueService.setValue(service);
    }

    public LiveData<ITorqueService> getTorqueService() {
        return torqueService;
    }

    public LiveData<GitHubDownloadManager.DownloadStatus> getDownloadStatus() {
        return downloadManager.getDownloadStatus();
    }

    public LiveData<List<File>> getCsvFiles() {
        return downloadManager.getCsvFiles();
    }

    /**
     * Start downloading PID files
     */
    public void downloadPidFiles() {
        // Clear existing files before download
        csvManager.clearPidFiles();
        
        try {
            downloadManager.downloadAndExtract();
        } catch (Exception e) {
            Log.e(TAG, "Error starting download", e);
            error.setValue("Failed to start download: " + e.getMessage());
        }
    }

    private void handleDownloadComplete() {
        try {
            // Move files from download directory to PID files directory
            List<File> downloadedFiles = downloadManager.getCsvFiles().getValue();
            if (downloadedFiles != null) {
                File pidFilesDir = csvManager.getPidFilesDirectory();
                for (File sourceFile : downloadedFiles) {
                    try {
                        // Get just the filename without the path
                        String fileName = sourceFile.getName();
                        File destFile = new File(pidFilesDir, fileName);

                        // Ensure the destination file doesn't exist
                        if (destFile.exists() && !destFile.delete()) {
                            Log.w(TAG, "Failed to delete existing file: " + destFile.getAbsolutePath());
                        }

                        // Create parent directories if they don't exist
                        if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs()) {
                            Log.w(TAG, "Failed to create parent directories for: " + destFile.getAbsolutePath());
                        }

                        // Copy the file
                        copyFile(sourceFile, destFile);
                        Log.d(TAG, "Successfully copied file to: " + destFile.getAbsolutePath());
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to copy file: " + sourceFile.getName(), e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling download completion", e);
            error.setValue("Failed to process downloaded files: " + e.getMessage());
        }
    }

    /**
     * Copy a file from source to destination using NIO FileChannel for efficiency
     */
    private void copyFile(File source, File dest) throws IOException {
        // Ensure source exists and is readable
        if (!source.exists() || !source.canRead()) {
            throw new IOException("Source file doesn't exist or is not readable: " + source.getAbsolutePath());
        }

        // Create parent directories if needed
        File parentDir = dest.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create parent directories for: " + dest.getAbsolutePath());
        }

        // Create destination file if it doesn't exist
        if (!dest.exists() && !dest.createNewFile()) {
            throw new IOException("Failed to create destination file: " + dest.getAbsolutePath());
        }

        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    public LiveData<String> getError() {
        return error;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        downloadManager.cleanup();
    }
}