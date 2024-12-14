package projet.gestionabsence.model;

import com.google.firebase.firestore.DocumentId;

public class Teacher {

    @DocumentId
    private String id;
    private String name;
    private int nbAbsence;
    private String post;

    public Teacher(String name, int nbAbsence) {
        this.name = name;
        this.nbAbsence = nbAbsence;
    }

    public Teacher(String name, String post, String documentId) {
    }

    public Teacher(String name, String post) {
        this.name = name;
        this.post = post;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNbAbsence() {
        return nbAbsence;
    }

    public void setNbAbsence(int nbAbsence) {
        this.nbAbsence = nbAbsence;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }
}
