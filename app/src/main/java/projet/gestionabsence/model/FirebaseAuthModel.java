package projet.gestionabsence.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class FirebaseAuthModel {

    private FirebaseAuth mAuth;

    public FirebaseAuthModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public interface AuthListener {
        void onAuthSuccess();
        void onAuthFailure(String errorMessage);
    }

    public void loginWithEmailAndPassword(String email, String password, AuthListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onAuthSuccess();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Erreur inconnue";
                        listener.onAuthFailure(errorMessage);
                    }
                });
    }
}
