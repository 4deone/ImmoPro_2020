package cm.deone.corp.imopro.models;

public class Post implements Comparable<Post>{
    private String pId;
    private String pCover;
    private String pDate;
    private String pTitre;
    private String pDescription;
    private String pDisponible;
    private String pNote;
    private String pNLikes;
    private String pNFavories;
    private String pNVues;
    private String pNComments;
    private String pNSignals;
    private String pNShares;
    private String pCreator;
    private String pPublicOrPrivate;
    private String pCountryName;
    private String pSubLocality;
    private String pLocality;
    private String uName;
    private String uAvatar;

    public Post() {
    }

    public Post(String pId, String pCover, String pDate,
                String pTitre, String pDescription,
                String pDisponible, String pNote,
                String pNLikes, String pNFavories,
                String pNVues, String pNComments,
                String pNSignals, String pNShares,
                String pCreator, String pPublicOrPrivate,
                String pCountryName, String pSubLocality,
                String pLocality, String uName, String uAvatar) {
        this.pId = pId;
        this.pCover = pCover;
        this.pDate = pDate;
        this.pTitre = pTitre;
        this.pDescription = pDescription;
        this.pDisponible = pDisponible;
        this.pNote = pNote;
        this.pNLikes = pNLikes;
        this.pNFavories = pNFavories;
        this.pNVues = pNVues;
        this.pNComments = pNComments;
        this.pNSignals = pNSignals;
        this.pNShares = pNShares;
        this.pCreator = pCreator;
        this.pPublicOrPrivate = pPublicOrPrivate;
        this.pCountryName = pCountryName;
        this.pSubLocality = pSubLocality;
        this.pLocality = pLocality;
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

    public String getpCountryName() {
        return pCountryName;
    }

    public void setpCountryName(String pCountryName) {
        this.pCountryName = pCountryName;
    }

    public String getpSubLocality() {
        return pSubLocality;
    }

    public void setpSubLocality(String pSubLocality) {
        this.pSubLocality = pSubLocality;
    }

    public String getpLocality() {
        return pLocality;
    }

    public void setpLocality(String pLocality) {
        this.pLocality = pLocality;
    }

    public String getpDisponible() {
        return pDisponible;
    }

    public void setpDisponible(String pDisponible) {
        this.pDisponible = pDisponible;
    }

    @Override
    public int compareTo(Post post) {
        return (Integer.parseInt(post.pNVues) - Integer.parseInt(this.pNVues));
    }
}
