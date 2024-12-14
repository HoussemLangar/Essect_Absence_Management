package projet.gestionabsence.model;

public class Absence {
    private String teacherName;
    private String date;
    private String time;
    private String classroom;
    private String nameAgent;


    public Absence(String teacherName, String classroom, String date, String time, String nameAgent ) {
        this.teacherName = teacherName;
        this.date = date;
        this.time = time;
        this.classroom = classroom;
        this.nameAgent = nameAgent;

    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public void setNameAgent(String nameAgent) {
        this.nameAgent = nameAgent;
    }

    public String getNameAgent() {
        return nameAgent ;
    }
}
