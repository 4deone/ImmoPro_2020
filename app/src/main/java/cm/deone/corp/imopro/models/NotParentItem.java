package cm.deone.corp.imopro.models;

import java.util.List;

public class NotParentItem {

    private String nAvatar;
    private String nName;
    private List<NotChildItem> notChildItemList;

    public NotParentItem() {
    }

    public NotParentItem(String nAvatar, String nName) {
        this.nAvatar = nAvatar;
        this.nName = nName;
    }

    public NotParentItem(String nAvatar, String nName,
                         List<NotChildItem> notChildItemList) {
        this.nAvatar = nAvatar;
        this.nName = nName;
        this.notChildItemList = notChildItemList;
    }

    public String getnAvatar() {
        return nAvatar;
    }

    public void setnAvatar(String nAvatar) {
        this.nAvatar = nAvatar;
    }

    public String getnName() {
        return nName;
    }

    public void setnName(String nName) {
        this.nName = nName;
    }

    public List<NotChildItem> getNotChildItemList() {
        return notChildItemList;
    }

    public void setNotChildItemList(List<NotChildItem> notChildItemList) {
        this.notChildItemList = notChildItemList;
    }
}
