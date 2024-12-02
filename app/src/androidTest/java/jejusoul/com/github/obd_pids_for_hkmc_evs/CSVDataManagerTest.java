package jejusoul.com.github.obd_pids_for_hkmc_evs;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

import java.util.List;

import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;

import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.CSVDataManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.data.model.PIDData;

@RunWith(AndroidJUnit4.class)
public class CSVDataManagerTest {
    private CSVDataManager dataManager;
    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dataManager = new CSVDataManager(context);
    }

    @Test
    public void testLoadSoulEvData() throws IOException, CsvValidationException {
        List<PIDData> data = dataManager.loadPIDData("SoulEV/Kia_Soul_EV_Battery_Cell_data.csv");
        assertNotNull("PID data should not be null", data);
        assertFalse("PID data should not be empty", data.isEmpty());
    }
} 