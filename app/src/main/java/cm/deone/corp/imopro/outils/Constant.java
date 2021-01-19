package cm.deone.corp.imopro.outils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import cm.deone.corp.imopro.CreateGalleryActivity;
import cm.deone.corp.imopro.HomeActivity;
import cm.deone.corp.imopro.PostActivity;
import cm.deone.corp.imopro.R;
import cm.deone.corp.imopro.SettingsActivity;
import cm.deone.corp.imopro.adapter.CommentAdaptor;
import cm.deone.corp.imopro.adapter.SignalerAdaptor;
import cm.deone.corp.imopro.models.Comment;
import cm.deone.corp.imopro.models.Post;
import cm.deone.corp.imopro.models.Signaler;

public class Constant {

    public static final String TOPIC_POST_NOTIFICATION = "POST";
    public static final String TOPIC_COMMENT_NOTIFICATION = "COMMENT";
    public static final String TOPIC_GALLERY_NOTIFICATION = "GALLERY";
    public static final String TOPIC_GEOLOCALISTION_NOTIFICATION = "GEOLOCALISTION";

    public static final String TYPE_NOTIFICATION = "notificationType";
    public static final String TYPE_POST_NOTIFICATION = "PostNotification";
    public static final String TYPE_COMMENT_NOTIFICATION = "CommentNotification";
    public static final String TYPE_GALLERY_NOTIFICATION = "GalleryNotification";
    public static final String TYPE_CHAT_NOTIFICATION = "ChatNotification";

    public static final String DB_USER = "Users";
    public static final String DB_POST = "Posts";
    public static final String DB_COMMENT = "Comments";
    public static final String DB_GALLERY = "Gallery";
    public static final String DB_SIGNALEMENT = "Signalements";
    public static final String DB_NOTES = "Notes";
    public static final String DB_FAVORIES = "Favorites";
    public static final String DB_SHARES = "Shares";
    public static final String DB_LIKES = "Likes";
    public static final String DB_VUES = "Vues";
    public static final String DB_BLOCKED_USERS = "BlockedUsers";

    public static final int CAMERA_REQUEST_CODE = 100;
    public static final int STORAGE_REQUEST_CODE = 200;
    public static final int IMAGE_PICK_GALLERY_CODE = 300;
    public static final int IMAGE_PICK_CAMERA_CODE = 400;

    public static final int LOCATION_REQUEST_CODE = 500;

    public static final String ID = "some_id";
    public static final String NAME = "FirebaseAPP";

    public static final String ADMIN_CHANNEL_ID = "admin_channel";

    public static final String TAG_POST_ID = "pId";
    public static final String TAG_POST_CREATOR = "pCreator";

    public static final int MSG_TYPE_IN = 0;
    public static final int MSG_TYPE_OUT = 1;

    public static final String TAG = "LocationAddress";
    public static final String AES = "AES";

