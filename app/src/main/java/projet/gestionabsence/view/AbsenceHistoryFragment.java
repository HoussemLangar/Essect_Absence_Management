package projet.gestionabsence.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import projet.gestionabsence.R;
import projet.gestionabsence.model.Absence;

import java.util.ArrayList;
import java.util.List;

public class AbsenceHistoryFragment extends Fragment {

    private RecyclerView absencesRecyclerView;
    private AbsenceAdapter absenceAdapter;
    private List<Absence> absenceList;
    private String teacherName;

    private Spinner yearSpinner, monthSpinner, daySpinner;
    private Button filterButton;
    private TextView professorNameTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_absence_history, container, false);

        absencesRecyclerView = view.findViewById(R.id.absenceHistoryRecyclerView);
        absencesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        absenceList = new ArrayList<>();

        yearSpinner = view.findViewById(R.id.yearSpinner);
        monthSpinner = view.findViewById(R.id.monthSpinner);
        daySpinner = view.findViewById(R.id.daySpinner);
        filterButton = view.findViewById(R.id.filterButton);
        professorNameTextView = view.findViewById(R.id.professorNameTextView);

        teacherName = getArguments().getString("teacherName");
        if (teacherName != null) {
            professorNameTextView.setText("Nom du Professeur : " + teacherName);
        }

        setupSpinners();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        loadAbsences(db, null, null, null);

        filterButton.setOnClickListener(v -> {
            String selectedYear = yearSpinner.getSelectedItem().toString();
            String selectedMonth = monthSpinner.getSelectedItem().toString();
            String selectedDay = daySpinner.getSelectedItem().toString();

            if ("Année".equals(selectedYear)) selectedYear = null;
            if ("Mois".equals(selectedMonth)) selectedMonth = null;
            if ("Jour".equals(selectedDay)) selectedDay = null;

            loadAbsences(db, selectedYear, selectedMonth, selectedDay);
        });

        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        return view;
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

    private void loadAbsences(FirebaseFirestore db, String year, String month, String day) {
        db.collection("absence")
                .whereEqualTo("teacherName", teacherName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            absenceList.clear();
                            for (DocumentSnapshot document : querySnapshot) {
                                String classroom = document.getString("classroom");
                                String date = document.getString("date");
                                String time = document.getString("time");
                                String nameAgent = document.getString("nameAgent");
                                if (nameAgent == null) {
                                    nameAgent = "Agent non spécifié";
                                }

                                if (isDateValid(date, year, month, day)) {
                                    absenceList.add(new Absence(teacherName, classroom, date, time, nameAgent));
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
