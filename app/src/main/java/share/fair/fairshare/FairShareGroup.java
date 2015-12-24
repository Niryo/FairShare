package share.fair.fairshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
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
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import share.fair.fairshare.activities.GroupActivity;


/**
 * This class represents a group of users
 */
public class FairShareGroup extends SugarRecord<FairShareGroup>  {
    private String groupName; //group name
    private String cloudGroupKey = ""; //for storing information on the cloud
    private long lastSyncTime; //for tracking when was the last sync wiht the cloud

    public String getInstallationId() {
        return installationId;
    }

    private String installationId =""; //unique id for every installation of the app

    @Ignore
    private boolean syncLock = false; //for preventing sync clashing with multiple threads
    @Ignore
    private List<User> users = new ArrayList<>(); //list of the users of the group
    @Ignore
    public List<Action> actions = new ArrayList<>();
    @Ignore
    private transient GroupActivity groupActivity; //for sending messages to the activity, for example when need to update the user list.
    public FairShareGroup() { //must ve empty constructor
    }

    private FairShareGroup(String name, String cloudGroupKey, String installationId) {
        this.lastSyncTime =0;
        this.groupName = name;
        this.cloudGroupKey =cloudGroupKey;
        this.installationId = installationId;
        actions = new ArrayList<>();
    }

    /**
     * Build new group
     * @param context application context. for using the sharedPreferences
     * @param groupName the group's name
     * @param userNameInGroup the first user in the group
     * @return a new Group object
     */
    public static FairShareGroup groupBuilder(Context context, String groupName, String userNameInGroup) {
        String cloudGroupKey = "a" + new BigInteger(130, new SecureRandom()).toString(32); //the key must start with a letter.
        ParsePush.subscribeInBackground(cloudGroupKey);//subscribe to the group chanel
        String installationId =  new BigInteger(130, new SecureRandom()).toString(32);
        FairShareGroup group = new FairShareGroup(groupName, cloudGroupKey, installationId);
        group.addUser(context,new User(userNameInGroup, 0.0));
        group.save();
        GroupNameRecord groupNameRecord = new GroupNameRecord(groupName, cloudGroupKey);
        groupNameRecord.save();
        return group;
    }

    /**
     * Returns a list with all the groups' names
     * @return a list with all group names
     */
    public static List<GroupNameRecord> getSavedGroupNames() {
        return GroupNameRecord.listAll(GroupNameRecord.class);
    }

    /**
     * Loads an existing group from storage
     * @param cloudGroupKey the group id
     * @return a Group object
     */
    public static FairShareGroup loadGroupFromStorage(String cloudGroupKey) {
        List<FairShareGroup> groups = FairShareGroup.find(FairShareGroup.class, "cloud_group_key = ?", cloudGroupKey);
        FairShareGroup group = groups.get(0);
        List<User> users = User.find(User.class, "belonging_group_id = ?", group.getCloudGroupKey());
        group.setUsers(users);
        List<Action> actions = Action.find(Action.class, "group_id = ?", group.getCloudGroupKey());
        for(Action action: actions){
            action.init();
        }
        group.actions = actions;

        return group;
    }

    /**
     * Returns a table with all actions' ids
     * @return table with all actions' ids
     */
    private Hashtable<String, Action> getActionsIdTable() {
        Hashtable<String, Action> table = new Hashtable<>();
        for (Action action : actions) {
            table.put(action.getActionId(), action);
        }
        return table;
    }


    /**
     * Add new action
     * @param action
     */
    public void addAction(Action action) {
        action.setGroup(cloudGroupKey,groupName,installationId);
        this.actions.add(action);
        action.save();
        save();
        action.sendActionToCloud();
    }




    /**
     * Join to an existing group with key
     * @param context application context
     * @param name group's name
     * @param cloudGroupKey group's key
     */
    public static void joinGroupWithKey(Context context, String name, String cloudGroupKey) {
        //check that group doesn't already exdist:
        for (GroupNameRecord groupNameRecord : getSavedGroupNames()){
            if(groupNameRecord.getGroupId().equals(cloudGroupKey)){
                Toast.makeText(context, "Group already exist", Toast.LENGTH_LONG).show();
                return;
            }
        }


        GroupNameRecord groupNameRecord = new GroupNameRecord(name, cloudGroupKey);
        groupNameRecord.save();
        ParsePush.subscribeInBackground(cloudGroupKey);//subscribe to the group chanel
        String installationId =  new BigInteger(130, new SecureRandom()).toString(32);
        FairShareGroup group = new FairShareGroup(name,cloudGroupKey,installationId);
        group.save();
        group.sync(context, true);

    }


