package projet.gestionabsence.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import projet.gestionabsence.R;
import projet.gestionabsence.model.Teacher;

import java.util.ArrayList;
import java.util.List;

public class AgentHomeActivity extends AppCompatActivity {

    private RecyclerView teachersRecyclerView;
    private TeacherAbsenceAdapter teacherAbsenceAdapter;
    private List<Teacher> teacherList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_agent);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        teachersRecyclerView = findViewById(R.id.teachersRecyclerView);
        teachersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        teacherList = new ArrayList<>();
        teacherAbsenceAdapter = new TeacherAbsenceAdapter(teacherList);
        teachersRecyclerView.setAdapter(teacherAbsenceAdapter);

        loadTeachersFromDatabase();

        FloatingActionButton addAbsenceButton = findViewById(R.id.addAbsenceButton);
        addAbsenceButton.setOnClickListener(v -> {
            AddAbsenceDialogFragment dialogFragment = AddAbsenceDialogFragment.newInstance();
            dialogFragment.show(getSupportFragmentManager(), "AddAbsenceDialogFragment");
        });
    }

    private void loadTeachersFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("teachers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        teacherList.clear();

                        for (DocumentSnapshot document : querySnapshot) {
                            String name = document.getString("name");
                            Object nbAbsenceObj = document.get("nb_absence");
                            int nbAbsence = nbAbsenceObj instanceof Number ? ((Number) nbAbsenceObj).intValue() : 0;

                            teacherList.add(new Teacher(name, nbAbsence));
                        }

                        teacherAbsenceAdapter.notifyDataSetChanged();
                    } else {
                        Log.w("FirebaseError", "Erreur lors de la récupération des documents.", task.getException());
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem notificationItem = menu.findItem(R.id.ic_notification);
        if (hasUnreadNotifications()) {
            notificationItem.setIcon(R.drawable.ic_notification_badge);
        }

        return true;
    }

    private boolean hasUnreadNotifications() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("hasUnreadNotifications", false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ic_logout) {
            showLogoutConfirmationDialog();
            return true;
        } else if (item.getItemId() == R.id.ic_notification) {
            Intent intent = new Intent(AgentHomeActivity.this, NotificationActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void performLogout() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(AgentHomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

