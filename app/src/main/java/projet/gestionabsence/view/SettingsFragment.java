package projet.gestionabsence.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import projet.gestionabsence.R;
import projet.gestionabsence.api.CloudinaryConfig;

public class SettingsFragment extends Fragment {

    private TextView usernameTextView;
    private TextView phoneNumberTextView;
    private TextView emailTextView;
    private ImageView profileImageView;
    private ImageButton uploadImageButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Cloudinary cloudinary;
    private Uri imageUri;
    private LoadingDialogFragment loadingDialog;


    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_settings, container, false);

        usernameTextView = rootView.findViewById(R.id.username_text);
        phoneNumberTextView = rootView.findViewById(R.id.phone_number_text);
        emailTextView = rootView.findViewById(R.id.email_text);
        profileImageView = rootView.findViewById(R.id.profile_image);
        uploadImageButton = rootView.findViewById(R.id.edit_profile_image);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        cloudinary = CloudinaryConfig.getCloudinaryInstance(getContext());

        loadUserData();

        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        ImageButton backButton = rootView.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> getActivity().onBackPressed());

        ImageButton removeImageButton = rootView.findViewById(R.id.remove_profile_image);
        removeImageButton.setOnClickListener(v -> {
            deleteProfileImage();
        });

        Button changePasswordButton = rootView.findViewById(R.id.change_password);
        changePasswordButton.setOnClickListener(v -> {
            ChangePasswordDialogFragment changePasswordDialog = new ChangePasswordDialogFragment();
            changePasswordDialog.setTargetFragment(SettingsFragment.this, 0);
            changePasswordDialog.show(getFragmentManager(), "ChangePasswordDialog");
        });

        return rootView;
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String username = documentSnapshot.getString("username");
                String phoneNumber = documentSnapshot.getString("phone");
                String email = documentSnapshot.getString("email");
                String profileImageUrl = documentSnapshot.getString("profileImage");

                usernameTextView.setText(username != null ? username : "Non renseigné");
                phoneNumberTextView.setText(phoneNumber != null ? phoneNumber : "Non renseigné");
                emailTextView.setText(email != null ? email : "Non renseigné");

                if (profileImageUrl != null) {
                    Glide.with(getActivity())
                            .load(profileImageUrl)
                            .circleCrop()
                            .into(profileImageView);
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);

            uploadImageToCloudinary();
        }
    }

    private void uploadImageToCloudinary() {
        showLoadingDialog();
        if (imageUri != null) {
            try {
                File file = new File(getActivity().getCacheDir(), "profile_image.jpg");
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                new Thread(() -> {
                    try {
                        Map<String, String> options = ObjectUtils.asMap("public_id", mAuth.getCurrentUser().getUid());
                        Map<String, Object> result = cloudinary.uploader().upload(file, options);

                        if (result != null) {
                            String imageUrl = (String) result.get("secure_url");
                            if (imageUrl != null) {
                                Log.d("UploadFragment", "Image uploadée avec succès : " + imageUrl);
                                hideLoadingDialog();
                                saveImageUrlToFirestore(imageUrl);
                            } else {
                                showMessage("Erreur : URL de l'image non disponible.");
                                hideLoadingDialog();
                            }
                        } else {
                            showMessage("Erreur : Résultat d'upload nul.");
                            hideLoadingDialog();
                        }
                    } catch (Exception e) {
                        Log.e("UploadFragment", "Erreur lors de l'upload vers Cloudinary : " + e.getMessage(), e);
                        showMessage("Erreur lors de l'upload vers Cloudinary : " + e.getMessage());
                        hideLoadingDialog();
                    } finally {
                        hideLoadingDialog();
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Erreur lors de la préparation de l'image", Toast.LENGTH_SHORT).show();
                hideLoadingDialog();
            }
        }
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        showLoadingDialog();
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.update("profileImage", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Image téléchargée avec succès", Toast.LENGTH_SHORT).show();
                    hideLoadingDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Erreur lors de la mise à jour de l'image", Toast.LENGTH_SHORT).show();
                    hideLoadingDialog();
                });
        hideLoadingDialog();
    }

    public void changePassword(String oldPassword, String newPassword) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(mAuth.getCurrentUser().getEmail(), oldPassword)
                .addOnSuccessListener(authResult -> {
                    mAuth.getCurrentUser().updatePassword(newPassword)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getActivity(), "Mot de passe changé avec succès", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Erreur lors du changement de mot de passe", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Ancien mot de passe incorrect", Toast.LENGTH_SHORT).show();
                });
    }

    public void updateUserData(String field, String newValue) {
        showLoadingDialog();
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.update(field, newValue)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Données mises à jour avec succès", Toast.LENGTH_SHORT).show();
                    loadUserData();
                    hideLoadingDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Erreur lors de la mise à jour des données", Toast.LENGTH_SHORT).show();
                    hideLoadingDialog();
                });
    }

    private void deleteProfileImage() {
        showLoadingDialog();
        String userId = mAuth.getCurrentUser().getUid();

        new Thread(() -> {
            try {
                cloudinary.uploader().destroy(userId, ObjectUtils.emptyMap());

                DocumentReference userRef = db.collection("users").document(userId);
                userRef.update("profileImage", null)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getActivity(), "Photo de profil supprimée", Toast.LENGTH_SHORT).show();
                            profileImageView.setImageResource(R.drawable.ic_default_profile);
                            hideLoadingDialog();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Erreur lors de la suppression de la photo", Toast.LENGTH_SHORT).show();
                            hideLoadingDialog();
                        });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Erreur lors de la suppression de l'image", Toast.LENGTH_SHORT).show();
                hideLoadingDialog();
            }
        }).start();
    }

    private void showLoadingDialog() {
        if (getActivity() != null) {
            loadingDialog = new LoadingDialogFragment();
            loadingDialog.show(getActivity().getSupportFragmentManager(), "loadingDialog");
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && getActivity() != null) {
            loadingDialog.dismiss();
        }
    }
    private void showMessage(String message) {
        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show());
    }
}
