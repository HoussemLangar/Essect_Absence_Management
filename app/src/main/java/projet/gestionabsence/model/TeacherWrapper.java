package projet.gestionabsence.model;

import java.io.Serializable;

public class TeacherWrapper implements Serializable {
    private Teacher teacher;

    public TeacherWrapper(Teacher teacher) {
        this.teacher = teacher;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
}
