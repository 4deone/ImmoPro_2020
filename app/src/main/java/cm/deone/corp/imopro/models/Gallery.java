package cm.deone.corp.imopro.models;

public class Gallery {
    private String gId;
    private String gImage;
    private String gDescription;
    private String gDate;

    public Gallery() {
    }

    public Gallery(String gImage, String gDescription) {
        this.gImage = gImage;
        this.gDescription = gDescription;
    }

    public Gallery(String gId, String gImage, String gDescription, String gDate) {
        this.gId = gId;
        this.gImage = gImage;
        this.gDescription = gDescription;
        this.gDate = gDate;
    }

    public String getgId() {
        return gId;
    }

    public void setgId(String gId) {
        this.gId = gId;
    }

    public String getgImage() {
        return gImage;
    }

    public void setgImage(String gImage) {
        this.gImage = gImage;
    }

    public String getgDescription() {
        return gDescription;
    }

    public void setgDescription(String gDescription) {
        this.gDescription = gDescription;
    }

    public String getgDate() {
        return gDate;
    }

    public void setgDate(String gDate) {
        this.gDate = gDate;
    }
}
