package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GitHubDownloadManager {
    private static final String TAG = "GitHubDownloadManager";
    private static final String GITHUB_REPO_URL = "https://github.com/JejuSoul/OBD-PIDs-for-HKMC-EVs/archive/refs/heads/master.zip";
    private static final int BUFFER_SIZE = 4096;
    private static final String CSV_EXTENSION = ".csv";
    private static final String REPO_BASE_NAME = "OBD-PIDs-for-HKMC-EVs-master";

    private final Context context;
    private final ExecutorService executorService;
    private final MutableLiveData<DownloadStatus> downloadStatus;
    private final MutableLiveData<List<File>> csvFiles;
    private final File downloadDir;

    public enum DownloadState {
        IDLE,
        DOWNLOADING,
        EXTRACTING,
        COMPLETED,
        ERROR
    }

    public static class DownloadStatus {
        public final DownloadState state;
        public final int progress;
        public final String message;

        public DownloadStatus(DownloadState state, int progress, String message) {
            this.state = state;
            this.progress = progress;
            this.message = message;
        }
    }

    public GitHubDownloadManager(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
        this.downloadStatus = new MutableLiveData<>(new DownloadStatus(DownloadState.IDLE, 0, ""));
        this.csvFiles = new MutableLiveData<>(new ArrayList<>());
        this.downloadDir = new File(context.getFilesDir(), "downloads");
        if (!downloadDir.exists() && !downloadDir.mkdirs()) {
            Log.e(TAG, "Failed to create download directory");
        }
    }

    public LiveData<DownloadStatus> getDownloadStatus() {
        return downloadStatus;
    }

    public LiveData<List<File>> getCsvFiles() {
        return csvFiles;
    }

    private void clearDownloadDirectory() {
        deleteRecursive(downloadDir);
        if (!downloadDir.exists() && !downloadDir.mkdirs()) {
            Log.e(TAG, "Failed to recreate download directory");
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        if (!fileOrDirectory.delete() && fileOrDirectory.exists()) {
            Log.w(TAG, "Failed to delete: " + fileOrDirectory.getAbsolutePath());
        }
    }

    @NonNull
    private File downloadZipFile() throws IOException {
        File zipFile = new File(downloadDir, "master.zip");
        URL url = new URL(GITHUB_REPO_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try (InputStream input = connection.getInputStream();
             FileOutputStream output = new FileOutputStream(zipFile)) {
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } finally {
            connection.disconnect();
        }
        
        return zipFile;
    }

    @NonNull
    private List<File> extractZipFile(File zipFile) throws IOException {
        List<File> csvFiles = new ArrayList<>();
        File extractDir = new File(downloadDir, REPO_BASE_NAME);
        if (!extractDir.exists() && !extractDir.mkdirs()) {
            Log.e(TAG, "Failed to create extraction directory");
        }
        
        try (ZipInputStream zis = new ZipInputStream(new java.io.FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[BUFFER_SIZE];
            
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                // Skip directories and non-CSV files
                if (entry.isDirectory() || !entryName.toLowerCase().endsWith(CSV_EXTENSION)) {
                    continue;
                }

                // Create the full path for the output file
                File outputFile = new File(downloadDir, entryName);
                File parentDir = outputFile.getParentFile();
                
                if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory: " + parentDir.getAbsolutePath());
                    continue;
                }

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    csvFiles.add(outputFile);
                    Log.d(TAG, "Extracted: " + outputFile.getAbsolutePath());
                } catch (IOException e) {
                    Log.e(TAG, "Failed to extract file: " + entryName, e);
                }
                
                zis.closeEntry();
            }
        }
        
        return csvFiles;
    }

    private void updateStatus(DownloadState state, int progress, String message) {
        downloadStatus.postValue(new DownloadStatus(state, progress, message));
    }

    public void downloadAndExtract() {
        executorService.execute(() -> {
            File zipFile = null;
            try {
                clearDownloadDirectory();
                updateStatus(DownloadState.DOWNLOADING, 0, "Starting download...");

                zipFile = downloadZipFile();
                updateStatus(DownloadState.EXTRACTING, 50, "Extracting files...");

                List<File> extractedFiles = extractZipFile(zipFile);
                csvFiles.postValue(extractedFiles);

                updateStatus(DownloadState.COMPLETED, 100, "Download completed");
            } catch (IOException e) {
                Log.e(TAG, "Error during download/extract", e);
                updateStatus(DownloadState.ERROR, 0, "Error: " + e.getMessage());
            } finally {
                if (zipFile != null && zipFile.exists()) {
                    zipFile.delete();
                }
            }
        });
    }

    public void cleanup() {
        executorService.shutdown();
        clearDownloadDirectory();
    }
}