    public static void showCoverDialog(Context context, String pId) {
        String[] options = {"Créer une galerie", "Modifier l'image"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sélectionner une action :");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case 0 :
                        // Créer une galerie
                        Intent intent = new Intent(context, CreateGalleryActivity.class);
                        intent.putExtra("pId", pId);
                        context.startActivity(intent);
                        break;
                    case 1 :
                        // Modifier l'image
                        break;
                    default:
                }
            }
        });
        builder.create().show();
    }

    public static void showSettingsAlert(Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static void showGalleryMenu(Context context, String pId) {
        String[] options = {"Ajouter une image", "Modifier l'image", "Supprimer l'image"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sélectionner une action :");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case 0 :
                        // Ajouter une image
                        Intent intent = new Intent(context, CreateGalleryActivity.class);
                        intent.putExtra("pId", pId);
                        context.startActivity(intent);
                        break;
                    case 1 :
                        // Details du commentaire
                        break;
                    case 2 :
                        // Envoyer un message
                        break;
                    default:
                }
            }
        });
        builder.create().show();
    }

    public static void unsuscribeNotification(Context context, String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = ""+context.getResources().getString(R.string.not_receive_notification);
                        if(!task.isSuccessful()){
                            msg = ""+context.getResources().getString(R.string.subscription_failed);
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void suscribeNotification(Context context, String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = ""+context.getResources().getString(R.string.receive_notification);
                        if(!task.isSuccessful()){
                            msg = ""+context.getResources().getString(R.string.unsubscription_failed);
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static boolean checkLocationPermission(Context context) {
        boolean result = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }

    public static void bloquerLutilisateur(Context context, String cCreator, String pId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("bId", cCreator);
        hashMap.put("bDate", timestamp);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(pId).child("BlockedUsers").child(cCreator).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Utilisateur bloqué", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void debloquerLutilisateur(Context context, String cCreator, String pId) {
        FirebaseDatabase.getInstance().getReference("Posts")
                .child(pId).child("BlockedUsers").orderByChild("bId").equalTo(cCreator)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (ds.exists()){
                                ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Utilisateur débloqué", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void supprimerLeCommentaire(Context context, String cId, String pId) {
        FirebaseDatabase.getInstance().getReference("Posts")
                .child(pId).child("Comments").child(cId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Commentaire supprimé", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        FirebaseDatabase.getInstance().getReference("Posts")
                .child(pId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = "" + snapshot.child("pNComments").getValue();
                int newCommentVal = Integer.parseInt(comments) - 1;
                FirebaseDatabase.getInstance().getReference("Posts")
                        .child(pId).child("pNComments").setValue(""+newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static long convertDateToTimestamp(int day, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTimeInMillis();
    }

    public static String convertTimestampToDate(String timestamp, String formatType) {
        Calendar cal = Calendar.getInstance(Locale.FRANCE);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        return DateFormat.format(formatType, cal).toString();
    }

    public static String differenceBetweenTwoDates(long startDate, long endDate) {
        String result = null;
        //long different = endDate.getTime() - startDate.getTime();
        long different = endDate - startDate;
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        if (elapsedDays!=0 && elapsedHours!=0){
            return ""+(elapsedDays)+"j "+elapsedHours+"H "+elapsedMinutes+" min";
        }else if (elapsedDays==0 && elapsedHours!=0 && elapsedMinutes!=0){
            return ""+elapsedHours+"H "+elapsedMinutes+"min";
        }else if (elapsedDays==0 && elapsedHours==0 && elapsedMinutes!=0 && elapsedSeconds!=0){
            return ""+elapsedMinutes+"min  "+elapsedSeconds+"s";
        }else if (elapsedDays==0 && elapsedHours==0 && elapsedMinutes==0 && elapsedSeconds!=0){
            return ""+elapsedSeconds+"s";
        }else if (elapsedDays==0 && elapsedHours==0 && elapsedMinutes==0 && elapsedSeconds==0){
            return "Disponible";
        }

        return null;
        //return ""+elapsedDays+" jr, "+elapsedHours+" hr, "+elapsedMinutes+" min";
    }

    public static String encryptData(String clearData, String passKey) throws Exception{
        SecretKeySpec key = generateKey(passKey);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = cipher.doFinal(clearData.getBytes());
        return Base64.encodeToString(encVal, Base64.DEFAULT);
    }

    private static SecretKeySpec generateKey(String passKey) throws Exception{
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = passKey.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    public static String decryptData(String cryptData,  String passKey) throws Exception{
        SecretKeySpec key = generateKey(passKey);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] decodeValue = Base64.decode(cryptData, Base64.DEFAULT);
        byte[] decVal = cipher.doFinal(decodeValue);
        return new String(decVal);
    }

    private static void sendPostNotification(Context context, JSONObject notificationJo) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponse: "+ response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, ""+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAAZCzfbyE:APA91bFgMvLCJrB-y0h_8jzGXYePXl5gicO0KcfMwXWRK8rHNv81UGdhvxzD9_SGADkKFxbvXFXut6ZX7bFx6RleoFbawR7igk-t1BALJGyFrSuhSZYu9hQkAimLNOya0REEAfRe2rYl");
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }

    public static void prepareNotification(Context context, String myUID, String pId,
                                           String titre, String description,
                                           String notificationType, String notificationTopic){
        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic;
        String NOTIFICATION_TITLE = titre;
        String NOTIFICATION_DESCRIPTION = description;
        String NOTIFICATION_TYPE = notificationType;

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", myUID);
            notificationBodyJo.put("pId", pId);
            notificationBodyJo.put("pTitre", NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescription", NOTIFICATION_DESCRIPTION);

            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);
        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendPostNotification(context, notificationJo);
    }



}
