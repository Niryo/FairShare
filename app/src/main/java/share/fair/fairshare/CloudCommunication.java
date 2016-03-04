package share.fair.fairshare;

import android.util.Log;

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

    private static CloudCommunication instance = null;
    private String ADDRESS = "https://fairshare.firebaseio.com/";
    private Firebase groupActionsRef;
    private FairShareGroup group;
    private boolean syncLock = false;

    private CloudCommunication() {
        final Firebase ref = new Firebase(ADDRESS);

//         ref.addListenerForSingleValueEvent(new ValueEventListener() {
//             @Override
//             public void onDataChange(DataSnapshot dataSnapshot) {
//                 Log.w("custom", dataSnapshot.getValue().toString());
//             }
//
//             @Override
//             public void onCancelled(FirebaseError firebaseError) {
//                 Log.w("custom", "error reading firebase");
//                 Log.w("custom", firebaseError.getMessage());
//                 Log.w("custom", firebaseError.getDetails());
//
//             }
//         });
    }

    public static CloudCommunication getInstance() {
        if (instance == null) {
            instance = new CloudCommunication();
        }
        return instance;
    }

    public void setCurrentGroup(FairShareGroup group) {
        this.group = group;
        this.groupActionsRef = new Firebase(ADDRESS + group.getCloudGroupKey()).child("Actions");
    }

//    public void createNewGroup(String groupKey){
//        Firebase ref = new Firebase(ADDRESS);
//        Map<String,String> emptyGroup
//        ref.child(groupKey).setValue();
//    }

    public void sendUserAddedCommand(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("userName", user.getUserName());
        data.put("userBalance", Double.toString(user.getBalance()));
        data.put("action", "USER_ADDED");
        data.put("installationId", this.group.getInstallationId());
        data.put("timeStamp", ServerValue.TIMESTAMP);
        Firebase test = groupActionsRef.push();
        test.setValue(data);
    }

    public void sendRemoveUserCommand(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("userName", user.getUserName());
        data.put("action", "USER_REMOVED");
        data.put("installationId", this.group.getInstallationId());
        data.put("timeStamp", ServerValue.TIMESTAMP);
        groupActionsRef.push().setValue(data);
    }

    public void sendAction(Action action) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", "NEW_ACTION");
        data.put("jsonAction", action.toJSON().toString());
        data.put("actionId", action.getActionId());
        data.put("installationId", this.group.getInstallationId());
        data.put("timeStamp", ServerValue.TIMESTAMP);
        groupActionsRef.push().setValue(data);
    }

    public void sendUnEditableCommand(Action action) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", "UNEDITED_ACTION");
        data.put("actionId", action.getActionId());
        data.put("installationId", this.group.getInstallationId());
        data.put("timeStamp", ServerValue.TIMESTAMP);
        groupActionsRef.push().setValue(data);
    }

    public void fetchData() {
        if (syncLock) {
            return;
        }
        syncLock = true;
        Query query = groupActionsRef.orderByChild("timeStamp").startAt(this.group.getLastSyncTime() + 1);
//        Query query = groupActionsRef.orderByChild("timeStamp").startAt(0 + 1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> userIdToIgnore = new ArrayList<>();
                List<User> userToSave = new ArrayList<>();
                long lastUserSyncCopy = group.getLastSyncTime(); //we will save the changes to the real value only at the end.
                boolean dirty = false; //will tell us if we need to save the group.

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String installationId= (String) snapshot.child("installationId").getValue();
                    if(installationId.equals(group.getInstallationId())){
                        continue;
                    }
                    final ArrayList<User> usersCopy = new ArrayList<>(group.getUsers());
                    String action = (String) snapshot.child("action").getValue();
                    lastUserSyncCopy = Math.max((Long) snapshot.child("timeStamp").getValue(), lastUserSyncCopy);
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
                            if (userIdToIgnore.contains(userId)) {
                                continue;
                            }

                            String userName = (String) snapshot.child("userName").getValue();
                            User newUser = new User(userName, userId, 0, false);
                            newUser.setBelongingGroupId(group.getCloudGroupKey());
                            //we add the user to the save list:
                            userToSave.add(newUser);
                            group.users.add(newUser);
                            dirty = true;
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
                group.lastSyncTime = lastUserSyncCopy; //only after saving the users we update lastSync
                group.save();
                syncLock = false;

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.w("custom", "can't fetch data from server");
            }
        });

    }
    //todo: add an on success callback that will send a push notification

}
