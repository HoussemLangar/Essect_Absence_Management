package projet.gestionabsence.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import projet.gestionabsence.R;
import projet.gestionabsence.model.Teacher;

public class TeacherFragment extends Fragment {

    private RecyclerView teachersRecyclerView;
    private TeacherAbsenceAdapter teacherAbsenceAdapter;
    private List<Teacher> teacherList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher, container, false);

        teachersRecyclerView = view.findViewById(R.id.absenceHistoryRecyclerView);
        teachersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        teacherList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("teachers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot) {
                                String name = document.getString("name");
                                Object nbAbsenceObj = document.get("nb_absence");
                                int nbAbsence = 0;

                                if (nbAbsenceObj instanceof Number) {
                                    nbAbsence = ((Number) nbAbsenceObj).intValue();
                                }


                                teacherList.add(new Teacher(name, nbAbsence));
                            }

                            teacherAbsenceAdapter = new TeacherAbsenceAdapter(teacherList);
                            teachersRecyclerView.setAdapter(teacherAbsenceAdapter);

                            teacherAbsenceAdapter.setOnItemClickListener(teacher -> {
                                AbsenceHistoryFragment fragment = new AbsenceHistoryFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("teacherName", teacher.getName());
                                fragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), "Erreur lors de la récupération des données", Toast.LENGTH_SHORT).show();
                    }
                });

        FloatingActionButton addAbsenceButton = view.findViewById(R.id.addAbsenceButton);
        addAbsenceButton.setOnClickListener(v -> {
            AddAbsenceDialogFragment dialogFragment = AddAbsenceDialogFragment.newInstance();
            dialogFragment.show(getParentFragmentManager(), "AddAbsenceDialogFragment");
        });

        return view;
    }
}
