package projet.gestionabsence.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import projet.gestionabsence.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends AppCompatActivity {

    private Map<Integer, Fragment> fragmentMap;
    private LoadingDialogFragment loadingDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setSupportActionBar(findViewById(R.id.toolbar));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.nav_home, new TeacherFragment());
        fragmentMap.put(R.id.nav_enseignants, new TeacherFragmentList());
        fragmentMap.put(R.id.nav_historique, new HistoryFragment());
        fragmentMap.put(R.id.nav_rapport, new RapportFragment());
        fragmentMap.put(R.id.nav_upload, new UploadFragment());

        if (savedInstanceState == null) {
            loadFragment(R.id.nav_home);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            loadFragment(item.getItemId());
            return true;
        });

        loadingDialogFragment = new LoadingDialogFragment();
    }

    private void loadFragment(int itemId) {
        Fragment fragment = fragmentMap.get(itemId);
        if (fragment != null) {
            if (getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName()) == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem notificationItem = menu.findItem(R.id.ic_notification);
        if (hasUnreadNotifications()) {
            notificationItem.setIcon(R.drawable.ic_notification_badge);
        } else {
            notificationItem.setIcon(R.drawable.ic_notification);
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
            showNotificationsPopup(item);
            return true;
        } else if (item.getItemId() == R.id.ic_settings) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment())
                    .addToBackStack(null)
                    .commit();
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

        Intent intent = new Intent(Home.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showNotificationsPopup(MenuItem item) {
        List<String> notifications = new ArrayList<>();
        notifications.add("Notification 1");
        notifications.add("Notification 2");
        notifications.add("Notification 3");

        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.ic_notification));

        for (String notification : notifications) {
            popupMenu.getMenu().add(notification);
        }

        popupMenu.getMenu().add("Voir toutes les notifications");

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            String clickedNotification = menuItem.getTitle().toString();

            if (clickedNotification.equals("Voir toutes les notifications")) {
                Intent intent = new Intent(Home.this, NotificationActivity.class);
                startActivity(intent);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Notification")
                        .setMessage(clickedNotification)
                        .setPositiveButton("OK", null)
                        .show();
            }
            return true;
        });

        popupMenu.show();
    }


    private void showLoadingDialog() {
        if (!loadingDialogFragment.isAdded()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(loadingDialogFragment, "loadingDialog");
            ft.commitAllowingStateLoss();
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialogFragment.isAdded()) {
            loadingDialogFragment.dismiss();
        }
    }


}
