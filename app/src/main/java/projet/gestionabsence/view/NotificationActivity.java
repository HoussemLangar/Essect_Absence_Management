package projet.gestionabsence.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import projet.gestionabsence.R;
import projet.gestionabsence.model.Notification;
import projet.gestionabsence.view.NotificationAdapter;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView notificationRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationRecyclerView = findViewById(R.id.notificationRecyclerView);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(notifications);
        notificationRecyclerView.setAdapter(notificationAdapter);

        loadNotifications();
    }

    private void loadNotifications() {
        SharedPreferences sharedPreferences = getSharedPreferences("Notifications", MODE_PRIVATE);
        String title = sharedPreferences.getString("title", "Aucune notification");
        String body = sharedPreferences.getString("body", "Aucun message");

        if (title != null && body != null) {
            Notification notification = new Notification(title, body);
            notifications.add(notification);
        }

        notificationAdapter.notifyDataSetChanged();
    }
}
