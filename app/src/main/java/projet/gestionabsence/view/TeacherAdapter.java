package projet.gestionabsence.view;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import projet.gestionabsence.R;
import projet.gestionabsence.model.Teacher;

import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    private List<Teacher> teacherList;
    private FirebaseFirestore db;

    public TeacherAdapter(List<Teacher> teacherList) {
        this.teacherList = teacherList;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_list, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        Teacher teacher = teacherList.get(position);
        holder.nameTextView.setText(teacher.getName());
        holder.postTextView.setText(teacher.getPost());

        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(teacher, holder));
        holder.editButton.setOnClickListener(v -> showEditTeacherDialog(teacher, holder));
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    private void showEditTeacherDialog(Teacher teacher, TeacherViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        View dialogView = inflater.inflate(R.layout.dialog_edit_teacher, null);

        EditText editTextName = dialogView.findViewById(R.id.editTextTeacherName);
        EditText editTextPost = dialogView.findViewById(R.id.editTextTeacherPost);
        Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdateTeacher);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        editTextName.setText(teacher.getName());
        editTextPost.setText(teacher.getPost());

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        buttonUpdate.setOnClickListener(v -> {
            String newName = editTextName.getText().toString().trim();
            String newPost = editTextPost.getText().toString().trim();

            if (newName.isEmpty() || newPost.isEmpty()) {
                Toast.makeText(holder.itemView.getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            updateTeacherInFirebase(teacher, newName, newPost, holder);
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateTeacherInFirebase(Teacher teacher, String newName, String newPost, TeacherViewHolder holder) {
        db.collection("teachers")
                .document(teacher.getId())
                .update("name", newName, "post", newPost)
                .addOnSuccessListener(aVoid -> {
                    teacher.setName(newName);
                    teacher.setPost(newPost);
                    notifyDataSetChanged();
                    Toast.makeText(holder.itemView.getContext(), "Mise à jour réussie", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(holder.itemView.getContext(), "Échec de la mise à jour", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmationDialog(Teacher teacher, TeacherViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setMessage("Êtes-vous sûr de vouloir supprimer cet enseignant ?");
        builder.setCancelable(false);

        builder.setPositiveButton("Oui", (dialog, id) -> deleteTeacherFromFirebase(teacher, holder));
        builder.setNegativeButton("Non", (dialog, id) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteTeacherFromFirebase(Teacher teacher, TeacherViewHolder holder) {
        db.collection("teachers")
                .document(teacher.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    teacherList.remove(teacher);
                    notifyDataSetChanged();
                    Toast.makeText(holder.itemView.getContext(), "Suppression réussie", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(holder.itemView.getContext(), "Échec de la suppression", Toast.LENGTH_SHORT).show();
                });
    }

    public static class TeacherViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView postTextView;
        ImageButton deleteButton;
        ImageButton editButton;

        public TeacherViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            postTextView = itemView.findViewById(R.id.postTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
}