    /**
     * Sets the current GroupActivity. This function is needed for comunicating with the activity.
     * @param parentActivity GroupActivity
     */
    public void setGroupActivity(GroupActivity parentActivity) {
        this.groupActivity = parentActivity;
    }

    /**
     * This methods puts a userRemoved command to the cloud
     * @param user the user to remove
     */
    private void sendRemoveUserCommand(User user){
        ParseObject parseGroup = new ParseObject(this.cloudGroupKey);
        parseGroup.put("userId", user.getUserId());
        parseGroup.put("userName", user.getName());
        parseGroup.put("action", "USER_REMOVED");
        parseGroup.put("creatorId", installationId);
        parseGroup.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    reportUserChangeViaPush();
                }
            }
        });
}

    /**
     * This methods puts a userAdded command to the cloud
     * @param context
     * @param user the user to add
     */
    private void sendUserAddedCommand(final Context context, User user) {
        ParseObject parseGroup = new ParseObject(this.cloudGroupKey);
        parseGroup.put("userId", user.getUserId());
        parseGroup.put("userName", user.getName());
        parseGroup.put("userBalance",user.getBalance());
        parseGroup.put("action", "USER_ADDED");
        parseGroup.put("creatorId", installationId);
        parseGroup.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    reportUserChangeViaPush();
                    Toast.makeText(context, "user saved in cloud", Toast.LENGTH_LONG).show();
                } else {
                    e.printStackTrace();
                    Toast.makeText(context, "user hasn't been saved in cloud", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Sync new actions and command from the cloud
     * @param context application context
     * @param firstTime if this is the first sync, put true in this value.
     */
    public void sync(final Context context, final boolean firstTime) {
        //make the sync thread safe:
        if(syncLock){
           return;
        }
        syncLock=true;
        ParseQuery<ParseObject> query = ParseQuery.getQuery(this.cloudGroupKey);
        final Date date = new Date(lastSyncTime);
        //if this is the first time we want to fetch even old actions that we made in the past.
        //this is the case when we remove a group and re join to it.
        if(!firstTime){
        query.whereNotEqualTo("creatorId", installationId);
        }
        query.whereGreaterThan("createdAt", date);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null && list.size()>0) {
                    ArrayList<String> userIdToIgnore = new ArrayList<>();
                    List<User> userToSave = new ArrayList<>();
                    long lastUserSyncCopy = lastSyncTime;

                    boolean dirty = false;
                    for (ParseObject parseObject : list) {
                        final ArrayList<User> usersCopy = new ArrayList<>(users);
                        String action = parseObject.getString("action");
                        lastUserSyncCopy = Math.max(parseObject.getCreatedAt().getTime(), lastUserSyncCopy);

                        if (action.equals("UNEDITED_ACTION")) {
                            Hashtable<String, Action> actionsIdTable = getActionsIdTable();
                            String actionId = parseObject.getString("actionId");
                            if (actionsIdTable.containsKey(actionId)) {
                                actionsIdTable.get(actionId).makeUneditable(context, false);
                            }

                        }


                        if (action.equals("NEW_ACTION")) {
                            Hashtable<String, Action> actionsIdTable = getActionsIdTable();
                            JSONObject jsonObject = parseObject.getJSONObject("jsonAction");
                            String actionId = parseObject.getString("actionId");
                            if (jsonObject != null && !actionsIdTable.containsKey(actionId)) { //check if we don't already have this action
                                Action newAction = new Action(jsonObject);
                                actionsIdTable.put(actionId, newAction);
                                newAction.setGroup(cloudGroupKey,groupName,installationId);
//                                actionsToSave.add((newAction));
                                actions.add(newAction);
                                consumeAction(newAction);
                                newAction.save();
                            }
                        }


                        if (action.equals("USER_ADDED")) {
                            String userId = parseObject.getString("userId");
                            boolean isUserExist = false;
                            for (User user : usersCopy) {
                                if (user.getUserId().equals(userId)) {
                                    isUserExist = true;
                                    break; //id user already exist, skip it.
                                }
                            }
                            if (!isUserExist) {
                                if (userIdToIgnore.contains(userId)) {
                                    Toast.makeText(context, "DEBUG: userIdToIgnore was activated!", Toast.LENGTH_LONG).show();
                                    continue;
                                }

                                String userName = parseObject.getString("userName");
                                User newUser = new User(userName, 0);
                                newUser.setUserId(userId);
                                newUser.setBelongingGroupId(getCloudGroupKey());
                                userToSave.add(newUser);
                                users.add(newUser);
                                dirty = true;
                            }
                        }

                        if (action.equals("USER_REMOVED")) {
                            String userId = parseObject.getString("userId");
                            boolean success = false;
                            for (User user : usersCopy) {
                                if (user.getUserId().equals(userId)) {
                                    users.remove(user);
                                    if (userToSave.contains(user)) {
                                        userToSave.remove(user); //no need to delete because user hasn't been save yet.
                                    } else {
                                        user.delete();
                                    }

                                    if (Math.abs(user.getBalance()) != 0) {
                                        user = new User(user.getName(), user.getUserId(), user.getBalance());
                                        addGhostUser(user);
                                    }

                                    success = true;
                                    dirty = true;
                                    break;
                                }
                            }
                            if (!success) {
                                userIdToIgnore.add(userId);
                            }
                        }


                    }

                    for (User user : userToSave) {
                        user.save();
                    }


                    if (dirty && groupActivity != null) {
                        groupActivity.messageHandler(GroupActivity.NOTIFY_USER_CHANGE, null);


                    }

                    lastSyncTime = lastUserSyncCopy; //only after saving the users we update lastSync
                    save();

                }
                syncLock = false;
            }
        });
    }

    private void reportUserChangeViaPush(){
        ParsePush push = new ParsePush();
        push.setChannel(getCloudGroupKey());
        JSONObject jsonToPush=new JSONObject();
        try {
            jsonToPush.put("alertType", "USER_CHANGE");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        push.setMessage(jsonToPush.toString());
        push.sendInBackground();
    }



    public List<User> getUsers() {
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }


    public String getGroupName() {
        return this.groupName;
    }

    public void addGhostUser(User user){
        user.setBelongingGroupId(getCloudGroupKey());
        user.save();
        this.users.add(user);
    }

    public void addUser(Context context, User user) {
        user.setUserId(new BigInteger(130, new SecureRandom()).toString(32).substring(0, 6));
        user.setBelongingGroupId(getCloudGroupKey());
        user.save();
        this.users.add(user);
        sendUserAddedCommand(context, user);

    }

    public void removeUser(Context context, User user){
        this.users.remove(user);
        sendRemoveUserCommand(user);
        user.delete();
    }

    public String getCloudGroupKey() {
        return this.cloudGroupKey;
    }


    public void consumeAction(Action action) {
        List<Alert.NotifiedId> notifiedIds =  Alert.NotifiedId.listAll(Alert.NotifiedId.class);
        Hashtable<String, Boolean> notifiedTable = new Hashtable<>();
        for(Alert.NotifiedId notifiedId: notifiedIds){
            notifiedTable.put(notifiedId.userId,true);
        }
        Hashtable<String, User> usersTable = new Hashtable<>();
        for (User user : users) {
            usersTable.put(user.getUserId(), user);
        }

        for (Operation operation : action.getOperations()) {
            User user = usersTable.get(operation.userId);

            if (user == null) {
                user = new User(operation.username ,operation.getUserId(),0);
                addGhostUser(user);
            }
                user.addToBalance(operation.getPaid() - operation.getShare());//todo: check!!
                if(!action.getCreatorId().equals(this.installationId) && notifiedTable.containsKey(user.getUserId())){
                    Alert.AlertObject newAlert = new Alert.AlertObject(action.getDescription(),operation.getPaid()-operation.getShare(),user.getName());
                    if (groupActivity != null) {
                        groupActivity.messageHandler(GroupActivity.BALANCE_CHANGED,newAlert);
                    }
                }



        }
        //report user changed to notify the adapter:
        if (groupActivity != null) {
            Message msg=Message.obtain();
            msg.what=GroupActivity.NOTIFY_USER_CHANGE;
            groupActivity.messageHandler(GroupActivity.NOTIFY_USER_CHANGE, null);
        }

    }


    @Override
    public void delete() {
        super.delete();
        for(User user: users){
            user.delete();
        }
        for(Action action : actions){
            action.delete();
        }

    }

    public static class GroupNameRecord extends SugarRecord<GroupNameRecord>  implements Serializable{
        private String groupName;
        private String groupId;

        public  GroupNameRecord(){}

        public GroupNameRecord(String name, String groupId) {
            this.groupName = name;
            this.groupId = groupId;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getGroupName() {
            return groupName;
        }

    }
}


