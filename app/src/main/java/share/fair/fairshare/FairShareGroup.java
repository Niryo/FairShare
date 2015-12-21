package share.fair.fairshare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
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
 * Created by Nir on 09/10/2015.
 */


public class FairShareGroup extends SugarRecord<FairShareGroup>  {
    private String name;
    private long groupLogId;
    private String cloudGroupKey = "";
    private long lastUserSync;
    private String ownerId="";
    @Ignore
    private boolean syncLock = false;
    @Ignore
    private List<User> users = new ArrayList<>();
    @Ignore
    private GroupLog groupLog;
    @Ignore
    private transient Handler parentActivityMessageHandler;
    public FairShareGroup() { //must ve empty constructor
    }
    private FairShareGroup(String name) {
        this.lastUserSync=0;
        this.name = name;
    }


    public static FairShareGroup groupBuilder(Context context, String groupName, String userNameInGroup) {
        FairShareGroup group = new FairShareGroup(groupName);
        String cloudGroupKey = "a" + new BigInteger(130, new SecureRandom()).toString(32);
        String cloudLogKey = "a" + new BigInteger(130, new SecureRandom()).toString(32);
        group.setCloudGroupKey(cloudGroupKey);
        ParsePush.subscribeInBackground(cloudGroupKey);//subscribe to the group chanel
        GroupLog groupLog = new GroupLog(group, cloudGroupKey);
        groupLog.save();
        group.setGroupLog(groupLog);
        SharedPreferences settings = context.getSharedPreferences("MAIN_PREFERENCES", 0);
        String ownerId = settings.getString("id", "");
        group.setOwnerId(ownerId);
        group.addUser(context,new User(userNameInGroup, 0.0));
        group.save();
        GroupNameRecord groupNameRecord = new GroupNameRecord(groupName, group.getCloudGroupKey());


        groupNameRecord.save();
        return group;
    }

    public static List<GroupNameRecord> getSavedGroupNames() {

        return GroupNameRecord.listAll(GroupNameRecord.class);

    }

    public static FairShareGroup loadGroupFromStorage(String groupId) {

        List<FairShareGroup> groups = FairShareGroup.find(FairShareGroup.class, "cloud_group_key = ?", groupId);
        FairShareGroup group = groups.get(0);
        List<User> users = User.find(User.class, "belonging_group_id = ?", group.getCloudGroupKey());
        group.setUsers(users);
        GroupLog groupLog = GroupLog.findById(GroupLog.class, group.groupLogId);
        groupLog.init(group);
        group.setGroupLog(groupLog);
        return group;
    }

