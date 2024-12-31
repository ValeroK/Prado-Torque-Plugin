package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import kotlin.coroutines.Continuation;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private final Context context;

    public DownloadManager(Context context) {
        this.context = context;
    }

    public interface ProgressCallback {
        void onProgress(int progress);
    }

    public Object downloadFile(ProgressCallback progressCallback, Continuation<? super File> continuation) {
        return BuildersKt.withContext(
            Dispatchers.getIO(),
            new Function2<CoroutineScope, Continuation<? super File>, Object>() {
                @Override
                public Object invoke(CoroutineScope scope, Continuation<? super File> continuation) {
                    HttpURLConnection connection = null;
                    try {
                        // Setup connection
                        connection = (HttpURLConnection) URI.create(
                                "https://github.com/JejuSoul/OBD-PIDs-for-HKMC-EVs/archive/master.zip")
                                .toURL()
                                .openConnection();

                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(15000);
                        connection.setReadTimeout(15000);

                        // Prepare file
                        File outputFile = new File(context.getExternalFilesDir(null), "master.zip");
                        int fileLength = connection.getContentLength();

                        // Download with progress
                        try (BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
                             FileOutputStream output = new FileOutputStream(outputFile)) {

                            byte[] buffer = new byte[8192];
                            long total = 0;
                            int count;

                            while ((count = input.read(buffer)) != -1) {
                                total += count;
                                output.write(buffer, 0, count);

                                // Update progress
                                if (fileLength > 0) {
                                    final int progress = (int) (total * 100 / fileLength);
                                    if (progressCallback != null) {
                                        progressCallback.onProgress(progress);
                                    }
                                }
                            }

                            return outputFile;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error downloading file", e);
                        throw new RuntimeException(e);
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            },
            continuation
        );
    }

    // Helper method to make it easier to call from Kotlin
    @NonNull
    public Object downloadFileWithScope(
            @NonNull CoroutineScope scope,
            @NonNull ProgressCallback progressCallback,
            @NonNull Continuation<? super File> continuation) {
        return BuildersKt.withContext(
            Dispatchers.getIO(),
            (coroutineScope, cont) -> downloadFile(progressCallback, cont),
            continuation
        );
    }
} 