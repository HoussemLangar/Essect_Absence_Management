package projet.gestionabsence.model;

import com.google.firebase.firestore.FieldValue;

public class FirestoreNotification {
    private String title;
    private String message;
    private FieldValue timestamp;

    public FirestoreNotification(String title, String message, FieldValue timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FieldValue getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(FieldValue timestamp) {
        this.timestamp = timestamp;
    }
}
