package share.fair.fairshare;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import share.fair.fairshare.activities.App;
import share.fair.fairshare.activities.GroupActivity;

/**
 * A receiver that handle Parse push notification
 */
public class FairShareReceiver extends ParsePushBroadcastReceiver {
    public static int NOTIFICATION_ID = 0; //the id of the notification in the notification center

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        try {
            ParseAnalytics.trackAppOpenedInBackground(intent);
            String rawData = intent.getExtras().getString("com.parse.Data");
            JSONObject data = new JSONObject(rawData);
            data = new JSONObject(data.getString("alert"));
            //check if there is a running instance of GroupActivity:
            GroupActivity activity = ((App) context.getApplicationContext()).activity;
            if (activity == null) {//FareShare is closed so we need to create a notification
                if (data.getString("alertType").equals("ACTION_CHANGE")) {
                    List<Alert.NotifiedId> notifiedIds = (List<Alert.NotifiedId>) Alert.NotifiedId.listAll(Alert.NotifiedId.class);
                    //we now iterate through the notified ID's and check if the data contains a change on one or more of the notified users:
                    for (Alert.NotifiedId notifiedId : notifiedIds) {
                        if (data.has(notifiedId.userId)) {
                            String groupName = data.getString("groupName");
                            String groupId = data.getString("groupId");
                            addNotificationToNotificationCenter(context, intent, groupName, groupId);
                            break;
                        }
                    }
                }


            } else { //we don't need to create notification because the app is open
                activity.sync();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Add notification to notification center
     *
     * @param context        context
     * @param receivedIntent
     * @param groupName
     * @param groupId
     */
    private void addNotificationToNotificationCenter(Context context, Intent receivedIntent, String groupName, String groupId) {
        Uri defaultNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(getSmallIconId(context, receivedIntent))
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
