package jejusoul.com.github.obd_pids_for_hkmc_evs.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jejusoul.com.github.obd_pids_for_hkmc_evs.R;

public class PIDFileAdapter extends RecyclerView.Adapter<PIDFileAdapter.ViewHolder> {
    private List<File> files = new ArrayList<>();
    private OnFileSelectedListener listener;

    public interface OnFileSelectedListener {
        void onFileSelected(File file);
    }

    public void setOnFileSelectedListener(OnFileSelectedListener listener) {
        this.listener = listener;
    }

    public void setFiles(List<File> newFiles) {
        files = new ArrayList<>(newFiles);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pid_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = files.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.file_name);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFileSelected(files.get(position));
                }
            });
        }

        void bind(File file) {
            textView.setText(file.getName());
        }
    }
}
