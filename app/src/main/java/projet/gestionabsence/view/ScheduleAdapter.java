package projet.gestionabsence.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import projet.gestionabsence.R;
import projet.gestionabsence.model.ScheduleItem;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<ScheduleItem> scheduleList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(ScheduleItem item);
        void onDownloadClick(ScheduleItem item);
    }

    public ScheduleAdapter(List<ScheduleItem> scheduleList, OnItemClickListener listener) {
        this.scheduleList = scheduleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleItem scheduleItem = scheduleList.get(position);
        holder.nameTextView.setText(scheduleItem.getName());
        holder.contentTextView.setText(scheduleItem.getContent());

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(scheduleItem);
            }
        });

        holder.downloadButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDownloadClick(scheduleItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView contentTextView;
        ImageView deleteButton;
        ImageView downloadButton;

        public ScheduleViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            contentTextView = itemView.findViewById(R.id.detailsTextView);
            deleteButton = itemView.findViewById(R.id.removeIcon);
            downloadButton = itemView.findViewById(R.id.downloadIcon);
        }
    }
}
