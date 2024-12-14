package projet.gestionabsence.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import projet.gestionabsence.R;

import java.util.List;

public class AbsenceReportAdapter extends RecyclerView.Adapter<AbsenceReportAdapter.ViewHolder> {

    private Context context;
    private List<AbsenceReport> reportList;

    public AbsenceReportAdapter(Context context, List<AbsenceReport> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rapport, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AbsenceReport report = reportList.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private Button downloadButton;
        private Button deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            downloadButton = itemView.findViewById(R.id.download_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(AbsenceReport report) {
            downloadButton.setOnClickListener(v -> {
                Uri uri = Uri.parse(report.getFileUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            });

            deleteButton.setOnClickListener(v -> {
            });
        }
    }
}
