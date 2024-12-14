package projet.gestionabsence.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import projet.gestionabsence.R;
import projet.gestionabsence.model.Absence;
import projet.gestionabsence.view.AdminAbsenceAdapter;


public class HistoryFragment extends Fragment {

    private RecyclerView absencesRecyclerView;
    private AdminAbsenceAdapter absenceAdapter;
    private List<Absence> absenceList;

    private Spinner yearSpinner, monthSpinner, daySpinner, teacherSpinner, classSpinner;
    private Button filterButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_historique, container, false);

        absencesRecyclerView = rootView.findViewById(R.id.absenceHistoryRecyclerView);
        absencesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        absenceList = new ArrayList<>();

        yearSpinner = rootView.findViewById(R.id.yearSpinner);
        monthSpinner = rootView.findViewById(R.id.monthSpinner);
        daySpinner = rootView.findViewById(R.id.daySpinner);
        teacherSpinner = rootView.findViewById(R.id.teacherSpinner);
        classSpinner = rootView.findViewById(R.id.classSpinner);
        filterButton = rootView.findViewById(R.id.filterButton);

        setupSpinners();
        fetchTeachers();
        fetchClasses();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        loadAbsences(db, null, null, null, null, null); // Load all absences initially

        filterButton.setOnClickListener(v -> {
            String selectedYear = yearSpinner.getSelectedItem().toString();
            String selectedMonth = monthSpinner.getSelectedItem().toString();
            String selectedDay = daySpinner.getSelectedItem().toString();
            String selectedTeacher = teacherSpinner.getSelectedItem().toString();
            String selectedClass = classSpinner.getSelectedItem().toString();

            loadAbsences(db, selectedYear, selectedMonth, selectedDay, selectedTeacher, selectedClass);
        });

        return rootView;
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

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, days);
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
                        teacherNames.add("Professeur"); // Add the header to the spinner
                        for (DocumentSnapshot document : task.getResult()) {
                            teacherNames.add(document.getString("name"));
                        }
                        ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, teacherNames);
                        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        teacherSpinner.setAdapter(teacherAdapter);
                    } else {
                        Log.w("FirebaseError", "Failed to fetch teachers.", task.getException());
                    }
                });
    }

    private void fetchClasses() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("classe")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> classNames = new ArrayList<>();
                        classNames.add("Salle"); // Add the header to the spinner
                        for (DocumentSnapshot document : task.getResult()) {
                            classNames.add(document.getString("name"));
                        }
                        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, classNames);
                        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        classSpinner.setAdapter(classAdapter);
                    } else {
                        Log.w("FirebaseError", "Failed to fetch classes.", task.getException());
                    }
                });
    }

    private void loadAbsences(FirebaseFirestore db, String year, String month, String day, String teacher, String classroom) {
        db.collection("absence")
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

                                if (isDateValid(date, year, month, day)) {
                                    boolean matchesFilter = false;

                                    if (teacher != null && !teacher.equals("Professeur") && teacherName.equals(teacher)) {
                                        matchesFilter = true;
                                    }
                                    if (classroom != null && !classroom.equals("Salle") && classroomName.equals(classroom)) {
                                        matchesFilter = true;
                                    }

                                    if ((teacher == null || teacher.equals("Professeur")) && (classroom == null || classroom.equals("Salle"))) {
                                        matchesFilter = true;
                                    }

                                    if (matchesFilter) {
                                        absenceList.add(new Absence(teacherName, classroomName, date, time, nameAgent));
                                    }
                                }
                            }

                            absenceAdapter = new AdminAbsenceAdapter(absenceList);
                            absencesRecyclerView.setAdapter(absenceAdapter);
                        }
                    } else {
                        Log.w("FirebaseError", "Erreur lors de la récupération des absences.", task.getException());
                    }
                });
    }


    private boolean isDateValid(String date, String year, String month, String day) {
        if (date != null) {
            String[] dateParts = date.split("/");
            if (dateParts.length < 3) return false;

            String dateYear = dateParts[2];
            String dateMonth = dateParts[1];
            String dateDay = dateParts[0];

            boolean yearMatches = (year == null || dateYear.equals(year));
            boolean monthMatches = (month == null || dateMonth.equals(month));
            boolean dayMatches = (day == null || dateDay.equals(day));

            return yearMatches && monthMatches && dayMatches;
        }
        return false;
    }
}
