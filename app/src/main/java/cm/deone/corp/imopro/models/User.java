package cm.deone.corp.imopro.models;

public class User {
    private String uId;
    private String uAvatar;
    private String uEmail;
    private String uDate;
    private String uName;
    private String uRole;
    private String uPhone;
    private String uDevise;

    public User() {
    }

    public User(String uId, String uAvatar, String uEmail, String uDate, String uName, String uRole, String uPhone, String uDevise) {
        this.uId = uId;
        this.uAvatar = uAvatar;
        this.uEmail = uEmail;
        this.uDate = uDate;
        this.uName = uName;
        this.uRole = uRole;
        this.uPhone = uPhone;
        this.uDevise = uDevise;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuAvatar() {
        return uAvatar;
    }

    public void setuAvatar(String uAvatar) {
        this.uAvatar = uAvatar;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDate() {
        return uDate;
    }

    public void setuDate(String uDate) {
        this.uDate = uDate;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuRole() {
        return uRole;
    }

    public void setuRole(String uRole) {
        this.uRole = uRole;
    }

    public String getuPhone() {
        return uPhone;
    }

    public void setuPhone(String uPhone) {
        this.uPhone = uPhone;
    }

    public String getuDevise() {
        return uDevise;
    }

    public void setuDevise(String uDevise) {
        this.uDevise = uDevise;
    }
}
