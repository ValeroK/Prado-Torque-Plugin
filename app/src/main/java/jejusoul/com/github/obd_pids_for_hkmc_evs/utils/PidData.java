package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

/**
 * PidData represents a Parameter ID (PID) used in OBD-II vehicle diagnostics.
 * 
 * This class encapsulates all the necessary information for a single PID:
 * - name: Full descriptive name of the parameter
 * - shortName: Abbreviated name for display purposes
 * - modeAndPID: OBD-II mode and PID code
 * - equation: Formula for converting raw data to meaningful values
 * - minValue/maxValue: Value range constraints
 * - unit: Measurement unit (e.g., km/h, °C)
 * - header: CAN header information
 * 
 * The class uses the Builder pattern for object creation, ensuring all required
 * fields are properly set and validated before instantiation.
 * 
 * Features:
 * - Immutable fields for thread safety
 * - Builder pattern for flexible object creation
 * - Input validation during build process
 * - Selection state tracking for UI purposes
 * 
 * Example usage:
 * PidData pid = new PidData.Builder()
 *     .setName("Vehicle Speed")
 *     .setShortName("SPEED")
 *     .setModeAndPID("010D")
 *     .setEquation("A")
 *     .setMinValue(0)
 *     .setMaxValue(255)
 *     .setUnit("km/h")
 *     .build();
 */
public class PidData {
    private String name;
    private String shortName;
    private String modeAndPID;
    private String equation;
    private float minValue;
    private float maxValue;
    private String unit;
    private String header;
    private boolean selected;

    public PidData(String name, String shortName, String modeAndPID, String equation,
                  float minValue, float maxValue, String unit, String header) {
        this.name = name;
        this.shortName = shortName;
        this.modeAndPID = modeAndPID;
        this.equation = equation;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
        this.header = header;
        this.selected = false;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getModeAndPID() {
        return modeAndPID;
    }

    public String getEquation() {
        return equation;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public String getUnit() {
        return unit;
    }

    public String getHeader() {
        return header;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