    public static void joinGroupWithKey(Context context, String name, String cloudGroupKey) {
        //check that group doesn't already exdist:
        for (GroupNameRecord groupNameRecord : getSavedGroupNames()){
            if(groupNameRecord.getGroupId().equals(cloudGroupKey)){
                Toast.makeText(context, "Group already exist", Toast.LENGTH_LONG).show();
                return;
            }
        }

        FairShareGroup group = new FairShareGroup(name);
        GroupLog groupLog = new GroupLog(group, cloudGroupKey);
        groupLog.save();
        group.setGroupLog(groupLog);
        group.setCloudGroupKey(cloudGroupKey);
        GroupNameRecord groupNameRecord = new GroupNameRecord(name, cloudGroupKey);
        groupNameRecord.save();
        ParsePush.subscribeInBackground(cloudGroupKey);//subscribe to the group chanel
        SharedPreferences settings = context.getSharedPreferences("MAIN_PREFERENCES", 0);
        String ownerId = settings.getString("id", "");
        group.setOwnerId(ownerId);
        group.save();
        group.sync(context, true);

    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setParentActivityMessageHandler(Handler parentActivityMessageHandler) {
        this.parentActivityMessageHandler = parentActivityMessageHandler;
    }

    public void removeUserFromCloud(final Context context, User user){
        ParseObject parseGroup = new ParseObject(this.cloudGroupKey);
        parseGroup.put("userId", user.getUserId());
        parseGroup.put("userName", user.getName());
        parseGroup.put("action", "USER_REMOVED");
        parseGroup.put("creatorId", ownerId);
        parseGroup.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    reportUserChangeViaPush();
                }
            }
        });
    }
    public void addUserToCloud(final Context context, User user) {
        ParseObject parseGroup = new ParseObject(this.cloudGroupKey);
        parseGroup.put("userId", user.getUserId());
        parseGroup.put("userName", user.getName());
        parseGroup.put("userBalance",user.getBalance());
        parseGroup.put("action", "USER_ADDED");
        parseGroup.put("creatorId", ownerId);
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

    public void sync(final Context context, final boolean firstTime) {
        if(syncLock){
           return;
        }
        syncLock=true;
        ParseQuery<ParseObject> query = ParseQuery.getQuery(this.cloudGroupKey);
        final Date date = new Date(lastUserSync);
        if(!firstTime){
        query.whereNotEqualTo("creatorId", ownerId);
        }
        query.whereGreaterThan("createdAt", date);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null) {
                    ArrayList<String> userIdToIgnore = new ArrayList<String>();
                    List<User> userToSave = new ArrayList<User>();
                    List<Action> actionsToSave = new ArrayList<Action>();
                    long lastUserSyncCopy = lastUserSync;

                    boolean dirty = false;
                    for (ParseObject parseObject : list) {
                        final ArrayList<User> usersCopy = new ArrayList<>(users);
                        String action = parseObject.getString("action");
                        lastUserSyncCopy = Math.max(parseObject.getCreatedAt().getTime(), lastUserSyncCopy);

                        if (action.equals("UNEDITED_ACTION")) {
                            Hashtable<String, Action> actionsIdTable = getGroupLog().getActionsIdTable();
                            String actionId = parseObject.getString("actionId");
                            if (actionsIdTable.containsKey(actionId)) {
                                actionsIdTable.get(actionId).makeUneditable();
                            }

                        }


                        if (action.equals("NEW_ACTION")) {
                            Hashtable<String, Action> actionsIdTable = getGroupLog().getActionsIdTable();
                            JSONObject jsonObject = parseObject.getJSONObject("jsonAction");
                            String actionId = parseObject.getString("actionId");
                            if (jsonObject != null && !actionsIdTable.containsKey(actionId)) { //check if we don't already have this action
                                Action newAction = new Action(jsonObject);
                                actionsIdTable.put(actionId, newAction);
                                newAction.setGroupLogId(getId());
//                                actionsToSave.add((newAction));
                                getGroupLog().actions.add(newAction);
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

                                    if(Math.abs(user.getBalance())!=0 ){
                                        user = new User(user.getName(),user.getUserId(), user.getBalance());
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

//                    for (Action action : actionsToSave) {
//                        getGroupLog().actions.add(action);
//                        consumeAction(action);
//                        action.save();
//                    }
                    if (dirty && parentActivityMessageHandler != null) {
                        Message msg = Message.obtain();
                        msg.what = GroupActivity.NOTIFY_USER_CHANGE;
                        parentActivityMessageHandler.sendMessage(msg);


                    }

                    lastUserSync = lastUserSyncCopy; //only after saving the users we update lastSync
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




    public GroupLog getGroupLog() {
        return groupLog;
    }

    public void setGroupLog(GroupLog groupLog) {
        this.groupLogId = groupLog.getId();
        this.groupLog = groupLog;
    }

    public List<User> getUsers() {
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }


    public String getName() {
        return this.name;
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
        addUserToCloud(context, user);

    }

    public void removeUser(Context context, User user){
        this.users.remove(user);
        removeUserFromCloud(context, user);
        user.delete();
    }

    public String getCloudGroupKey() {
        return this.cloudGroupKey;
    }

    public void setCloudGroupKey(String cloudKey) {
        this.cloudGroupKey = cloudKey;
    }

    public void consumeAction(Action action) {
        List<Alert.NotifiedId> notifiedIds = (List<Alert.NotifiedId>) Alert.NotifiedId.listAll(Alert.NotifiedId.class);
        Hashtable<String, Boolean> notifiedTable = new Hashtable<>();
        for(Alert.NotifiedId notifiedId: notifiedIds){
            notifiedTable.put(notifiedId.userId,true);
        }
        Hashtable<String, User> usersTable = new Hashtable<String, User>();
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
                if(!action.getCreatorId().equals(this.ownerId) && notifiedTable.containsKey(user.getUserId())){
                    Alert.AlertObject newAlert = new Alert.AlertObject(action.getDescription(),operation.getPaid()-operation.getShare(),user.getName());
                    if (parentActivityMessageHandler != null) {
                    Message msg;
                    msg = Message.obtain();
                    msg.obj=newAlert;
                    msg.what = GroupActivity.BALANCE_CHANGED;
                        parentActivityMessageHandler.sendMessage(msg);
                    }
                }



        }
        //report user changed to notify the adapter:
        if (parentActivityMessageHandler != null) {
            Message msg=Message.obtain();
            msg.what=GroupActivity.NOTIFY_USER_CHANGE;
            parentActivityMessageHandler.sendMessage(msg);
        }

    }


    @Override
    public void delete() {
        super.delete();
        for(User user: users){
            user.delete();
        }
        groupLog.delete();

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


