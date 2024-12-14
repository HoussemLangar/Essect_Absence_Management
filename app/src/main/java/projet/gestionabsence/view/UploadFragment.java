package projet.gestionabsence.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.Cloudinary;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import projet.gestionabsence.R;
import projet.gestionabsence.api.CloudinaryConfig;
import projet.gestionabsence.model.ScheduleItem;



public class UploadFragment extends Fragment {

    private static final int FILE_PICKER_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private RecyclerView recyclerView;
    private LoadingDialogFragment loadingDialog;

    public UploadFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton uploadButton = view.findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> checkPermissionsAndOpenFilePicker());

        loadSchedulesFromFirebase();

        return view;
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

    private void checkPermissionsAndOpenFilePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                }, PERMISSION_REQUEST_CODE);
            } else {
                openFilePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, PERMISSION_REQUEST_CODE);
            } else {
                openFilePicker();
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    File file = createTempFileFromUri(uri);
                    if (file != null) {
                        uploadFileToCloudinary(file);
                    } else {
                        showMessage("Erreur lors de la création du fichier temporaire.");
                    }
                } catch (Exception e) {
                    Log.e("UploadFragment", "Erreur lors de la lecture du fichier : " + e.getMessage(), e);
                    showMessage("Erreur lors de la lecture du fichier.");
                }
            } else {
                showMessage("Fichier non sélectionné.");
            }
        }
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        String uniqueId = "EMP_" + System.currentTimeMillis();
        File tempFile = new File(getContext().getCacheDir(), uniqueId);

        InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Impossible de lire le fichier.");
        }

        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
        inputStream.close();

        Log.d("UploadFragment", "Fichier temporaire créé : " + tempFile.getAbsolutePath() + ", Taille : " + tempFile.length());
        return tempFile;
    }

    private void uploadFileToCloudinary(File file) {
        Cloudinary cloudinary = CloudinaryConfig.getCloudinaryInstance(getContext());
        if (cloudinary == null) {
            showMessage("Erreur de configuration Cloudinary.");
            return;
        }
        showLoadingDialog();

        Map<String, Object> uploadParams = new HashMap<>();
        uploadParams.put("resource_type", "auto");

        new Thread(() -> {
            try {
                Map<String, Object> result = cloudinary.uploader().upload(file, uploadParams);
                if (result != null) {
                    String fileUrl = (String) result.get("secure_url");
                    if (fileUrl != null) {
                        Log.d("UploadFragment", "Fichier uploadé avec succès : " + fileUrl);
                        saveFileToFirebase(file.getName(), fileUrl);
                    } else {
                        showMessage("Erreur : URL du fichier non disponible.");
                    }
                } else {
                    showMessage("Erreur : Résultat d'upload nul.");
                }
            } catch (Exception e) {
                Log.e("UploadFragment", "Erreur lors de l'upload vers Cloudinary : " + e.getMessage(), e);
                showMessage("Erreur lors de l'upload vers Cloudinary : " + e.getMessage());
            } finally {
            hideLoadingDialog();
        }
        }).start();
    }

    private void saveFileToFirebase(String fileName, String fileUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference schedulesRef = db.collection("schedules");

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("name", fileName);
        fileData.put("url", fileUrl);

        schedulesRef.add(fileData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("UploadFragment", "Document ajouté avec succès : " + documentReference.getId());
                    showMessage("Fichier ajouté avec succès.");
                    loadSchedulesFromFirebase();  // Recharge les schedules après l'ajout
                })
                .addOnFailureListener(e -> {
                    Log.e("UploadFragment", "Erreur lors de l'ajout du document : " + e.getMessage(), e);
                    showMessage("Erreur lors de l'ajout du fichier.");
                });
    }

    private void loadSchedulesFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference schedulesRef = db.collection("schedules");

        schedulesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ScheduleItem> scheduleItems = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ScheduleItem scheduleItem = document.toObject(ScheduleItem.class);
                        if (scheduleItem != null) {
                            scheduleItems.add(scheduleItem);
                        }
                    }

                    ScheduleAdapter.OnItemClickListener listener = new ScheduleAdapter.OnItemClickListener() {
                        @Override
                        public void onDeleteClick(ScheduleItem item) {
                            deleteSchedule(item);
                        }

                        @Override
                        public void onDownloadClick(ScheduleItem item) {
                            downloadSchedule(item);
                        }
                    };

                    ScheduleAdapter adapter = new ScheduleAdapter(scheduleItems, listener);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("UploadFragment", "Erreur lors de la récupération des schedules : " + e.getMessage());
                    showMessage("Erreur lors de la récupération des schedules.");
                });
    }

    private void showMessage(String message) {
        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show());
    }

    private void deleteSchedule(ScheduleItem scheduleItem) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmer la suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce fichier ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    showLoadingDialog();

                    Cloudinary cloudinary = CloudinaryConfig.getCloudinaryInstance(getContext());
                    if (cloudinary != null) {
                        Map<String, Object> deleteParams = new HashMap<>();
                        deleteParams.put("public_id", scheduleItem.getName());  // Nom correspond à public_id sur Cloudinary

                        new Thread(() -> {
                            try {
                                cloudinary.uploader().destroy(scheduleItem.getName(), deleteParams);
                                deleteFromFirebase(scheduleItem);
                            } catch (Exception e) {
                                Log.e("UploadFragment", "Erreur lors de la suppression du fichier : " + e.getMessage(), e);
                                showMessage("Erreur lors de la suppression du fichier.");
                            } finally {
                                hideLoadingDialog();
                            }
                        }).start();
                    }
                })
                .setNegativeButton("Non", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }


    private void deleteFromFirebase(ScheduleItem scheduleItem) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference schedulesRef = db.collection("schedules");

        schedulesRef.whereEqualTo("name", scheduleItem.getName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("UploadFragment", "Fichier supprimé de Firebase.");
                                    showMessage("Fichier supprimé avec succès.");
                                    loadSchedulesFromFirebase();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UploadFragment", "Erreur lors de la suppression de Firebase : " + e.getMessage());
                                    showMessage("Erreur lors de la suppression de Firebase.");
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UploadFragment", "Erreur lors de la récupération du document pour suppression : " + e.getMessage());
                    showMessage("Erreur lors de la récupération du document pour suppression.");
                });
    }

    private void downloadSchedule(ScheduleItem item) {
        String url = item.getUrl();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

}
