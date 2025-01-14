package jejusoul.com.github.obd_pids_for_hkmc_evs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.stream.Collectors;

import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PidData;

/**
 * PidAdapter is a RecyclerView adapter for displaying and managing PID items in the UI.
 * 
 * This adapter is responsible for:
 * - Displaying PID information in a list format
 * - Managing PID selection state
 * - Handling user interactions with PID items
 * - Providing access to selected PIDs
 * 
 * Features:
 * - Efficient view recycling
 * - Checkbox selection for each PID
 * - Display of PID details (name, shortName, etc.)
 * - Selection state persistence
 * 
 * UI Components per Item:
 * - Checkbox for selection
 * - PID name and description
 * - Additional PID details (optional)
 * 
 * Key Methods:
 * - getSelectedPids(): Returns list of selected PIDs
 * - onBindViewHolder(): Binds PID data to views
 * 
 * Usage Example:
 * PidAdapter adapter = new PidAdapter(pidList);
 * recyclerView.setAdapter(adapter);
 * 
 * @see PidData
 * @see RecyclerView.Adapter
 */
public class PidAdapter extends RecyclerView.Adapter<PidAdapter.PidViewHolder> {
    private List<PidData> pidList;

    public PidAdapter(List<PidData> pidList) {
        this.pidList = pidList;
    }

    @NonNull
    @Override
    public PidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pid, parent, false);
        return new PidViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PidViewHolder holder, int position) {
        PidData pid = pidList.get(position);
        holder.bind(pid);
    }

    @Override
    public int getItemCount() {
        return pidList.size();
    }

    public List<PidData> getSelectedPids() {
        return pidList.stream()
                .filter(PidData::isSelected)
                .collect(Collectors.toList());
    }

    static class PidViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;
        private final TextView nameText;
        private final TextView descriptionText;

        public PidViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.pid_checkbox);
            nameText = itemView.findViewById(R.id.pid_name);
            descriptionText = itemView.findViewById(R.id.pid_description);
        }

        public void bind(PidData pid) {
            checkBox.setChecked(pid.isSelected());
            nameText.setText(pid.getName());
            
            // Create a detailed description string
            String description = String.format("Short Name: %s\nMode/PID: %s\nEquation: %s\n" +
                    "Min: %f, Max: %f\nUnit: %s\nHeader: %s",
                    pid.getShortName(),
                    pid.getModeAndPID(),
                    pid.getEquation(),
                    pid.getMinValue(),
                    pid.getMaxValue(),
                    pid.getUnit(),
                    pid.getHeader());
            descriptionText.setText(description);

            itemView.setOnClickListener(v -> {
                pid.setSelected(!pid.isSelected());
                checkBox.setChecked(pid.isSelected());
            });

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> 
                pid.setSelected(isChecked));
        }
    }
}
