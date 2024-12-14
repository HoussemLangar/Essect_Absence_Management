package projet.gestionabsence.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import projet.gestionabsence.R;
import projet.gestionabsence.model.Absence;

public class AbsenceHistoryTeacherActivity extends AppCompatActivity {

    private RecyclerView absencesRecyclerView;
    private AbsenceAdapter absenceAdapter;
    private List<Absence> absenceList;

    private Spinner yearSpinner, monthSpinner, daySpinner, teacherSpinner, classSpinner;
    private Button filterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_historique);

        absencesRecyclerView = findViewById(R.id.absenceHistoryRecyclerView);
        absencesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        absenceList = new ArrayList<>();

        yearSpinner = findViewById(R.id.yearSpinner);
        monthSpinner = findViewById(R.id.monthSpinner);
        daySpinner = findViewById(R.id.daySpinner);
        teacherSpinner = findViewById(R.id.teacherSpinner);
        classSpinner = findViewById(R.id.classSpinner);
        filterButton = findViewById(R.id.filterButton);

        setupSpinners();
        fetchTeachers();
        fetchClasses();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        loadAbsences(db, null, null, null, null, null);

        filterButton.setOnClickListener(v -> {
            String selectedYear = yearSpinner.getSelectedItem().toString();
            String selectedMonth = monthSpinner.getSelectedItem().toString();
            String selectedDay = daySpinner.getSelectedItem().toString();
            String selectedTeacher = teacherSpinner.getSelectedItem().toString();
            String selectedClass = classSpinner.getSelectedItem().toString();

            loadAbsences(db, selectedYear, selectedMonth, selectedDay, selectedTeacher, selectedClass);
        });
    }

    private void setupSpinners() {
        List<String> years = new ArrayList<>();
        years.add("Année");
        for (int i = 2020; i <= 2030; i++) {
            years.add(String.valueOf(i));
        }

        List<String> months = new ArrayList<>();
        months.add("Mois");
        for (int i = 1; i <= 12; i++) {
            months.add(String.format("%02d", i));
        }

        List<String> days = new ArrayList<>();
        days.add("Jour");
        for (int i = 1; i <= 31; i++) {
            days.add(String.format("%02d", i));
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
    }

    private void fetchTeachers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("teachers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> teacherNames = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            teacherNames.add(document.getString("name"));
                        }
                        ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teacherNames);
                        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        teacherSpinner.setAdapter(teacherAdapter);
                    } else {
                        Log.w("FirebaseError", "Failed to fetch teachers.", task.getException());
                    }
                });
    }

    private void fetchClasses() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("classes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> classNames = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            classNames.add(document.getString("name"));
                        }
                        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classNames);
                        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        classSpinner.setAdapter(classAdapter);
                    } else {
                        Log.w("FirebaseError", "Failed to fetch classes.", task.getException());
                    }
                });
    }

    private void loadAbsences(FirebaseFirestore db, String year, String month, String day, String teacher, String classroom) {
        db.collection("absence")
                .whereEqualTo("teacherName", teacher)
                .whereEqualTo("classroom", classroom)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        absenceList.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot) {
                                String teacherName = document.getString("teacherName");
                                String classroomName = document.getString("classroom");
                                String date = document.getString("date");
                                String time = document.getString("time");
                                String nameAgent = document.getString("nameAgent");

                                if (nameAgent == null) {
                                    nameAgent = "Agent non spécifié";
                                }

                                // Ensure the date matches the selected filters
                                if (isDateValid(date, year, month, day)) {
                                    absenceList.add(new Absence(teacherName, classroomName, date, time, nameAgent));
                                }
                            }

                            absenceAdapter = new AbsenceAdapter(absenceList);
                            absencesRecyclerView.setAdapter(absenceAdapter);
                        }
                    } else {
                        Log.w("FirebaseError", "Erreur lors de la récupération des absences.", task.getException());
                    }
                });
    }

    private boolean isDateValid(String date, String year, String month, String day) {
        if (year != null && !date.startsWith(year)) {
            return false;
        }
        if (month != null && !date.substring(5, 7).equals(month)) {
            return false;
        }
        if (day != null && !date.substring(8, 10).equals(day)) {
            return false;
        }
        return true;
    }
}
