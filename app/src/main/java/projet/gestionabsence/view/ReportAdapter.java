package projet.gestionabsence.view;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DownloadManager;
import androidx.recyclerview.widget.RecyclerView;
import projet.gestionabsence.R;
import projet.gestionabsence.model.AbsenceReport;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<AbsenceReport> reportsList;
    private Context context;

    public ReportAdapter(Context context, List<AbsenceReport> reportsList) {
        this.context = context;
        this.reportsList = reportsList;
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        AbsenceReport report = reportsList.get(position);
        holder.reportName.setText(report.getReportId());

        holder.icDownload.setOnClickListener(v -> {
            downloadReport(report);
        });

        holder.icRemove.setOnClickListener(v -> {
            removeReport(report, position);
        });

    }

    @Override
    public int getItemCount() {
        return reportsList.size();
    }

    public void downloadReport(AbsenceReport report) {
        String reportUrl = report.getFileUrl();

        if (reportUrl != null && !reportUrl.isEmpty()) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(reportUrl));
            request.setTitle("Téléchargement du rapport");
            request.setDescription("Téléchargement du rapport " + report.getReportId());

            request.setMimeType("application/pdf");

            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, report.getReportId() + ".pdf");

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(context, "Téléchargement en cours", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Erreur de téléchargement", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "URL du rapport invalide", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeReport(AbsenceReport report, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String reportId = report.getReportId();
        db.collection("absenceReports")
                .document(reportId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    reportsList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Rapport supprimé", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Erreur lors de la suppression du rapport", Toast.LENGTH_SHORT).show();
                });
    }

    private void viewReport(AbsenceReport report) {
        Toast.makeText(context, "Voir le rapport " + report.getReportId(), Toast.LENGTH_SHORT).show();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView reportName;
        ImageView icDownload, icRemove;

        public ReportViewHolder(View itemView) {
            super(itemView);
            reportName = itemView.findViewById(R.id.report_name);
            icDownload = itemView.findViewById(R.id.ic_download);
            icRemove = itemView.findViewById(R.id.ic_remove);
        }
    }
}
