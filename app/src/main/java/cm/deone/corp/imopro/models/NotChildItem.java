package cm.deone.corp.imopro.models;

public class NotChildItem {

    private String nActivite;
    private String nMessage;
    private String nTime;

    public NotChildItem() {
    }

    public NotChildItem(String nActivite, String nMessage, String nTime) {
        this.nActivite = nActivite;
        this.nMessage = nMessage;
        this.nTime = nTime;
    }

    public String getnActivite() {
        return nActivite;
    }

    public void setnActivite(String nActivite) {
        this.nActivite = nActivite;
    }

    public String getnMessage() {
        return nMessage;
    }

    public void setnMessage(String nMessage) {
        this.nMessage = nMessage;
    }

    public String getnTime() {
        return nTime;
    }

    public void setnTime(String nTime) {
        this.nTime = nTime;
    }
}
