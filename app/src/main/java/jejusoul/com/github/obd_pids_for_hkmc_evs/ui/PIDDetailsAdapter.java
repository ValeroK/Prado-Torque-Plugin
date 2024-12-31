package jejusoul.com.github.obd_pids_for_hkmc_evs.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jejusoul.com.github.obd_pids_for_hkmc_evs.R;
import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.PidData;

public class PIDDetailsAdapter extends RecyclerView.Adapter<PIDDetailsAdapter.ViewHolder> {
    private List<PidData> pidList = new ArrayList<>();
    private Set<PidData> selectedPids = new HashSet<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pid_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PidData pid = pidList.get(position);
        holder.bind(pid);
    }

    @Override
    public int getItemCount() {
        return pidList.size();
    }

    public void submitList(List<PidData> newPidList) {
        if (newPidList == null) {
            pidList.clear();
            selectedPids.clear();
        } else {
            pidList = new ArrayList<>(newPidList);
            selectedPids.clear();
            selectedPids.addAll(newPidList); // Select all by default
        }
        notifyDataSetChanged();
    }

    public Set<PidData> getSelectedPids() {
        return new HashSet<>(selectedPids);
    }

    public void toggleSelectAll() {
        if (selectedPids.size() == pidList.size()) {
            selectedPids.clear();
        } else {
            selectedPids.clear();
            selectedPids.addAll(pidList);
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkbox;
        private final TextView nameText;
        private final TextView shortNameText;
        private final TextView detailsText;

        ViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.pidCheckbox);
            nameText = itemView.findViewById(R.id.pidName);
            shortNameText = itemView.findViewById(R.id.pidShortName);
            detailsText = itemView.findViewById(R.id.pidDetails);

            View.OnClickListener clickListener = v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PidData pid = pidList.get(position);
                    if (selectedPids.contains(pid)) {
                        selectedPids.remove(pid);
                    } else {
                        selectedPids.add(pid);
                    }
                    notifyItemChanged(position);
                }
            };

            // Make the whole item clickable
            itemView.setOnClickListener(clickListener);
            checkbox.setOnClickListener(clickListener);
        }

        void bind(PidData pid) {
            checkbox.setChecked(selectedPids.contains(pid));
            nameText.setText(pid.getName());
            shortNameText.setText(pid.getShortName());
            String details = String.format("Mode: %s\nEquation: %s\nRange: %.2f to %.2f %s",
                    pid.getModeAndPID(),
                    pid.getEquation(),
                    pid.getMinValue(),
                    pid.getMaxValue(),
                    pid.getUnit());
            detailsText.setText(details);
        }
    }
}
