package projet.gestionabsence.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import projet.gestionabsence.R;
import projet.gestionabsence.model.AbsenceReport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Assurez-vous d'importer cette classe

public class RapportFragment extends Fragment {

    private EditText editProfesseur, editDate;
    private RecyclerView recyclerViewRapports;
    private List<AbsenceReport> reportsList = new ArrayList<>();
    private ReportAdapter reportAdapter;
    private FirebaseFirestore db;
    private Cloudinary cloudinary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_rapport, container, false);

        editProfesseur = view.findViewById(R.id.edit_professeur);
        editDate = view.findViewById(R.id.edit_date);
        recyclerViewRapports = view.findViewById(R.id.recycler_view_rapports);

        FloatingActionButton btnGenerateReport = view.findViewById(R.id.btn_generate_report);

        db = FirebaseFirestore.getInstance();
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dchi2qhfi",
                "api_key", "496294249286756",
                "api_secret", "89s7ADH0n2FtK4U15fv7XtzbfiA"
        ));

        reportAdapter = new ReportAdapter(getContext(), reportsList);  // Passer `getContext()` ici
        recyclerViewRapports.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRapports.setAdapter(reportAdapter);

        loadReports();

        btnGenerateReport.setOnClickListener(v -> generateReport());

        return view;
    }

    private void loadReports() {
        db.collection("absenceReports")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<AbsenceReport> reports = task.getResult().toObjects(AbsenceReport.class);
                        reportsList.clear();
                        reportsList.addAll(reports);
                        reportAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Erreur lors de la récupération des rapports", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generateReport() {
        String professeur = editProfesseur.getText().toString().trim();
        String date = editDate.getText().toString().trim();

        Query query = db.collection("absence");
        if (!professeur.isEmpty()) {
            query = query.whereEqualTo("teacherName", professeur);
        }
        if (!date.isEmpty()) {
            query = query.whereEqualTo("date", date);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<AbsenceReport> absences = task.getResult().toObjects(AbsenceReport.class);
                generatePdfAndUpload(absences);
            } else {
                Toast.makeText(getContext(), "Erreur lors de la récupération des absences", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generatePdfAndUpload(List<AbsenceReport> absences) {
    }

    private void saveReportToDatabase(String reportId, String url) {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("reportId", reportId);
        reportData.put("url", url);
        reportData.put("creationDate", new Date());

        db.collection("absenceReports")
                .add(reportData)
                .addOnSuccessListener(documentReference -> {
                    AbsenceReport report = new AbsenceReport(reportId, url);
                    reportsList.add(report);
                    reportAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors de la sauvegarde du rapport", Toast.LENGTH_SHORT).show());
    }
}
