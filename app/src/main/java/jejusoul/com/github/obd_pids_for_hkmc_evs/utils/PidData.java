package jejusoul.com.github.obd_pids_for_hkmc_evs.utils;

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
