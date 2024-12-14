package projet.gestionabsence.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import projet.gestionabsence.R;
import projet.gestionabsence.model.Absence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddAbsenceActivity extends AppCompatActivity {

    private Spinner teacherSpinner, classroomSpinner;
    private EditText dateEditText, timeEditText;
    private Button addAbsenceButton;
    private List<String> teacherNames = new ArrayList<>();
    private List<String> classroomNames = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_absence);

        teacherSpinner = findViewById(R.id.teacherSpinner);
        classroomSpinner = findViewById(R.id.classroomSpinner);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        addAbsenceButton = findViewById(R.id.addAbsenceButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserName();
        }

        loadTeachers();
        loadClassrooms();
        setDatePickerListener();
        setTimePickerListener();

        addAbsenceButton.setOnClickListener(view -> {
            String teacherName = teacherSpinner.getSelectedItem().toString();
            String classroom = classroomSpinner.getSelectedItem().toString();
            String date = dateEditText.getText().toString();
            String time = timeEditText.getText().toString();

            if (teacherName.isEmpty() || date.isEmpty() || time.isEmpty() || classroom.isEmpty()) {
                Toast.makeText(AddAbsenceActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                addAbsence(teacherName, classroom, date, time, currentUserName);
            }
        });
    }

    private void loadUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserName = currentUser.getDisplayName();

            if (currentUserName == null || currentUserName.isEmpty()) {
                String[] User = currentUser.getEmail().split("@");
                currentUserName = User[0];
                if (currentUserName == null || currentUserName.isEmpty()) {
                    Toast.makeText(this, "Nom d'utilisateur introuvable", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
        }
    }


    private void setDatePickerListener() {
        dateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AddAbsenceActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        dateEditText.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setTimePickerListener() {
        timeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(AddAbsenceActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        timeEditText.setText(selectedHour + ":" + String.format("%02d", selectedMinute));
                    }, hour, minute, true);
            timePickerDialog.show();
        });
    }

    private void loadTeachers() {
        db.collection("teachers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            teacherNames.clear();
                            for (DocumentSnapshot document : querySnapshot) {
                                String teacherName = document.getString("name");
                                if (teacherName != null) {
                                    teacherNames.add(teacherName);
                                }
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddAbsenceActivity.this,
                                    android.R.layout.simple_spinner_item, teacherNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            teacherSpinner.setAdapter(adapter);
                        } else {
                            Toast.makeText(AddAbsenceActivity.this, "Aucun enseignant trouvé", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddAbsenceActivity.this, "Échec de la connexion à la base de données", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadClassrooms() {
        db.collection("classe")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            classroomNames.clear();
                            for (DocumentSnapshot document : querySnapshot) {
                                String classroomName = document.getString("name");
                                if (classroomName != null) {
                                    classroomNames.add(classroomName);
                                }
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddAbsenceActivity.this,
                                    android.R.layout.simple_spinner_item, classroomNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            classroomSpinner.setAdapter(adapter);
                        } else {
                            Toast.makeText(AddAbsenceActivity.this, "Aucune salle trouvée", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddAbsenceActivity.this, "Échec de la connexion à la base de données", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addAbsence(String teacherName, String classroom, String date, String time, String currentUserName) {
        if (currentUserName == null) {
            Toast.makeText(this, "Nom de l'agent non récupéré", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(AddAbsenceActivity.this, AddNotificationActivity.class);
            startActivity(intent);
            finish();

            return;
        }

        Absence absence = new Absence(teacherName, classroom, date, time, currentUserName);

        db.collection("absence")
                .add(absence)
                .addOnSuccessListener(documentReference -> {
                    updateTeacherAbsenceCount(teacherName);
                    Toast.makeText(AddAbsenceActivity.this, "Absence ajoutée avec succès", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddAbsenceActivity.this, Home.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddAbsenceActivity.this, "Erreur lors de l'ajout de l'absence", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateTeacherAbsenceCount(String teacherName) {
        db.collection("teachers")
                .whereEqualTo("name", teacherName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot teacherDocument = task.getResult().getDocuments().get(0);
                        db.collection("teachers")
                                .document(teacherDocument.getId())
                                .update("nb_absence", FieldValue.increment(1))
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddAbsenceActivity.this, "Erreur lors de la mise à jour du nombre d'absences", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(AddAbsenceActivity.this, "Enseignant non trouvé", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddAbsenceActivity.this, "Erreur de récupération des informations de l'enseignant", Toast.LENGTH_SHORT).show();
                });
    }
}
