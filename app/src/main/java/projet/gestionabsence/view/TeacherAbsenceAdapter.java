package projet.gestionabsence.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;
import android.content.Intent;
import projet.gestionabsence.R;
import projet.gestionabsence.model.Teacher;
import java.util.List;

public class TeacherAbsenceAdapter extends RecyclerView.Adapter<TeacherAbsenceAdapter.TeacherViewHolder> {

    private List<Teacher> teacherList;
    private OnItemClickListener onItemClickListener;

    public TeacherAbsenceAdapter(List<Teacher> teacherList) {
        this.teacherList = teacherList;
    }

    @Override
    public TeacherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TeacherViewHolder holder, int position) {
        Teacher teacher = teacherList.get(position);
        holder.nameTextView.setText(teacher.getName());
        holder.nbAbsenceTextView.setText(String.valueOf(teacher.getNbAbsence()));

        holder.showAbsencesButton.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(teacher);
            }
        });
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(Teacher teacher);
    }

    public static class TeacherViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, nbAbsenceTextView;
        ImageButton showAbsencesButton;

        public TeacherViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            nbAbsenceTextView = itemView.findViewById(R.id.nbAbsenceTextView);
            showAbsencesButton = itemView.findViewById(R.id.showAbsencesButton);
        }
    }
}

