package share.fair.fairshare;

import android.content.Context;
import android.widget.Toast;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import share.fair.fairshare.activities.GroupActivity;


/**
 * This class represents a group of users
 */
public class FairShareGroup extends SugarRecord<FairShareGroup> {
    @Ignore
    public List<Action> actions = new ArrayList<>();
    private String groupName; //group name
    private String cloudGroupKey = ""; //for storing information on the cloud
    public long lastSyncTime; //for tracking when was the last sync wiht the cloud
    private String installationId = ""; //unique id for every installation of the app

    @Ignore
    private boolean syncLock = false; //for preventing sync clashing with multiple threads
    @Ignore
    public List<User> users = new ArrayList<>(); //list of the users of the group
    @Ignore
    public transient GroupActivity groupActivity; //for sending messages to the activity, for example when need to update the user list.
    @Ignore
    private CloudCommunication cloud;

    public FairShareGroup() { //must ve empty constructor
    }

    private FairShareGroup(String name, String cloudGroupKey, String installationId) {
        this.lastSyncTime = 0;
        this.groupName = name;
        this.cloudGroupKey = cloudGroupKey;
        this.installationId = installationId;
        actions = new ArrayList<>();
    }

    /**
     * Build new group
     *
     * @param context         application context. for using the sharedPreferences
     * @param groupName       the group's name
     * @param userNameInGroup the first user in the group
     * @return a new Group object
     */
    public static FairShareGroup groupBuilder(Context context, String groupName, String userNameInGroup) {
        String cloudGroupKey = "a" + new BigInteger(130, new SecureRandom()).toString(32); //the key must start with a letter.
        ParsePush.subscribeInBackground(cloudGroupKey);//subscribe to the group chanel
        String installationId = new BigInteger(130, new SecureRandom()).toString(32);
        FairShareGroup group = new FairShareGroup(groupName, cloudGroupKey, installationId);
        group.addUser(context, new User(userNameInGroup,null, 0.0,false));
        group.save();
        GroupNameRecord groupNameRecord = new GroupNameRecord(groupName, cloudGroupKey,installationId);
        groupNameRecord.save();
        return group;
    }

    /**
     * Returns a list with all the groups' names
     *
     * @return a list with all group names
     */
    public static List<GroupNameRecord> getSavedGroupNames() {
        return GroupNameRecord.listAll(GroupNameRecord.class);
    }

    /**
     * Loads an existing group from storage
     *
     * @param cloudGroupKey the group id
     * @return a Group object
     */
    public static FairShareGroup loadGroupFromStorage(String cloudGroupKey) {
        List<FairShareGroup> groups = FairShareGroup.find(FairShareGroup.class, "cloud_group_key = ?", cloudGroupKey);
        FairShareGroup group = groups.get(0);
        List<User> users = User.find(User.class, "belonging_group_id = ?", group.getCloudGroupKey());
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User first, User second) {
                return first.getUserName().compareTo(second.getUserName());
            }
        });
        group.setUsers(users);
        group.cloud=CloudCommunication.getInstance();
        group.cloud.setCurrentGroup(group);
        List<Action> actions = Action.find(Action.class, "group_id = ?", group.getCloudGroupKey());
        for (Action action : actions) {
            action.init();
        }
        group.actions = actions;

        return group;
    }

    /**
     * Join to an existing group with key
     *
     * @param context       application context
     * @param name          group's name
     * @param cloudGroupKey group's key
     */
    public static void joinGroupWithKey(Context context, String name, String cloudGroupKey) {
        //check that group doesn't already exdist:
        for (GroupNameRecord groupNameRecord : getSavedGroupNames()) {
            if (groupNameRecord.getGroupCloudKey().equals(cloudGroupKey)) {
                Toast.makeText(context, "Group already exist", Toast.LENGTH_LONG).show();
                return;
            }
        }


        String installationId = new BigInteger(130, new SecureRandom()).toString(32);
        GroupNameRecord groupNameRecord = new GroupNameRecord(name, cloudGroupKey,installationId);
        groupNameRecord.save();
        ParsePush.subscribeInBackground(cloudGroupKey);//subscribe to the group chanel
        FairShareGroup group = new FairShareGroup(name, cloudGroupKey, installationId);
        group.save();
        group.sync(context);

    }
