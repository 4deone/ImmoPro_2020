package cm.deone.corp.imopro.models;

public class Signaler {
    private String sId;
    private String sDate;
    private String sMessage;
    private String uAvatar;
    private String uName;

    public Signaler() {
    }

    public Signaler(String sId, String sDate, String sMessage, String uAvatar, String uName) {
        this.sId = sId;
        this.sDate = sDate;
        this.sMessage = sMessage;
        this.uAvatar = uAvatar;
        this.uName = uName;
    }

    public String getsId() {
        return sId;
    }

    public void setsId(String sId) {
        this.sId = sId;
    }

    public String getsDate() {
        return sDate;
    }

    public void setsDate(String sDate) {
        this.sDate = sDate;
    }

    public String getsMessage() {
        return sMessage;
    }

    public void setsMessage(String sMessage) {
        this.sMessage = sMessage;
    }

    public String getuAvatar() {
        return uAvatar;
    }

    public void setuAvatar(String uAvatar) {
        this.uAvatar = uAvatar;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }
}
