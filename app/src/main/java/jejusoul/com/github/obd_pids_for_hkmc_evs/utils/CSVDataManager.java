package jejusoul.com.github.obd_pids_for_hkmc_evs.data;

import android.content.Context;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import jejusoul.com.github.obd_pids_for_hkmc_evs.data.model.PIDData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVDataManager {
    private final Context context;

    public CSVDataManager(Context context) {
        this.context = context;
    }

    public List<PIDData> loadPIDData(String fileName) throws IOException, CsvValidationException {
        List<PIDData> pidData = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new BufferedReader(
                new InputStreamReader(context.getAssets().open(fileName))))) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                pidData.add(parsePIDLine(nextLine));
            }
        }
        return pidData;
    }

    private PIDData parsePIDLine(String[] line) {
        return new PIDData(
                line[0],  // name
                line[1],  // shortName
                line[2],  // modeAndPID
                line[3],  // equation
                Float.parseFloat(line[4]),  // minValue
                Float.parseFloat(line[5]),  // maxValue
                line[6],  // unit
                line[7]   // header
        );
    }
} 