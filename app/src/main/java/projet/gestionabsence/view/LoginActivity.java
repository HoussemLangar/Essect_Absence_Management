package projet.gestionabsence.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import projet.gestionabsence.controller.AuthController;
import projet.gestionabsence.R;
import projet.gestionabsence.model.User;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private TextInputLayout passwordTextInputLayout;
    private Button loginButton;

    private AuthController authController;

    private LoadingDialogFragment loadingDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            String userType = sharedPreferences.getString("userType", null);
            if (userType != null) {
                redirectUserBasedOnType(userType);
                return;
            }
        }

        setContentView(R.layout.activity_login);

        authController = new AuthController(this);

        emailEditText = findViewById(R.id.emailEditText);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        loginButton = findViewById(R.id.loginButton);

        loadingDialogFragment = new LoadingDialogFragment();

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordTextInputLayout.getEditText().getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                hideLoadingDialog();
            } else {
                showLoadingDialog();
                authController.loginUser(email, password, this::handleLoginSuccess);
                hideLoadingDialog();
            }
        });

        TextView forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
        forgotPasswordLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void handleLoginSuccess(User user) {
        hideLoadingDialog();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("userEmail", user.getEmail());
        editor.putString("userType", user.getUserType());
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        saveTokenToFirestore(user.getUid(), token);
                    } else {
                        Log.e("FCM", "Erreur lors de la récupération du token FCM", task.getException());
                    }
                });

        redirectUserBasedOnType(user.getUserType());
    }

    private void redirectUserBasedOnType(String userType) {
        Intent intent;
        switch (userType) {
            case "admin":
                intent = new Intent(LoginActivity.this, Home.class);
                break;
            case "agent":
                intent = new Intent(LoginActivity.this, AgentHomeActivity.class);
                break;
            case "teacher":
                intent = new Intent(LoginActivity.this, TeacherHomeActivity.class);
                break;
            default:
                Toast.makeText(this, "Type d'utilisateur inconnu", Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
        finish();
    }

    private void saveTokenToFirestore(String userId, String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token FCM enregistré avec succès"))
                .addOnFailureListener(e -> Log.e("FCM", "Erreur lors de l'enregistrement du token FCM", e));
    }

    private void showLoadingDialog() {
        if (!loadingDialogFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(loadingDialogFragment, "loadingDialog")
                    .commitAllowingStateLoss();
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialogFragment.isAdded()) {
            loadingDialogFragment.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Permission de notifications accordée");
            } else {
                Toast.makeText(this, "Permission de notifications refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
        startActivity(intent);

        finish();
    }
}
