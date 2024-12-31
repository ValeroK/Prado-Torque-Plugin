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
import jejusoul.com.github.obd_pids_for_hkmc_evs.data.model.PIDData;

public class PIDDetailsAdapter extends RecyclerView.Adapter<PIDDetailsAdapter.ViewHolder> {
    private List<PIDData> pidList = new ArrayList<>();
    private Set<PIDData> selectedPids = new HashSet<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pid_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PIDData pid = pidList.get(position);
        holder.bind(pid);
    }

    @Override
    public int getItemCount() {
        return pidList.size();
    }

    public void setPidList(List<PIDData> newPidList) {
        pidList = new ArrayList<>(newPidList);
        selectedPids.clear();
        selectedPids.addAll(newPidList); // Select all by default
        notifyDataSetChanged();
    }

    public Set<PIDData> getSelectedPids() {
        return new HashSet<>(selectedPids);
    }

    public boolean areAllSelected() {
        return selectedPids.size() == pidList.size();
    }

    public void selectAll(boolean select) {
        selectedPids.clear();
        if (select) {
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

            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PIDData pid = pidList.get(position);
                    if (isChecked) {
                        selectedPids.add(pid);
                    } else {
                        selectedPids.remove(pid);
                    }
                }
            });
        }

        void bind(PIDData pid) {
            nameText.setText(itemView.getContext().getString(R.string.pid_name, pid.getName()));
            shortNameText.setText(itemView.getContext().getString(R.string.pid_short_name, pid.getShortName()));
            
            StringBuilder details = new StringBuilder();
            details.append(itemView.getContext().getString(R.string.pid_mode, pid.getModeAndPID())).append('\n')
                   .append(itemView.getContext().getString(R.string.pid_equation, pid.getEquation())).append('\n')
                   .append(itemView.getContext().getString(R.string.pid_min_value, pid.getMinValue())).append('\n')
                   .append(itemView.getContext().getString(R.string.pid_max_value, pid.getMaxValue())).append('\n')
                   .append(itemView.getContext().getString(R.string.pid_unit, pid.getUnit())).append('\n')
                   .append(itemView.getContext().getString(R.string.pid_header, pid.getHeader()));
            
            detailsText.setText(details.toString());
            checkbox.setChecked(selectedPids.contains(pid));
        }
    }
}
