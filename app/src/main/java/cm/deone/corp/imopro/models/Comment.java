package cm.deone.corp.imopro.models;

public class Comment {
    private String cId;
    private String cCreator;
    private String cMessage;
    private String cDate;

    public Comment() {
    }

    public Comment(String cId, String cCreator, String cMessage, String cDate) {
        this.cId = cId;
        this.cCreator = cCreator;
        this.cMessage = cMessage;
        this.cDate = cDate;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getcCreator() {
        return cCreator;
    }

    public void setcCreator(String cCreator) {
        this.cCreator = cCreator;
    }

    public String getcMessage() {
        return cMessage;
    }

    public void setcMessage(String cMessage) {
        this.cMessage = cMessage;
    }

    public String getcDate() {
        return cDate;
    }

    public void setcDate(String cDate) {
        this.cDate = cDate;
    }
}
