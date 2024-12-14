package projet.gestionabsence.view;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import projet.gestionabsence.R;
import projet.gestionabsence.model.AbsenceReport;
import projet.gestionabsence.model.Absence;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateReportActivity extends AppCompatActivity {

    private EditText editProfesseur, editDate;
    private RecyclerView recyclerViewRapports;
    private FloatingActionButton btnGenerateReport;
    private List<AbsenceReport> reportsList = new ArrayList<>();
    private ReportAdapter reportAdapter;
    private FirebaseFirestore db;
    private Cloudinary cloudinary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rapport);

        editProfesseur = findViewById(R.id.edit_professeur);
        editDate = findViewById(R.id.edit_date);
        recyclerViewRapports = findViewById(R.id.recycler_view_rapports);
        btnGenerateReport = findViewById(R.id.btn_generate_report);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();

        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "<your_cloud_name>",
                "api_key", "<your_api_key>",
                "api_secret", "<your_api_secret>"
        ));

        reportAdapter = new ReportAdapter(this, reportsList);
        recyclerViewRapports.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRapports.setAdapter(reportAdapter);

        btnGenerateReport.setOnClickListener(v -> generateReport());
    }

    private void generateReport() {
        String professeur = editProfesseur.getText().toString();
        String date = editDate.getText().toString();

        Query query = db.collection("absences");
        if (!professeur.isEmpty()) {
            query = query.whereEqualTo("professeur", professeur);
        }
        if (!date.isEmpty()) {
            query = query.whereEqualTo("date", date);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Absence> absences = task.getResult().toObjects(Absence.class);
                generatePdfAndUpload(absences);
            } else {
                Toast.makeText(this, "Erreur lors de la récupération des absences", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generatePdfAndUpload(List<Absence> absences) {
        File pdfFile = generatePdf(absences);

        if (pdfFile != null) {
            uploadPdfToCloudinary(pdfFile);
        }
    }

    private File generatePdf(List<Absence> absences) {
        File pdfFile = new File(getFilesDir(), "rapport_absence.pdf");

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDocument = new PdfDocument(writer);
            pdfDocument.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdfDocument);

            document.add(new Paragraph("Rapport des Absences")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            for (Absence absence : absences) {
                document.add(new Paragraph("Professeur: " + (absence.getTeacherName() != null ? absence.getTeacherName() : "Non spécifié")));
                document.add(new Paragraph("Date: " + (absence.getDate() != null ? absence.getDate() : "Non spécifiée")));
                document.add(new Paragraph("Agent: " + (absence.getNameAgent() != null ? absence.getNameAgent() : "Non spécifié")));
                document.add(new Paragraph("\n"));
            }

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la génération du PDF", Toast.LENGTH_SHORT).show();
            return null;
        }

        return pdfFile;
    }

    private void uploadPdfToCloudinary(File file) {
        String reportId = "RapportAbsence" + String.format("%07d", reportsList.size() + 1);

        new Thread(() -> {
            try {
                Map<String, Object> uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap("public_id", reportId));
                String url = (String) uploadResult.get("url");
                runOnUiThread(() -> saveReportToDatabase(reportId, url));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Erreur lors du téléchargement", Toast.LENGTH_SHORT).show());
            }
        }).start();
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
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la sauvegarde du rapport", Toast.LENGTH_SHORT).show();
                });
    }
}
