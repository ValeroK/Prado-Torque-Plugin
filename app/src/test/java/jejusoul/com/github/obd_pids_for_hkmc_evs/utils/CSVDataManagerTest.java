package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.opencsv.exceptions.CsvValidationException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jejusoul.com.github.obd_pids_for_hkmc_evs.data.model.PIDData;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class CSVDataManagerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private Context mockContext;

    @Mock
    private ConnectivityManager mockConnectivityManager;

    @Mock
    private NetworkInfo mockNetworkInfo;

    private CSVDataManager csvDataManager;
    private File pidDirectory;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        // Set up mock context and directories
        pidDirectory = temporaryFolder.newFolder("pid_files");
        when(mockContext.getExternalFilesDir(null)).thenReturn(temporaryFolder.getRoot());
        when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
        
        csvDataManager = new CSVDataManager(mockContext);
    }

    @Test
    public void loadPIDData_ValidCSV_ReturnsPIDDataList() throws IOException, CsvValidationException {
        // Create a test CSV file with Soul EV BMS data format
        String csvContent = 
            "Name,ShortName,Mode/PID,Equation,Min,Max,Unit,Header\n" +  // Header line
            "000_Auxillary Battery Voltage,Aux Batt Volts,2101,ad*0.1,11,14.6,V,7E4\n" +
            "000_Available Charge Power,Max REGEN,2101,((f<8)+g)/100,0,98,kW,7E4\n" +
            "000_Battery Current,Batt Current,2101,((Signed(K)*256)+L)/10,-230,230,A,7E4";
        
        File testFile = new File(pidDirectory, "test.csv");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(csvContent);
        }

        // Test loading the file
        List<PIDData> result = csvDataManager.loadPIDDataFromFile(testFile);

        // Verify results
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 3 PID entries", 3, result.size());

        // Verify first PID data
        PIDData firstPid = result.get(0);
        assertEquals("000_Auxillary Battery Voltage", firstPid.getName());
        assertEquals("Aux Batt Volts", firstPid.getShortName());
        assertEquals("2101", firstPid.getModeAndPID());
        assertEquals("ad*0.1", firstPid.getEquation());
        assertEquals(11.0f, firstPid.getMinValue(), 0.001f);
        assertEquals(14.6f, firstPid.getMaxValue(), 0.001f);
        assertEquals("V", firstPid.getUnit());
        assertEquals("7E4", firstPid.getHeader());
    }

    @Test
    public void loadPIDData_EmptyCSV_ReturnsEmptyList() throws IOException, CsvValidationException {
        // Create empty CSV file with just header
        File emptyFile = new File(pidDirectory, "empty.csv");
        try (FileWriter writer = new FileWriter(emptyFile)) {
            writer.write("Name,ShortName,Mode/PID,Equation,Min,Max,Unit,Header\n");
        }
        
        List<PIDData> result = csvDataManager.loadPIDDataFromFile(emptyFile);
        
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test(expected = IOException.class)
    public void loadPIDData_InvalidFile_ThrowsIOException() throws IOException, CsvValidationException {
        // Test with non-existent file
        File nonExistentFile = new File(pidDirectory, "nonexistent.csv");
        csvDataManager.loadPIDDataFromFile(nonExistentFile);
    }

    @Test
    public void loadPIDData_InvalidCSVFormat_SkipsInvalidLines() throws IOException, CsvValidationException {
        // Create CSV with some invalid lines
        String csvContent = 
            "Name,ShortName,Mode/PID,Equation,Min,Max,Unit,Header\n" +  // Header line
            "000_Auxillary Battery Voltage,Aux Batt Volts,2101,ad*0.1,11,14.6,V,7E4\n" +
            "Invalid Line Format\n" +  // Invalid line
            "000_Battery Current,Batt Current,2101,((Signed(K)*256)+L)/10,-230,230,A,7E4";
        
        File testFile = new File(pidDirectory, "test_invalid.csv");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(csvContent);
        }

        List<PIDData> result = csvDataManager.loadPIDDataFromFile(testFile);

        assertNotNull("Result should not be null", result);
        assertEquals("Should have 2 valid PID entries", 2, result.size());
    }

    @Test
    public void loadPIDData_NoHeader_ParsesFirstLineAsData() throws IOException, CsvValidationException {
        // Create CSV file without header
        String csvContent = 
            "000_Auxillary Battery Voltage,Aux Batt Volts,2101,ad*0.1,11,14.6,V,7E4\n" +
            "000_Battery Current,Batt Current,2101,((Signed(K)*256)+L)/10,-230,230,A,7E4";
        
        File testFile = new File(pidDirectory, "test_no_header.csv");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(csvContent);
        }

        List<PIDData> result = csvDataManager.loadPIDDataFromFile(testFile);

        assertNotNull("Result should not be null", result);
        assertEquals("Should parse both lines including first line as data", 2, result.size());
        
        // Verify first line was parsed as data
        PIDData firstPid = result.get(0);
        assertEquals("000_Auxillary Battery Voltage", firstPid.getName());
        assertEquals("Aux Batt Volts", firstPid.getShortName());
        assertEquals("2101", firstPid.getModeAndPID());
        assertEquals("ad*0.1", firstPid.getEquation());
        assertEquals(11.0f, firstPid.getMinValue(), 0.001f);
        assertEquals(14.6f, firstPid.getMaxValue(), 0.001f);
        assertEquals("V", firstPid.getUnit());
        assertEquals("7E4", firstPid.getHeader());
    }

    @Test
    public void getPidFiles_ReturnsOnlyCSVFiles() throws IOException {
        // Create various files
        File csvFile1 = new File(pidDirectory, "test1.csv");
        File csvFile2 = new File(pidDirectory, "test2.csv");
        File txtFile = new File(pidDirectory, "test.txt");
        
        csvFile1.createNewFile();
        csvFile2.createNewFile();
        txtFile.createNewFile();

        List<File> result = csvDataManager.getPidFiles();

        assertEquals("Should only return CSV files", 2, result.size());
        assertTrue("Should contain first CSV file", result.stream().anyMatch(f -> f.getName().equals("test1.csv")));
        assertTrue("Should contain second CSV file", result.stream().anyMatch(f -> f.getName().equals("test2.csv")));
    }

    @Test(expected = IOException.class)
    public void downloadPidFiles_NoConnectivity_ThrowsIOException() throws IOException {
        when(mockNetworkInfo.isConnectedOrConnecting()).thenReturn(false);
        csvDataManager.downloadPidFiles();
    }

    @Test
    public void downloadPidFiles_WithConnectivity_DownloadsFiles() throws IOException {
        when(mockNetworkInfo.isConnectedOrConnecting()).thenReturn(true);
        // Note: This test would need to mock HTTP connections for full testing
        // For now we just verify it attempts the download
        try {
            csvDataManager.downloadPidFiles();
        } catch (IOException e) {
            // Expected as we haven't mocked the HTTP connection
            assertTrue("Exception should be about connection", 
                e.getMessage().contains("Failed to download") || 
                e.getMessage().contains("Unable to resolve host"));
        }
        verify(mockNetworkInfo).isConnectedOrConnecting();
    }
}
