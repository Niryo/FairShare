package share.fair.fairshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import share.fair.fairshare.activities.GroupActivity;

/**
 * Created by niryo on 27/02/16.
 */
public class CloudCommunication {

    private static String ADDRESS = "https://fairshare.firebaseio.com/";
    private static CloudCommunication instance = null;
    private Firebase groupActionsRef;
    private Firebase subscribersRef;
    private FairShareGroup group;
    private boolean syncLock = false;
    private CustomChildEventListener childEventListener = new CustomChildEventListener();
    private boolean isAlreadyFetchingData = false;
    private Query activeQuery = null;

    private CloudCommunication() {
        final Firebase ref = new Firebase(ADDRESS);
    }

    public static CloudCommunication getInstance() {
        if (instance == null) {
            instance = new CloudCommunication();
        }
        return instance;
    }

    public static void queryVersion(final CloudCallback callback) {
        Firebase versionRef = new Firebase(ADDRESS + "VERSION");
        versionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callback.done(null, dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                callback.done(firebaseError, null);
            }
        });
    }

    public void setCurrentGroup(FairShareGroup group) {
        this.group = group;
        stopFetchingData();
        this.groupActionsRef = new Firebase(ADDRESS + group.getCloudGroupKey()).child("Actions");
        this.subscribersRef = new Firebase(ADDRESS + group.getCloudGroupKey()).child("subscribers");
        stopFetchingData();
    }

    public void stopFetchingData() {
        if(activeQuery!=null){
            activeQuery.removeEventListener(this.childEventListener);
        }
        this.isAlreadyFetchingData = false;
    }

    public void sendUserAddedCommand(User user, final CloudCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("userName", user.getUserName());
        data.put("userBalance", Double.toString(user.getBalance()));
        data.put("action", "USER_ADDED");
        data.put("installationId", this.group.getInstallationId());
        data.put("timeStamp", ServerValue.TIMESTAMP);
        groupActionsRef.push().setValue(data, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                callback.done(firebaseError, null);
            }
        });
    }

    public void getSubscribers(final PushService.SubscribersCallback callback) {
        this.subscribersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> subscribers = new ArrayList<String>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.w("custom", snapshot.getKey());
                    subscribers.add(snapshot.getKey());
                }
                callback.processSubscribers(subscribers);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                callback.processSubscribers(null);
            }
        });
    }

    public void subscribe(final Context context) {
        final SharedPreferences settings = context.getSharedPreferences("MAIN_PREFERENCES", 0);
        String gcmToken = settings.getString(RegistrationIntentService.GCM_TOKEN, "");
        if (gcmToken.equals("")) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(gcmToken, true);
        this.subscribersRef.updateChildren(data, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    System.out.println("Data could not be saved. " + firebaseError.getMessage());
                } else {
                    System.out.println("Data saved successfully.");
                    final SharedPreferences settings = context.getSharedPreferences(group.getCloudGroupKey(), 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("IS_SUBSCRIBED", true);
                    editor.commit();
                }
            }
        });
    }

    public void unsubscribe(final Context context) {
        final SharedPreferences settings = context.getSharedPreferences("MAIN_PREFERENCES", 0);
        String gcmToken = settings.getString(RegistrationIntentService.GCM_TOKEN, "");
        if (gcmToken.equals("")) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put(gcmToken, null);
        this.subscribersRef.updateChildren(data, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    System.out.println("Data could not be saved. " + firebaseError.getMessage());
                } else {
                    System.out.println("Data saved successfully.");
                }
            }
        });
    }


    public void sendRemoveUserCommand(User user, final CloudCallback callback) {
        final Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("userName", user.getUserName());
        data.put("action", "USER_REMOVED");
        data.put("installationId", this.group.getInstallationId());
        data.put("timeStamp", ServerValue.TIMESTAMP);
        groupActionsRef.push().setValue(data, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                callback.done(firebaseError, null);
            }
        });
    }

    public void sendAction(Action action, final CloudCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", "NEW_ACTION");
        data.put("jsonAction", action.toJSON().toString());
        data.put("actionId", action.getActionId());
        data.put("installationId", this.group.getInstallationId());
        data.put("timeStamp", ServerValue.TIMESTAMP);
        groupActionsRef.push().setValue(data, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                callback.done(firebaseError, null);
            }
        });
    }

    public void sendUnEditableCommand(Action action, final CloudCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", "UNEDITED_ACTION");
        data.put("actionId", action.getActionId());
        data.put("installationId", this.group.getInstallationId());
        data.put("timeStamp", ServerValue.TIMESTAMP);
        groupActionsRef.push().setValue(data, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                callback.done(firebaseError, null);
            }
        });
    }

    public void fetchData() {
        if (isAlreadyFetchingData) {
            Log.w("custom", "synclock activated");
            return;
        }
        //syncLock = true;
        isAlreadyFetchingData = true;
        this.activeQuery = groupActionsRef.orderByChild("timeStamp").startAt(this.group.getLastSyncTime() + 1);
        Log.w("custom", "groupref is: " + groupActionsRef);
        Log.w("custom", "last sync is: " + group.getLastSyncTime());

//        Query query = groupActionsRef.orderByChild("timeStamp").startAt(0 + 1);
//         query.addChildEventListener(new ChildEventListener() {
//             @Override
//             public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
//                 Log.w("custom", "data has been fetched!");
//                 Log.w("custom", dataSnapshot.toString());
//
//                 ArrayList<String> userIdToIgnore = new ArrayList<>();
//                 List<User> userToSave = new ArrayList<>();
//                 long lastUserSyncCopy = group.getLastSyncTime(); //we will save the changes to the real value only at the end.
//                 boolean dirty = false; //will tell us if we need to save the group.
//                 for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                     String installationId = (String) snapshot.child("installationId").getValue();
//                     Log.w("custom", "time stamp: " + (long) snapshot.child("timeStamp").getValue());
//                     lastUserSyncCopy = Math.max((Long) snapshot.child("timeStamp").getValue(), lastUserSyncCopy);
//                     if (installationId.equals(group.getInstallationId()) || lastUserSyncCopy > (Long) snapshot.child("timeStamp").getValue()) {
//                         continue;
//                     }
//                     final ArrayList<User> usersCopy = new ArrayList<>(group.getUsers());
//                     String action = (String) snapshot.child("action").getValue();
//
//                     //case we need to make an action unediatable:
//                     if (action.equals("UNEDITED_ACTION")) {
//                         Hashtable<String, Action> actionsIdTable = group.getActionsIdTable();
//                         String actionId = (String) snapshot.child("actionId").getValue();
//                         if (actionsIdTable.containsKey(actionId)) {
//                             actionsIdTable.get(actionId).makeUneditable(false);
//                         }
//                     }
//
//                     //case we need to add new action:
//                     if (action.equals("NEW_ACTION")) {
//                         Hashtable<String, Action> actionsIdTable = group.getActionsIdTable(); //we will use this table to find specific actionIds
//                         JSONObject jsonObject = null;
//                         try {
//                             jsonObject = new JSONObject((String) snapshot.child("jsonAction").getValue());
//                         } catch (JSONException e) {
//                             e.printStackTrace();
//                         }
//                         String actionId = (String) snapshot.child("actionId").getValue();
//                         if (jsonObject != null && !actionsIdTable.containsKey(actionId)) { //check if we don't already have this action
//                             Action newAction = new Action(jsonObject);
//                             actionsIdTable.put(actionId, newAction);
//                             newAction.setGroup(group.getCloudGroupKey(), group.getGroupName(), group.getInstallationId());
//                             group.actions.add(newAction);
//                             group.consumeAction(newAction);
//                             newAction.save();
//                         }
//                     }
//
//                     //case we need to add user:
//                     if (action.equals("USER_ADDED")) {
//                         String userId = (String) snapshot.child("userId").getValue();
//                         //check that user is not already exist:
//                         boolean isUserExist = false;
//                         for (User user : usersCopy) {
//                             if (user.getUserId().equals(userId)) {
//                                 isUserExist = true;
//                                 break;
//                             }
//                         }
//                         if (!isUserExist) {
//                             //check if we need to ignore this user because of previous remove-command:
//                             if (userIdToIgnore.contains(userId)) {
//                                 continue;
//                             }
//
//                             String userName = (String) snapshot.child("userName").getValue();
//                             User newUser = new User(userName, userId, 0, false);
//                             newUser.setBelongingGroupId(group.getCloudGroupKey());
//                             //we add the user to the save list:
//                             userToSave.add(newUser);
//                             group.users.add(newUser);
//                             dirty = true;
//                         }
//                     }
//                     //case we need to remove user:
//                     if (action.equals("USER_REMOVED")) {
//                         String userId = (String) snapshot.child("userId").getValue();
//                         boolean success = false;
//                         //we search for the user in the user list:
//                         for (User user : usersCopy) {
//                             if (user.getUserId().equals(userId)) {
//                                 group.users.remove(user);
//                                 if (userToSave.contains(user)) {
//                                     userToSave.remove(user); //no need to delete because user hasn't been save yet.
//                                 } else {
//                                     user.delete();
//                                 }
//                                 //if we try to remove a user with non-zero balance, he will come back as a ghost:
//                                 if (Math.abs(user.getBalance()) != 0) {
//                                     user = new User(user.getUserName(), user.getUserId(), user.getBalance(), true);
//                                     group.addGhostUser(user);
//                                 }
//
//                                 success = true;
//                                 dirty = true;
//                                 break;
//                             }
//                         }
//                         //if we weren't able to remove the user, we add him to the ignore list.
//                         //(we probably got the remove command before the add command).
//                         if (!success) {
//                             userIdToIgnore.add(userId);
//                         }
//                     }
//                 }
//                 //save the users:
//                 for (User user : userToSave) {
//                     user.save();
//                 }
//
//                 //notify groupActivity to update the user list:
//                 if (dirty && group.groupActivity != null) {
//                     group.groupActivity.messageHandler(GroupActivity.NOTIFY_USER_CHANGE, null);
//                 }
//                 //save lasySyncTime:
//                 Log.w("custom", "new sync time is: " + lastUserSyncCopy);
//                 group.lastSyncTime = lastUserSyncCopy; //only after saving the users we update lastSync
//                 group.save();
//                 syncLock = false;
//             }
//
//             @Override
//             public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//             }
//
//             @Override
//             public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//             }
//
//             @Override
//             public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//             }
//
//             @Override
//             public void onCancelled(FirebaseError firebaseError) {
//                 Log.w("custom", "can't fetch data from server");
//             }
//         });
        this.activeQuery.removeEventListener(this.childEventListener);
        this.activeQuery.addChildEventListener(this.childEventListener);
    }

    public interface CloudCallback {
        public void done(FirebaseError firebaseError, DataSnapshot dataSnapshot);
    }

    private class CustomChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
            Log.w("custom", "data has been fetched!");
            Log.w("custom", snapshot.toString());

            ArrayList<String> userIdToIgnore = new ArrayList<>();
            List<User> userToSave = new ArrayList<>();
            long lastUserSyncCopy = group.getLastSyncTime(); //we will save the changes to the real value only at the end.
            boolean dirty = false; //will tell us if we need to save the group.
            String installationId = (String) snapshot.child("installationId").getValue();
            Log.w("custom", "time stamp: " + (long) snapshot.child("timeStamp").getValue());
            lastUserSyncCopy = Math.max((Long) snapshot.child("timeStamp").getValue(), lastUserSyncCopy);
            if (!installationId.equals(group.getInstallationId())) {
                final ArrayList<User> usersCopy = new ArrayList<>(group.getUsers());
                String action = (String) snapshot.child("action").getValue();

                //case we need to make an action unediatable:
                if (action.equals("UNEDITED_ACTION")) {
                    Hashtable<String, Action> actionsIdTable = group.getActionsIdTable();
                    String actionId = (String) snapshot.child("actionId").getValue();
                    if (actionsIdTable.containsKey(actionId)) {
                        actionsIdTable.get(actionId).makeUneditable(false);
                    }
                }

                //case we need to add new action:
                if (action.equals("NEW_ACTION")) {
                    Hashtable<String, Action> actionsIdTable = group.getActionsIdTable(); //we will use this table to find specific actionIds
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject((String) snapshot.child("jsonAction").getValue());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String actionId = (String) snapshot.child("actionId").getValue();
                    if (jsonObject != null && !actionsIdTable.containsKey(actionId)) { //check if we don't already have this action
                        Action newAction = new Action(jsonObject);
                        actionsIdTable.put(actionId, newAction);
                        newAction.setGroup(group.getCloudGroupKey(), group.getGroupName(), group.getInstallationId());
                        group.actions.add(newAction);
                        group.consumeAction(newAction);
                        newAction.save();
                    }
                }

                //case we need to add user:
                if (action.equals("USER_ADDED")) {
                    String userId = (String) snapshot.child("userId").getValue();
                    //check that user is not already exist:
                    boolean isUserExist = false;
                    for (User user : usersCopy) {
                        if (user.getUserId().equals(userId)) {
                            isUserExist = true;
                            break;
                        }
                    }
                    if (!isUserExist) {
                        //check if we need to ignore this user because of previous remove-command:
                        if (!userIdToIgnore.contains(userId)) {
                            String userName = (String) snapshot.child("userName").getValue();
                            User newUser = new User(userName, userId, 0, false);
                            newUser.setBelongingGroupId(group.getCloudGroupKey());
                            //we add the user to the save list:
                            userToSave.add(newUser);
                            group.users.add(newUser);
                            dirty = true;
                        }

                    }
                }
                //case we need to remove user:
                if (action.equals("USER_REMOVED")) {
                    String userId = (String) snapshot.child("userId").getValue();
                    boolean success = false;
                    //we search for the user in the user list:
                    for (User user : usersCopy) {
                        if (user.getUserId().equals(userId)) {
                            group.users.remove(user);
                            if (userToSave.contains(user)) {
                                userToSave.remove(user); //no need to delete because user hasn't been save yet.
                            } else {
                                user.delete();
                            }
                            //if we try to remove a user with non-zero balance, he will come back as a ghost:
                            if (Math.abs(user.getBalance()) != 0) {
                                user = new User(user.getUserName(), user.getUserId(), user.getBalance(), true);
                                group.addGhostUser(user);
                            }

                            success = true;
                            dirty = true;
                            break;
                        }
                    }
                    //if we weren't able to remove the user, we add him to the ignore list.
                    //(we probably got the remove command before the add command).
                    if (!success) {
                        userIdToIgnore.add(userId);
                    }
                }
            }

            //save the users:
            for (User user : userToSave) {
                user.save();
            }

            //notify groupActivity to update the user list:
            if (dirty && group.groupActivity != null) {
                group.groupActivity.messageHandler(GroupActivity.NOTIFY_USER_CHANGE, null);
            }
            //save lasySyncTime:
            Log.w("custom", "new sync time is: " + lastUserSyncCopy);
            group.lastSyncTime = lastUserSyncCopy; //only after saving the users we update lastSync
            group.save();
            syncLock = false;
            Log.w("custom", "synclock set to false");

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.w("custom", "can't fetch data from server");
        }
    }


}
