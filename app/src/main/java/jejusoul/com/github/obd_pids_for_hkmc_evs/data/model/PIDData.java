package jejusoul.com.github.obd_pids_for_hkmc_evs.data.model;

public class PIDData {
    private final String name;
    private final String shortName;
    private final String modeAndPID;
    private final String equation;
    private final float minValue;
    private final float maxValue;
    private final String unit;
    private final String header;

    public PIDData(String name, String shortName, String modeAndPID,
                   String equation, float minValue, float maxValue,
                   String unit, String header) {
        this.name = name;
        this.shortName = shortName;
        this.modeAndPID = modeAndPID;
        this.equation = equation;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
        this.header = header;
    }

    // Getters
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
} 