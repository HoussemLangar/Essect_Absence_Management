package projet.gestionabsence.view;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import projet.gestionabsence.R;
import projet.gestionabsence.model.Teacher;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TeacherFragmentList extends Fragment {

    private RecyclerView recyclerView;
    private TeacherAdapter teacherAdapter;
    private List<Teacher> teacherList;
    private FirebaseFirestore db;

    public TeacherFragmentList() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewTeachers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        teacherList = new ArrayList<>();
        teacherAdapter = new TeacherAdapter(teacherList);
        recyclerView.setAdapter(teacherAdapter);

        loadTeachersFromFirebase();

        FloatingActionButton addTeacherButton = view.findViewById(R.id.addTeacherButton);
        addTeacherButton.setOnClickListener(v -> showAddTeacherDialog());

        return view;
    }

    private void loadTeachersFromFirebase() {
        db.collection("teachers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            teacherList.clear();
                            for (DocumentSnapshot document : querySnapshot) {
                                String name = document.getString("name");
                                String post = document.getString("post");

                                Long nbAbsenceLong = document.getLong("nbAbsence");
                                int nbAbsence = (nbAbsenceLong != null) ? nbAbsenceLong.intValue() : 0;

                                String documentId = document.getId();

                                Teacher teacher = new Teacher(name, post);
                                teacher.setId(documentId);
                                teacher.setNbAbsence(nbAbsence);

                                teacherList.add(teacher);
                            }
                            teacherAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void showAddTeacherDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_teacher, null);

        EditText editTextName = dialogView.findViewById(R.id.editTextTeacherName);
        EditText editTextPost = dialogView.findViewById(R.id.editTextTeacherPost);
        Button buttonAdd = dialogView.findViewById(R.id.buttonAddTeacher);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        buttonAdd.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String post = editTextPost.getText().toString().trim();

            if (name.isEmpty() || post.isEmpty()) {
                Toast.makeText(getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            addTeacherToFirebase(name, post);
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }


    private void addTeacherToFirebase(String name, String post) {
        Teacher teacher = new Teacher(name, post);
        teacher.setNbAbsence(0); // Par défaut à 0

        db.collection("teachers")
                .add(teacher)
                .addOnSuccessListener(documentReference -> {
                    teacher.setId(documentReference.getId());
                    teacherList.add(teacher);
                    teacherAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Enseignant ajouté avec succès", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                });
    }
}
