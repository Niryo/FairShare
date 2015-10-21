package share.fair.fairshare;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


/**
 * Created by Nir on 09/10/2015.
 */


public class FairShareGroup extends SugarRecord<FairShareGroup>  {
    private String name;
    private long groupLogId;
    private String cloudLogKey = "";
    private String cloudGroupKey = "";

    @Ignore
    private List<User> users = new ArrayList<>();
    @Ignore
    private GroupLog groupLog;
    @Ignore
    private transient Handler parentActivityMessageHandler;


    public FairShareGroup() { //must ve empty constructor
    }

    private FairShareGroup(String name) {
        this.name = name;
    }


    public static FairShareGroup groupBuilder(String name) {
        FairShareGroup group = new FairShareGroup(name);
        Date zeroDate = new Date();
        zeroDate.setTime(0);
        String cloudGroupKey = "a" + new BigInteger(130, new SecureRandom()).toString(32);
        String cloudLogKey = "a" + new BigInteger(130, new SecureRandom()).toString(32);
        group.setCloudGroupKey(cloudGroupKey);
        group.setCloudLogKey(cloudLogKey);
        GroupLog groupLog = new GroupLog(group, cloudLogKey);
        groupLog.save();
        group.setGroupLog(groupLog);
        group.save();
        GroupNameRecord groupNameRecord = new GroupNameRecord(name, group.getId());
        groupNameRecord.save();


        return group;
    }

    public static List<GroupNameRecord> getSavedGroupNames() {

        return GroupNameRecord.listAll(GroupNameRecord.class);

    }

    public static FairShareGroup loadGroupFromStorage(long groupId) {
        FairShareGroup group = FairShareGroup.findById(FairShareGroup.class, groupId);
        List<User> users = User.find(User.class, "belonging_group_id = ?", Long.toString(group.getId()));
        group.setUsers(users);
        GroupLog groupLog = GroupLog.findById(GroupLog.class, group.groupLogId);
        groupLog.init(group);
        group.setGroupLog(groupLog);
        return group;
    }



    public static void joinGroupWithKey(Context context, String name, String cloudGroupKey, String cloudLogKey) {
        FairShareGroup group = new FairShareGroup(name);
        group.setGroupLog(new GroupLog(group, cloudLogKey));
        GroupNameRecord groupNameRecord = new GroupNameRecord(name, group.getId());
        groupNameRecord.save();
        group.setCloudGroupKey(cloudGroupKey);
        group.save();
    }


    public void setParentActivityMessageHandler(Handler parentActivityMessageHandler) {
        this.parentActivityMessageHandler = parentActivityMessageHandler;
    }

    public void addUserToCloud(final Context context, User user) {
        ParseObject parseGroup = new ParseObject(this.cloudGroupKey);
        parseGroup.put("userId", user.getUserId());
        parseGroup.put("userName", user.getName());
        parseGroup.put("userEmail", user.getEmail());
        parseGroup.put("userBalance",user.getBalance());
        parseGroup.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(context, "user saved in cloud", Toast.LENGTH_LONG).show();
                } else {
                    e.printStackTrace();
                    Toast.makeText(context, "user hasn't been saved in cloud", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void syncUsers() {
        final ArrayList<User> usersCopy = new ArrayList<>(users); //a copy so we wan't run on mutable list;
        ParseQuery<ParseObject> query = ParseQuery.getQuery(this.cloudGroupKey);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null) {
                    boolean dirty = false;
                    Hashtable<String, String> currentUserTable = new Hashtable<String, String>();
                    Hashtable<String, String> cloudUserTable = new Hashtable<String, String>();
                    for (User user : usersCopy) {
                        currentUserTable.put(user.getUserId(), ""); //name is not relevant here
                    }
                    for (ParseObject parseObject : list) {
                        String userId = parseObject.getString("userId");
                        String userName = parseObject.getString("userName");
                        if (userId != null && userName != null) {
                            cloudUserTable.put(parseObject.getString("userId"), parseObject.getString("userName"));
                        }
                    }

                    for (User user : usersCopy) {
                        if (!cloudUserTable.containsKey(user.getUserId())) {
                            users.remove(user);
                            user.delete();
                            dirty = true;
                        }
                    }
                    Enumeration<String> keys = cloudUserTable.keys();
                    while (keys.hasMoreElements()) {
                        String id = keys.nextElement();
                        if (!currentUserTable.containsKey(id)) {
                            User newUser = new User(cloudUserTable.get(id), 0);
                            newUser.setUserId(id);
                            newUser.setBelongingGroupId(getId());
                            newUser.save();
                            users.add(newUser);
                            dirty = true;
                        }
                    }
                    if (dirty) {
                        Message msg = Message.obtain();
                        msg.what=GroupActivity.NOTIFY_USER_CHANGE;
                        parentActivityMessageHandler.sendMessage(msg);
                    }
                }

            }
        });
    }

    public String getCloudLogKey() {
        return cloudLogKey;
    }

    public void setCloudLogKey(String cloudLogKey) {
        this.cloudLogKey = cloudLogKey;
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

    public void addUser(Context context, User user) {
        user.setUserId(new BigInteger(130, new SecureRandom()).toString(32).substring(0, 6));
        user.setBelongingGroupId(getId());
        user.save();
        this.users.add(user);
        addUserToCloud(context, user);
    }

    public String getCloudGroupKey() {
        return this.cloudGroupKey;

    }

    public void setCloudGroupKey(String cloudKey) {
        this.cloudGroupKey = cloudKey;
    }

    public void consumeAction(Action action) {
        Hashtable<String, User> usersTable = new Hashtable<String, User>();
        for (User user : users) {
            usersTable.put(user.getUserId(), user);
        }
        for (Operation operation : action.getOperations()) {
            User user = usersTable.get(operation.userId);
            if (user != null) {
                user.addToBalance(operation.getPaid() - operation.getShare());//todo: check!!
            }
        }
        //report user changed to notify the adapter:
        if (parentActivityMessageHandler != null) { //todo: check if uerlist is being updated
            Message msg;
            msg = Message.obtain();
            parentActivityMessageHandler.sendMessage(msg);
        }

    }


    @Override
    public void save() {
        super.save();

        this.groupLog.save();
        for (User user : users) {
            user.save();
        }
    }

    public static class GroupNameRecord extends SugarRecord<GroupNameRecord> {
        private String groupName;
        private Long groupId;

        public  GroupNameRecord(){}

        public GroupNameRecord(String name, Long groupId) {
            this.groupName = name;
            this.groupId = groupId;
        }

        public String getGroupName() {
            return groupName;
        }

    }
}


