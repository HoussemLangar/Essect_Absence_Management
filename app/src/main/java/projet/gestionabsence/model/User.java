package projet.gestionabsence.model;

public class User {
    private String uid;
    private String userType;
    private String email;

    public User(String uid, String userType, String email) {
        this.uid = uid;
        this.userType = userType;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public String getUserType() {
        return userType;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
