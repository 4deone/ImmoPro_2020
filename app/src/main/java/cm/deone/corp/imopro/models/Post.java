package cm.deone.corp.imopro.models;

public class Post {
    private String pId;
    private String pCover;
    private String pDate;
    private String pTitre;
    private String pDescription;
    private String pNote;
    private String pNLikes;
    private String pNFavories;
    private String pNVues;
    private String pNComments;
    private String pNSignals;
    private String pNShares;
    private String pCreator;
    private String pPublicOrPrivate;
    private String uName;
    private String uAvatar;

    public Post() {
    }

    public Post(String pId, String pCover,
                String pDate, String pTitre,
                String pDescription, String pNote,
                String pNLikes, String pNFavories,
                String pNVues, String pNComments,
                String pNSignals, String pNShares,
                String pCreator, String pPublicOrPrivate,
                String uName, String uAvatar) {
        this.pId = pId;
        this.pCover = pCover;
        this.pDate = pDate;
        this.pTitre = pTitre;
        this.pDescription = pDescription;
        this.pNote = pNote;
        this.pNLikes = pNLikes;
        this.pNFavories = pNFavories;
        this.pNVues = pNVues;
        this.pNComments = pNComments;
        this.pNSignals = pNSignals;
        this.pNShares = pNShares;
        this.pCreator = pCreator;
        this.pPublicOrPrivate = pPublicOrPrivate;
        this.uName = uName;
        this.uAvatar = uAvatar;
    }

    public String getpNFavories() {
        return pNFavories;
    }

    public void setpNFavories(String pNFavories) {
        this.pNFavories = pNFavories;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpCover() {
        return pCover;
    }

    public void setpCover(String pCover) {
        this.pCover = pCover;
    }

    public String getpDate() {
        return pDate;
    }

    public void setpDate(String pDate) {
        this.pDate = pDate;
    }

    public String getpTitre() {
        return pTitre;
    }

    public void setpTitre(String pTitre) {
        this.pTitre = pTitre;
    }

    public String getpDescription() {
        return pDescription;
    }

    public void setpDescription(String pDescription) {
        this.pDescription = pDescription;
    }

    public String getpNShares() {
        return pNShares;
    }

    public void setpNShares(String pNShares) {
        this.pNShares = pNShares;
    }

    public String getpNote() {
        return pNote;
    }

    public void setpNote(String pNote) {
        this.pNote = pNote;
    }

    public String getpNLikes() {
        return pNLikes;
    }

    public void setpNLikes(String pNLikes) {
        this.pNLikes = pNLikes;
    }

    public String getpNSignals() {
        return pNSignals;
    }

    public void setpNSignals(String pNSignals) {
        this.pNSignals = pNSignals;
    }

    public String getpNVues() {
        return pNVues;
    }

    public void setpNVues(String pNVues) {
        this.pNVues = pNVues;
    }

    public String getpNComments() {
        return pNComments;
    }

    public void setpNComments(String pNComments) {
        this.pNComments = pNComments;
    }

    public String getpCreator() {
        return pCreator;
    }

    public void setpCreator(String pCreator) {
        this.pCreator = pCreator;
    }

    public String getpPublicOrPrivate() {
        return pPublicOrPrivate;
    }

    public void setpPublicOrPrivate(String pPublicOrPrivate) {
        this.pPublicOrPrivate = pPublicOrPrivate;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuAvatar() {
        return uAvatar;
    }

    public void setuAvatar(String uAvatar) {
        this.uAvatar = uAvatar;
    }
}
