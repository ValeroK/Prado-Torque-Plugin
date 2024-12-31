package jejusoul.com.github.obd_pids_for_hkmc_evs.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import jejusoul.com.github.obd_pids_for_hkmc_evs.R;

public class PIDFileAdapter extends ListAdapter<File, PIDFileAdapter.ViewHolder> {
    private OnFileSelectedListener listener;

    public interface OnFileSelectedListener {
        void onFileSelected(File file);
    }

    public PIDFileAdapter() {
        super(new FileDiffCallback());
    }

    public void setOnFileSelectedListener(OnFileSelectedListener listener) {
        this.listener = listener;
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
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.file_name);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFileSelected(getItem(position));
                }
            });
        }

        void bind(File file) {
            textView.setText(file.getName());
        }
    }

    private static class FileDiffCallback extends DiffUtil.ItemCallback<File> {
        @Override
        public boolean areItemsTheSame(@NonNull File oldItem, @NonNull File newItem) {
            return oldItem.getAbsolutePath().equals(newItem.getAbsolutePath());
        }

        @Override
        public boolean areContentsTheSame(@NonNull File oldItem, @NonNull File newItem) {
            return oldItem.lastModified() == newItem.lastModified() &&
                   oldItem.length() == newItem.length();
        }
    }
}
