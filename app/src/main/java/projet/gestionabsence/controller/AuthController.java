package projet.gestionabsence.controller;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import projet.gestionabsence.model.User;

public class AuthController {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private Context context;

    public AuthController(Context context) {
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void loginUser(String email, String password, final UserCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        db.collection("users").document(firebaseUser.getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String userType = documentSnapshot.getString("userType");
                                        User user = new User(firebaseUser.getUid(), userType, email);
                                        callback.onUserRetrieved(user);
                                    } else {
                                        Toast.makeText(context, "Données utilisateur non trouvées", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Échec de l'authentification", Toast.LENGTH_SHORT).show();
                });
    }

    public interface UserCallback {
        void onUserRetrieved(User user);
    }
}
