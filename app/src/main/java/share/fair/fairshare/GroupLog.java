package share.fair.fairshare;

import android.content.Context;
import android.content.SharedPreferences;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Nir on 13/10/2015.
 */
public class GroupLog extends SugarRecord<GroupLog> implements Serializable {
    @Ignore
    List<Action> actions = new ArrayList<>();
    private String cloudLogKey;
    private Long lastActionTimestampInMilisec;
    @Ignore
    private FairShareGroup parentGroup;

    public GroupLog() {
    }

    public GroupLog(FairShareGroup parentGroup, String cloudLogKey) {
        this.parentGroup = parentGroup;
        this.cloudLogKey = cloudLogKey;
        this.lastActionTimestampInMilisec = Long.valueOf(0);
        actions = new ArrayList<>();
    }


    public void init(FairShareGroup parentGroup) {
        if(getId()==null){save();}
        this.parentGroup = parentGroup;
        actions = Action.find(Action.class, "group_log_id = ?", Long.toString(getId()));
        for(Action action: actions){
            action.init();
        }
    }


    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cloudLogKey", this.cloudLogKey);
            jsonObject.put("lastActionTimestamp", this.lastActionTimestampInMilisec);
            JSONArray actionsArray = new JSONArray();
            for (int i = 0; i < this.actions.size(); i++) {
                actionsArray.put(actions.get(i).toJSON());
            }
            jsonObject.put("actions", actionsArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    private Hashtable<String, Boolean> getActionsIdTable() {
        Hashtable<String, Boolean> table = new Hashtable<>();
        for (Action action : actions) {
            table.put(action.getActionId(), true);
        }
        return table;
    }

    public void syncActions(final Context context) {
        parentGroup.syncUsers(new FairShareCallback() { //we sync the users
            @Override
            public void doAction() {
                ParseQuery<ParseObject> query = ParseQuery.getQuery(cloudLogKey);
                SharedPreferences settings = context.getSharedPreferences("MAIN_PREFERENCES", 0);
                String creatorId = settings.getString("id", "");
                query.whereNotEqualTo("creatorId", creatorId); //we don't want to fetch our own updates
                query.whereGreaterThan("createdAt", new Date(lastActionTimestampInMilisec));
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e == null && list != null) {
                            Hashtable<String, Boolean> actionsIdTable = getActionsIdTable();
                            for (ParseObject parseObject : list) {
                                JSONObject jsonObject = parseObject.getJSONObject("jsonAction");
                                String actionId = parseObject.getString("actionId");
                                if (jsonObject != null && !actionsIdTable.containsKey(actionId)) { //check if we don't already have this action
                                    Action newAction = new Action(jsonObject);
                                    actionsIdTable.put(actionId, true);
                                    if (getId() == null) {
                                        save();
                                    }
                                    newAction.setGroupLogId(getId());
                                    newAction.save();
                                    actions.add(newAction);
                                    lastActionTimestampInMilisec = Math.max(parseObject.getCreatedAt().getTime(), lastActionTimestampInMilisec);
                                    parentGroup.consumeAction(newAction);
                                }
                            }
                            save();
                        }
                    }
                });
            }
        });


    }
    public void addAction(Context context, Action action) {
        if(getId()==null) {
        save();
        }
            action.setGroupLogId(getId());
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
            jsonToPush.put("creatorId", parentGroup.getOwnerId());
            jsonToPush.put("groupName", parentGroup.getName());
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


    private void sendActionToCloud(final Action action) {
        ParseObject parseGroupLog = new ParseObject(this.cloudLogKey);
        parseGroupLog.put("jsonAction", action.toJSON());
        parseGroupLog.put("actionId", action.getActionId());
        parseGroupLog.put("creatorId", action.getCreatorId());
        parseGroupLog.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null) {
                    reportActionViaPush(action);
                }
            }
        });
    }

    @Override
    public void save() {
        super.save();

        for (Action action : actions) {
            action.save();
        }
    }

}

