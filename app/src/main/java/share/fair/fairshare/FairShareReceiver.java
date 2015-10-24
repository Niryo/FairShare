package share.fair.fairshare;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Nir on 24/10/2015.
 */
public class FairShareReceiver extends ParsePushBroadcastReceiver {


    @Override
    protected void onPushReceive(Context context, Intent intent) {
        SharedPreferences settings = context.getSharedPreferences("MAIN_PREFERENCES", 0);
        String ownerId = settings.getString("id", "");


        ParseAnalytics.trackAppOpenedInBackground(intent);
        String rawData= intent.getExtras().getString("com.parse.Data");

        try {
            JSONObject data = new JSONObject(rawData);
            data= new JSONObject(data.getString("alert"));
            if(data.getString("creatorId").equals(ownerId)){
                return; //we created this changed so we don't need to be notified
            }else{
                List<Alert.NotifiedId> notifiedIds = (List<Alert.NotifiedId>) Alert.NotifiedId.listAll(Alert.NotifiedId.class);
                boolean isNotifiedId=false;
                for(Alert.NotifiedId notifiedId: notifiedIds){
                    if(data.has(notifiedId.userId)){
                        isNotifiedId=true;
                        break;
                    }
                }
                if(isNotifiedId){
                String groupName= data.getString("groupName");
                String groupId = data.getString("groupId");
                sendNotification(context, intent, groupName,groupId);
                }
               // Toast.makeText(context, "got new message", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void sendNotification(Context context,Intent recivedIntent,String groupName,String groupId){
        Uri defaultNotificationSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(getSmallIconId(context,recivedIntent))
                        .setContentTitle("Balance changed")
                        .setContentText("Your balance in group "+groupName+ " has changed.")
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
        .setSound(defaultNotificationSound);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context,GroupActivity.class);
        recivedIntent.putExtra("groupId",Long.parseLong(groupId));

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(GroupActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }

}
