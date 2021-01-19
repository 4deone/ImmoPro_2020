package cm.deone.corp.imopro.outils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class GeocoderHandler extends Handler {
    private double latitude;
    private double longitude;
    private String pId;
    private String myUID;

    public GeocoderHandler(double latitude, double longitude, String pId, String myUID) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.pId = pId;
        this.myUID = myUID;
    }

    @Override
    public void handleMessage(@NonNull Message message) {
        String locationAddress;
        String subLocality;
        String locality;
        String countryName;
        switch (message.what) {
            case 1:
                Bundle bundle = message.getData();
                locationAddress = bundle.getString("address");
                subLocality = bundle.getString("subLocality");
                locality = bundle.getString("locality");
                countryName = bundle.getString("countryName");
                break;
            default:
                locationAddress = null;
                subLocality = null;
                locality = null;
                countryName = null;
        }
        if (myUID.equals("")){
            String timestamp = String.valueOf(System.currentTimeMillis());
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", timestamp);
            hashMap.put("latitude", ""+latitude);
            hashMap.put("longitude", ""+longitude);
            hashMap.put("adresse", locationAddress);
            DatabaseReference refGeo = FirebaseDatabase.getInstance().getReference("Posts");
            refGeo.child(pId).child("Geolocalisation").setValue(hashMap);
            //
            HashMap<String, Object> hashMapPost = new HashMap<>();
            hashMapPost.put("pSubLocality", ""+subLocality);
            hashMapPost.put("pLocality", ""+locality);
            hashMapPost.put("pCountryName", ""+countryName);
            refGeo.child(pId).updateChildren(hashMapPost);
        }else if (pId.equals("")){
            //
            DatabaseReference refGeoUsers = FirebaseDatabase.getInstance().getReference("Users");
            HashMap<String, Object> hashMapUsers = new HashMap<>();
            hashMapUsers.put("uSubLocality", ""+subLocality);
            hashMapUsers.put("uLocality", ""+locality);
            hashMapUsers.put("uCountryName", ""+countryName);
            refGeoUsers.child(myUID).updateChildren(hashMapUsers);
        }

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getMyUID() {
        return myUID;
    }

    public void setMyUID(String myUID) {
        this.myUID = myUID;
    }
}
