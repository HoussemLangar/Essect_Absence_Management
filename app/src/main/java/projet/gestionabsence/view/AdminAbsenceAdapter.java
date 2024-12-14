package projet.gestionabsence.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import projet.gestionabsence.R;
import projet.gestionabsence.model.Absence;

import java.util.List;

public class AdminAbsenceAdapter extends RecyclerView.Adapter<AdminAbsenceAdapter.AbsenceViewHolder> {

    private List<Absence> absenceList;

    public AdminAbsenceAdapter(List<Absence> absenceList) {
        this.absenceList = absenceList;
    }

    @NonNull
    @Override
    public AbsenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_absence_admin, parent, false);
        return new AbsenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsenceViewHolder holder, int position) {
        Absence absence = absenceList.get(position);

        holder.teacherNameTextView.setText("Professeur : " + absence.getTeacherName());
        holder.classroomTextView.setText("Classe : " + absence.getClassroom());
        holder.dateTextView.setText("Date : " + absence.getDate());
        holder.timeTextView.setText("Temps : " + absence.getTime());
        holder.agentNameTextView.setText("Absence assignée par : " + absence.getNameAgent());
    }

    @Override
    public int getItemCount() {
        return absenceList.size();
    }

    public static class AbsenceViewHolder extends RecyclerView.ViewHolder {

        TextView teacherNameTextView;
        TextView classroomTextView;
        TextView dateTextView;
        TextView timeTextView;
        TextView agentNameTextView;

        public AbsenceViewHolder(View itemView) {
            super(itemView);

            teacherNameTextView = itemView.findViewById(R.id.teacherNameTextView);
            classroomTextView = itemView.findViewById(R.id.classroomTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            agentNameTextView = itemView.findViewById(R.id.agentNameTextView);
        }
    }
}