public long getLastSyncTime(){
    return this.lastSyncTime;
}
    public String getInstallationId() {
        return installationId;
    }

    /**
     * Returns a table with all actions' ids
     *
     * @return table with all actions' ids
     */
    public Hashtable<String, Action> getActionsIdTable() {
        Hashtable<String, Action> table = new Hashtable<>();
        for (Action action : actions) {
            table.put(action.getActionId(), action);
        }
        return table;
    }

    /**
     * Add new action
     *
     * @param action
     */
    public void addAction(Action action) {
        action.setGroup(cloudGroupKey, groupName, installationId);
        this.actions.add(action);
        action.save();
        save();
        action.sendActionToCloud();
    }

    /**
     * Sets the current GroupActivity. This function is needed for comunicating with the activity.
     *
     * @param parentActivity GroupActivity
     */
    public void setGroupActivity(GroupActivity parentActivity) {
        this.groupActivity = parentActivity;
    }

    /**
     * This methods puts a userRemoved command to the cloud
     *
     * @param user the user to remove
     */
    private void sendRemoveUserCommand(User user) {
        this.cloud.sendRemoveUserCommand(user);
    }

    /**
     * This methods puts a userAdded command to the cloud
     *
     * @param context
     * @param user    the user to add
     */
    private void sendUserAddedCommand(final Context context, User user) {
        this.cloud.sendUserAddedCommand(user);
    }

    /**
     * Sync new actions and command from the cloud
     *
     * @param context application context
     */
    public void sync(final Context context) {
        //make the sync thread safe:
        this.cloud.fetchData();
    }

    /**
     * Report changes in the user list via push notification
     */
    private void reportUserChangeViaPush() {
        ParsePush push = new ParsePush();
        push.setChannel(getCloudGroupKey());
        JSONObject jsonToPush = new JSONObject();
        try {
            jsonToPush.put("alertType", "USER_CHANGE");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        push.setMessage(jsonToPush.toString());
        push.sendInBackground();
    }


    /**
     * Get list of all the users in the group
     *
     * @return a list with all the users in the group
     */
    public List<User> getUsers() {
        return this.users;
    }

    /**
     * Set users
     *
     * @param users a list of users
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Get group's name
     *
     * @return group's name
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * Add a ghost user to the group
     *
     * @param user user to add
     */
    public void addGhostUser(User user) {
        user.setBelongingGroupId(getCloudGroupKey());
        user.save();
        this.users.add(user);
    }

    /**
     * Add new user to the group
     *
     * @param context context
     * @param user    user user to add
     */
    public void addUser(Context context, User user) {
        user.setBelongingGroupId(getCloudGroupKey());
        user.save();
        this.users.add(user);
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User first, User second) {
                return first.getUserName().compareTo(second.getUserName());
            }
        });
        sendUserAddedCommand(context, user);

    }

    /**
     * Remove user from the group
     *
     * @param context context
     * @param user    user to remove
     */
    public void removeUser(Context context, User user) {
        this.users.remove(user);
        sendRemoveUserCommand(user);
        user.delete();
    }

    /**
     * Return the group's cloud key
     *
     * @return group's cloud key
     */
    public String getCloudGroupKey() {
        return this.cloudGroupKey;
    }

    /**
     * Consumes an action.
     * This method is used for aplying all the operations of the action on the user's balance.
     *
     * @param action the action to consume
     */
    public void consumeAction(Action action) {
        //We start by getting a list of notified id's, so if we make a change to a notified id we
        //send a notice to the activityGroup to activate the alert icon:
        List<Alert.NotifiedId> notifiedIds = Alert.NotifiedId.listAll(Alert.NotifiedId.class);
        Hashtable<String, Boolean> notifiedTable = new Hashtable<>();
        for (Alert.NotifiedId notifiedId : notifiedIds) {
            notifiedTable.put(notifiedId.userId, true);
        }

        //we build a map between userId string and a User object
        Hashtable<String, User> usersTable = new Hashtable<>();
        for (User user : users) {
            usersTable.put(user.getUserId(), user);
        }

        for (Operation operation : action.getOperations()) {
            User user = usersTable.get(operation.userId);

            if (user == null) { //if user not exist, he will come back as a ghost:
                user = new User(operation.username, operation.getUserId(), 0,true);
                addGhostUser(user);
            }
            user.addToBalance(operation.getPaid() - operation.getShare());
            //if we didn't create this action (we don't want to be notified on our own actions) and the ID was registered to be notified, we create an alert object:
            if (!action.getInstallationId().equals(this.installationId) && notifiedTable.containsKey(user.getUserId())) {
                Alert.AlertObject newAlert = new Alert.AlertObject(action.getDescription(), operation.getPaid() - operation.getShare(), user.getUserName());
                if (groupActivity != null) {
                    groupActivity.messageHandler(GroupActivity.BALANCE_CHANGED, newAlert);
                }
            }


        }
        //report user changed to notify the adapter:
        if (groupActivity != null) {
            groupActivity.messageHandler(GroupActivity.NOTIFY_USER_CHANGE, null);
        }

    }


    /**
     * Deletes the group
     */
    @Override
    public void delete() {
        super.delete();
        for (User user : users) {
            user.delete();
        }
        for (Action action : actions) {
            action.delete();
        }

    }

    /**
     * A class that represents a mapping between group name and a group ID
     */
    public static class GroupNameRecord extends SugarRecord<GroupNameRecord> implements Serializable {
        private String groupName;
        private String groupCloudKey;
        private String installationId;

        public GroupNameRecord() {
        }

        public GroupNameRecord(String name, String groupCloudKey, String installationId) {
            this.groupName = name;
            this.groupCloudKey = groupCloudKey;
            this.installationId= installationId;
        }

        public String getGroupCloudKey() {
            return groupCloudKey;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getInstallationId(){
            return this.installationId;
        }


    }
}


