package share.fair.fairshare;

import android.content.Context;
import android.content.SharedPreferences;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Nir on 13/10/2015.
 */
public class GroupLog extends SugarRecord<GroupLog> implements Serializable {
    @Ignore
    public List<Action> actions = new ArrayList<>();
    

    private String cloudLogKey;

    public String getGroupLogId() {
        return groupLogId;
    }

    private String groupLogId;


    public void setParentGroup(FairShareGroup parentGroup) {
        this.parentGroup = parentGroup;
    }

    @Ignore
    private FairShareGroup parentGroup;

    public GroupLog() {
    }

    public GroupLog(String cloudLogKey) {
        this.cloudLogKey = cloudLogKey;
        this.groupLogId = new BigInteger(130, new SecureRandom()).toString(32);
        actions = new ArrayList<>();
    }


    public  void init() {
        actions = Action.find(Action.class, "group_log_id = ?", groupLogId);
        for(Action action: actions){
            action.init();
        }
    }




    public Hashtable<String, Action> getActionsIdTable() {
        Hashtable<String, Action> table = new Hashtable<>();
        for (Action action : actions) {
            table.put(action.getActionId(), action);
        }
        return table;
    }


    public void addAction(Context context, Action action) {
        action.setGroupLogId(groupLogId);
        this.actions.add(action);
        action.save();
        save();
        sendActionToCloud(action);

    }

    private void reportActionViaPush(Action action){
        ParsePush push = new ParsePush();
        push.setChannel(parentGroup.getCloudGroupKey());
            JSONObject jsonToPush=new JSONObject();
        try {
            jsonToPush.put("alertType", "ACTION_CHANGE");
            jsonToPush.put("creatorId", parentGroup.getInstallationId());
            jsonToPush.put("groupName", parentGroup.getGroupName());
            jsonToPush.put("groupId", parentGroup.getCloudGroupKey());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for(Operation operation: action.getOperations()){
            try {
                jsonToPush.put(operation.getUserId(), true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        push.setMessage(jsonToPush.toString());
        push.sendInBackground();
    }

    public void makeActionUneditable(Context context, Action action){
        action.makeUneditable();
        sendUnEditableCommandToCloud(context,action);
    }

    private void sendUnEditableCommandToCloud(Context context, final Action action) {
        ParseObject parseGroupLog = new ParseObject(this.cloudLogKey);
        parseGroupLog.put("action", "UNEDITED_ACTION");
        parseGroupLog.put("actionId", action.getActionId());

        SharedPreferences settings = context.getSharedPreferences("MAIN_PREFERENCES", 0);
        final String creatorId= settings.getString("id", "");
        parseGroupLog.put("creatorId", creatorId);

        parseGroupLog.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    reportActionViaPush(action);
                }
            }
        });


    }

    private void sendActionToCloud(final Action action) {
        ParseObject parseGroupLog = new ParseObject(this.cloudLogKey);
        parseGroupLog.put("action", "NEW_ACTION");
        parseGroupLog.put("jsonAction", action.toJSON());
        parseGroupLog.put("actionId", action.getActionId());
        parseGroupLog.put("creatorId", action.getCreatorId());
        parseGroupLog.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    reportActionViaPush(action);
                }
            }
        });
    }

    @Override
    public void delete() {
        super.delete();
        for(Action action : actions){
            action.delete();
        }
    }
}

