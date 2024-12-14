package projet.gestionabsence.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import projet.gestionabsence.R;
import projet.gestionabsence.model.Absence;
import okhttp3.*;
import projet.gestionabsence.model.FirestoreNotification;

import org.json.JSONException;
import org.json.JSONObject;

public class AddAbsenceDialogFragment extends DialogFragment {

    private Spinner teacherSpinner, classroomSpinner;
    private EditText dateEditText, timeEditText;
    private Button addAbsenceButton, cancelButton;
    private List<String> teacherNames = new ArrayList<>();
    private List<String> classroomNames = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserName;

    public static AddAbsenceDialogFragment newInstance() {
        return new AddAbsenceDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_absence, container, false);

        teacherSpinner = view.findViewById(R.id.teacherSpinner);
        classroomSpinner = view.findViewById(R.id.classroomSpinner);
        dateEditText = view.findViewById(R.id.dateEditText);
        timeEditText = view.findViewById(R.id.timeEditText);
        addAbsenceButton = view.findViewById(R.id.addAbsenceButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadUserName();
        loadTeachers();
        loadClassrooms();
        setDatePickerListener();
        setTimePickerListener();

        addAbsenceButton.setOnClickListener(v -> handleAddAbsence());
        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void loadUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserName = currentUser.getDisplayName();
            if (currentUserName == null || currentUserName.isEmpty()) {
                String[] userEmailParts = currentUser.getEmail().split("@");
                currentUserName = userEmailParts[0];
            }
            if (currentUserName == null || currentUserName.isEmpty()) {
                Toast.makeText(getContext(), "Nom d'utilisateur introuvable", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
        }
    }

    private void setDatePickerListener() {
        dateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) ->
                            dateEditText.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear),
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private void setTimePickerListener() {
        timeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (view, selectedHour, selectedMinute) ->
                            timeEditText.setText(selectedHour + ":" + String.format("%02d", selectedMinute)),
                    hour, minute, true);
            timePickerDialog.show();
        });
    }

    private void loadTeachers() {
        db.collection("teachers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                teacherNames.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    String name = document.getString("name");
                    if (name != null) teacherNames.add(name);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, teacherNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                teacherSpinner.setAdapter(adapter);
            } else {
                Toast.makeText(getContext(), "Erreur de récupération des enseignants", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClassrooms() {
        db.collection("classe").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                classroomNames.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    String name = document.getString("name");
                    if (name != null) classroomNames.add(name);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, classroomNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                classroomSpinner.setAdapter(adapter);
            } else {
                Toast.makeText(getContext(), "Erreur de récupération des classes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleAddAbsence() {
        String teacherName = teacherSpinner.getSelectedItem() != null ? teacherSpinner.getSelectedItem().toString() : "";
        String classroom = classroomSpinner.getSelectedItem() != null ? classroomSpinner.getSelectedItem().toString() : "";
        String date = dateEditText.getText().toString();
        String time = timeEditText.getText().toString();

        if (teacherName.isEmpty() || classroom.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
        } else {
            addAbsence(teacherName, classroom, date, time);
        }
    }

    private void addAbsence(String teacherName, String classroom, String date, String time) {
        Absence absence = new Absence(teacherName, classroom, date, time, currentUserName);

        db.collection("absence")
                .add(absence)
                .addOnSuccessListener(documentReference -> {
                    updateTeacherAbsenceCount(teacherName);

                    db.collection("users")
                            .whereEqualTo("userType", "teacher")
                            .whereEqualTo("name", teacherName)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    String teacherToken = querySnapshot.getDocuments().get(0).getString("fcmToken");

                                    db.collection("users")
                                            .whereEqualTo("userType", "admin")
                                            .get()
                                            .addOnSuccessListener(adminSnapshot -> {
                                                List<String> adminTokens = new ArrayList<>();
                                                for (DocumentSnapshot adminDoc : adminSnapshot.getDocuments()) {
                                                    String adminToken = adminDoc.getString("fcmToken");
                                                    if (adminToken != null) {
                                                        adminTokens.add(adminToken);
                                                    }
                                                }

                                                sendNotificationToTeacherAndAdmins(teacherToken, adminTokens, teacherName, date);
                                                saveNotificationToFirestore(teacherName, date);
                                            });
                                }
                            });

                    Toast.makeText(getContext(), "Absence ajoutée avec succès", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Erreur lors de l'ajout de l'absence", Toast.LENGTH_SHORT).show());
    }

    private void sendNotificationToTeacherAndAdmins(String teacherToken, List<String> adminTokens, String teacherName, String absenceDate) {
        try {
            String FCM_API_URL = "https://fcm.googleapis.com/v1/projects/gestionabsence-36259/messages:send";
            String SERVER_KEY = "BFsxfmoMtvYxLyB3L9lpQ3ELWfqNDQFBgBA9Obwq9UhqW0BT8IK5CppZYiuK-2cZ6ECJXYp72GTa8k2TLIiJPgk";

            JSONObject message = new JSONObject();
            JSONObject notification = new JSONObject();
            notification.put("title", "Nouvelle absence enregistrée");
            notification.put("body", "Une absence a été ajoutée pour " + teacherName + " le " + absenceDate);

            JSONObject data = new JSONObject();
            data.put("teacherName", teacherName);
            data.put("absenceDate", absenceDate);

            sendFCMNotification(teacherToken, notification, data, FCM_API_URL, SERVER_KEY);

            for (String adminToken : adminTokens) {
                sendFCMNotification(adminToken, notification, data, FCM_API_URL, SERVER_KEY);
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Erreur lors de l'envoi de la notification", Toast.LENGTH_LONG).show();
        }
    }

    private void sendFCMNotification(String token, JSONObject notification, JSONObject data, String url, String serverKey) throws IOException, JSONException {
        JSONObject message = new JSONObject();
        JSONObject tokenMessage = new JSONObject();
        tokenMessage.put("token", token);
        tokenMessage.put("notification", notification);
        tokenMessage.put("data", data);

        message.put("message", tokenMessage);

        Log.d("FCM", "Message JSON : " + message.toString());

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(message.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + serverKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FCM", "Échec de l'envoi de la notification", e);
                Toast.makeText(getContext(), "Échec de l'envoi de la notification", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("FCM", "Erreur dans la réponse FCM: " + response.message());
                }
            }
        });
    }

    private void saveNotificationToFirestore(String teacherName, String absenceDate) {
        FirestoreNotification notification = new FirestoreNotification(teacherName, absenceDate, FieldValue.serverTimestamp());
        db.collection("notifications").add(notification);
    }

    private void updateTeacherAbsenceCount(String teacherName) {
        db.collection("teachers")
                .whereEqualTo("name", teacherName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot teacherDoc = querySnapshot.getDocuments().get(0);
                        int absenceCount = teacherDoc.getLong("nb_absence") != null ? teacherDoc.getLong("nb_absence").intValue() : 0;
                        db.collection("teachers").document(teacherDoc.getId()).update("nb_absence", absenceCount + 1);
                    }
                });
    }
}
