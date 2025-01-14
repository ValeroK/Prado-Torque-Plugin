package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

import android.content.Context;
import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CSVDataManager handles the loading and parsing of PID data from CSV files.
 * 
 * This utility class is responsible for:
 * - Reading CSV files containing PID definitions
 * - Parsing CSV data into PidData objects
 * - Validating PID data format and content
 * - Managing file operations related to PID CSV files
 * 
 * The class supports both local file reading and handling downloaded CSV files.
 * It implements error handling for various file operations and data validation
 * scenarios.
 * 
 * CSV File Format Expected:
 * - Header row with column names
 * - Columns: Name, ShortName, ModeAndPID, Equation, MinValue, MaxValue, Unit, Header
 * - All numeric values (MinValue, MaxValue) must be valid floating-point numbers
 * 
 * Key Features:
 * - Robust error handling for file operations
 * - Data validation during parsing
 * - Support for different CSV formats
 * - Context-aware file management
 * 
 * Usage Example:
 * CSVDataManager manager = new CSVDataManager(context);
 * List<PidData> pids = manager.loadPidsFromFile(filePath);
 * 
 * @see PidData
 */
public class CSVDataManager {
    private static final String TAG = "CSVDataManager";
    private static final String PID_FILES_DIR = "pid_files";
    private final Context context;
    private final File pidFilesDirectory;

    public CSVDataManager(Context context) {
        this.context = context;
        this.pidFilesDirectory = new File(context.getExternalFilesDir(null), PID_FILES_DIR);
        if (!pidFilesDirectory.exists()) {
            if (!pidFilesDirectory.mkdirs()) {
                Log.e(TAG, "Failed to create PID files directory");
            }
        }
    }

    /**
     * Load PID data from a file in the PID files directory
     * @param fileName Name of the file to load
     * @return List of PID data
     * @throws IOException If there's an error reading the file
     */
    public List<PidData> loadPIDData(String fileName) throws IOException {
        List<PidData> pidList = new ArrayList<>();
        File file = new File(pidFilesDirectory, fileName);
        
        if (!file.exists()) {
            Log.e(TAG, "File does not exist: " + fileName);
            return pidList;
        }

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                PidData pid = parsePIDLine(line);
                if (pid != null) {
                    pidList.add(pid);
                }
            }
        } catch (CsvValidationException e) {
            Log.e(TAG, "Error validating CSV: " + fileName, e);
            throw new IOException("Error validating CSV file", e);
        }

        return pidList;
    }

    /**
     * Load PID data from a specific file
     * @param file File to load
     * @return List of PID data
     * @throws IOException If there's an error reading the file
     */
    public List<PidData> loadPIDDataFromFile(File file) throws IOException {
        List<PidData> pidList = new ArrayList<>();
        
        if (!file.exists()) {
            Log.e(TAG, "File does not exist: " + file.getAbsolutePath());
            return pidList;
        }

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                PidData pid = parsePIDLine(line);
                if (pid != null) {
                    pidList.add(pid);
                }
            }
        } catch (CsvValidationException e) {
            Log.e(TAG, "Error validating CSV: " + file.getName(), e);
            throw new IOException("Error validating CSV file", e);
        }

        return pidList;
    }

    /**
     * Parse a line from the CSV file into a PidData object
     * @param line Array of strings from CSV line
     * @return PidData object or null if parsing fails
     */
    private PidData parsePIDLine(String[] line) {
        if (line == null || line.length < 8) {
            Log.w(TAG, "Invalid CSV line: " + Arrays.toString(line));
            return null;
        }

        try {
            // Trim values and validate
            String name = line[0].trim();
            String shortName = line[1].trim();
            String modeAndPID = line[2].trim();
            String equation = line[3].trim();
            float minValue = Float.parseFloat(line[4].trim());
            float maxValue = Float.parseFloat(line[5].trim());
            String unit = line[6].trim();
            String header = line[7].trim();

            // Basic validation
            if (name.isEmpty() || shortName.isEmpty() || modeAndPID.isEmpty()) {
                Log.w(TAG, "Required fields are empty in CSV line: " + Arrays.toString(line));
                return null;
            }

            return new PidData(name, shortName, modeAndPID, equation, minValue, maxValue, unit, header);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing numeric values in CSV line: " + Arrays.toString(line), e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error parsing CSV line: " + Arrays.toString(line), e);
            return null;
        }
    }

    /**
     * Get list of PID files in the PID files directory
     * @return List of CSV files
     */
    public List<File> getPidFiles() {
        List<File> pidFiles = new ArrayList<>();
        if (!pidFilesDirectory.exists() || !pidFilesDirectory.isDirectory()) {
            Log.w(TAG, "PID files directory does not exist or is not a directory");
            return pidFiles;
        }

        File[] files = pidFilesDirectory.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".csv"));
        
        if (files != null) {
            pidFiles.addAll(Arrays.asList(files));
        } else {
            Log.w(TAG, "Failed to list files in PID directory");
        }

        return pidFiles;
    }

    /**
     * Get the PID files directory
     * @return File object representing the PID files directory
     */
    public File getPidFilesDirectory() {
        return pidFilesDirectory;
    }

    /**
     * Clear all PID files from the directory
     * @return true if all files were deleted successfully
     */
    public boolean clearPidFiles() {
        if (!pidFilesDirectory.exists() || !pidFilesDirectory.isDirectory()) {
            return false;
        }

        boolean allDeleted = true;
        File[] files = pidFilesDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    Log.e(TAG, "Failed to delete file: " + file.getName());
                    allDeleted = false;
                }
            }
        }
        return allDeleted;
    }
}