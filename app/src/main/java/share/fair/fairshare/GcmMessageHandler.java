package share.fair.fairshare;

import com.google.android.gms.gcm.GcmListenerService;

        import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
        import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import share.fair.fairshare.activities.App;
import share.fair.fairshare.activities.GroupActivity;

public class GcmMessageHandler extends GcmListenerService {
    public static int NOTIFICATION_ID = 0;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.w("custom", "data received!");
        Context context = getBaseContext();
        //check if there is a running instance of GroupActivity:
        GroupActivity activity = ((App) context.getApplicationContext()).activity;
        if (activity == null) {//FareShare is closed so we need to create a notification
            if (data.getString("alertType").equals("ACTION_CHANGE")) {
                //check if we are the ones that created this change:
                List<FairShareGroup.GroupNameRecord> groupNameRecords = FairShareGroup.getSavedGroupNames();
                for (FairShareGroup.GroupNameRecord groupName : groupNameRecords) {
                    if (data.getString("installationId").equals(groupName.getInstallationId())) {
                        return;
                    }
                }
                List<Alert.NotifiedId> notifiedIds = (List<Alert.NotifiedId>) Alert.NotifiedId.listAll(Alert.NotifiedId.class);
                //we now iterate through the notified ID's and check if the data contains a change on one or more of the notified users:
                for (Alert.NotifiedId notifiedId : notifiedIds) {
                    if (data.getString(notifiedId.userId)!=null) {
                        String groupName = data.getString("groupName");
                        String groupId = data.getString("groupId");
                        addNotificationToNotificationCenter(groupName, groupId);
                        break;
                    }
                }
            }


        } else { //we don't need to create notification because the app is open
            activity.sync();
        }

    }

    /**
     * Add notification to notification center
     *
     * @param groupName
     * @param groupId
     */
    private void addNotificationToNotificationCenter(String groupName, String groupId) {
        Context context =getBaseContext();
        Uri defaultNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v7.app.NotificationCompat.Builder mBuilder = new android.support.v7.app.NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Balance changed")
                .setContentText("Your balance in group " + groupName + " has changed.")
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(defaultNotificationSound);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, GroupActivity.class);
        resultIntent.putExtra("groupId", groupId);


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
                        PendingIntent.FLAG_ONE_SHOT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}