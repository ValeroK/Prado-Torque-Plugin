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
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
    }

    public LiveData<DownloadStatus> getDownloadStatus() {
        return downloadStatus;
    }

    public LiveData<List<File>> getCsvFiles() {
        return csvFiles;
    }

    private void clearDownloadDirectory() {
        File[] files = downloadDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
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
        
        try (ZipInputStream zis = new ZipInputStream(new java.io.FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(CSV_EXTENSION)) {
                    File csvFile = new File(downloadDir, new File(entry.getName()).getName());
                    try (FileOutputStream fos = new FileOutputStream(csvFile)) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    csvFiles.add(csvFile);
                }
            }
        }
        
        return csvFiles;
    }

    private void updateStatus(DownloadState state, int progress, String message) {
        downloadStatus.postValue(new DownloadStatus(state, progress, message));
    }

    public void cleanup() {
        executorService.shutdown();
    }
}
