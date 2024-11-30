package jejusoul.com.github.obd_pids_for_hkmc_evs;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.prowl.torque.remote.ITorqueService;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import java.util.List;


public class PluginActivity extends AppCompatActivity {

    private ITorqueService torqueService;
    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            handleSoulEvClick();
        }
    };
    private final View.OnClickListener updateClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            new DownloadFilesTask().execute();
        }
    };
    private MainViewModel viewModel;

    private void handleSoulEvClick() {
        try {
            if (torqueService == null || !torqueService.hasFullPermissions()) {
                showPermissionError();
                return;
            }

            List<String> name = new ArrayList<>();
            List<String> shortName = new ArrayList<>();
            List<String> modeAndPID = new ArrayList<>();
            List<String> equation = new ArrayList<>();
            List<Float> minValue = new ArrayList<>();
            List<Float> maxValue = new ArrayList<>();
            List<String> unit = new ArrayList<>();
            List<String> header = new ArrayList<>();

            String[] nextLine;
            for (String filename : getAssets().list("Soul EV")) {
                CSVReader reader = new CSVReader(new BufferedReader(
                        new InputStreamReader(getAssets().open("Soul EV/" + filename),
                                Charset.forName("UTF-8"))));

                while ((nextLine = reader.readNext()) != null) {
                    name.add(nextLine[0]);
                    shortName.add(nextLine[1]);
                    modeAndPID.add(nextLine[2]);
                    equation.add(nextLine[3]);
                    minValue.add(Float.parseFloat(nextLine[4]));
                    maxValue.add(Float.parseFloat(nextLine[5]));
                    unit.add(nextLine[6]);
                    header.add(nextLine[7]);
                }
            }

            float[] minvalueArray = new float[minValue.size()];
            float[] maxvalueArray = new float[maxValue.size()];
            for (int i = 0; i < minValue.size(); i++) {
                minvalueArray[i] = minValue.get(i);
                maxvalueArray[i] = maxValue.get(i);
            }

            boolean success = torqueService.sendPIDDataV2(getPackageName(),
                    name.toArray(new String[0]),
                    shortName.toArray(new String[0]),
                    modeAndPID.toArray(new String[0]),
                    equation.toArray(new String[0]),
                    minvalueArray,
                    maxvalueArray,
                    unit.toArray(new String[0]),
                    header.toArray(new String[0]),
                    null,
                    null
            );

            if (!success) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.error_pid_data),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.success_pid_data),
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            handleError(ex);
        }
    }

    private void showPermissionError() {
        Toast.makeText(getApplicationContext(),
                getString(R.string.error_permissions),
                Toast.LENGTH_SHORT).show();
    }

    private void handleError(Exception ex) {
        Log.e("PluginActivity", "Error: ", ex);
        Toast.makeText(getApplicationContext(),
                getString(R.string.error_generic),
                Toast.LENGTH_SHORT).show();
    }

    private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = URI.create("https://github.com/JejuSoul/OBD-PIDs-for-HKMC-EVs/archive/master.zip").toURL();
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                int len = 1024*1024; // Buffer Size
                byte[] tmpByte = new byte[len];

                InputStream is = conn.getInputStream();

                String filepath = getExternalFilesDir(null).toString() + "/master.zip";
                File file = new File(filepath);
                FileOutputStream fos = new FileOutputStream(file);

                while(true)
                {
                    int read = is.read(tmpByte);
                    if(read<=0)
                    {
                        break;
                    }
                    else
                    {
                        fos.write(tmpByte, 0, read);
                    }
                }

                is.close();
                fos.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }
    }

    /**
     * Bits of service code. You usually won't need to change this.
     */
    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            torqueService = ITorqueService.Stub.asInterface(service);

            try {
                if (torqueService.getVersion() < 19) {
                    return;
                }
            } catch (RemoteException e) {

            }

        }

        ;

        public void onServiceDisconnected(ComponentName name) {
            torqueService = null;
        }

        ;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        findViewById(R.id.soul_since_2015).setOnClickListener(mClickListener);
        findViewById(R.id.update).setOnClickListener(updateClickListener);

        Intent intent = new Intent();
        intent.setClassName("org.prowl.torque", "org.prowl.torque.remote.TorqueService");
        boolean successfulBind = bindService(intent, connection, 0);

        if (!successfulBind) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.error_bind_service),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
