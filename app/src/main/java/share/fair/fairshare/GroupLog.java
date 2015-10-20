package share.fair.fairshare;

import android.content.Context;
import android.content.SharedPreferences;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
        ParseQuery<ParseObject> query = ParseQuery.getQuery(this.cloudLogKey);
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
                            newAction.setGroupLogId(parentGroup.getGroupLog().getId());
                            newAction.save();
                            actions.add(newAction);
                            lastActionTimestampInMilisec = parseObject.getCreatedAt().getTime();
                            parentGroup.consumeAction(newAction);
                        }
                    }
                }
            }
        });
    }

    public void addAction(Context context, Action action) {
        action.setGroupLogId(parentGroup.getGroupLog().getId());
        this.actions.add(action);
        action.save();
        sendActionToCloud(action);
    }

    private void sendActionToCloud(Action action) {
        ParseObject parseGroupLog = new ParseObject(this.cloudLogKey);
        parseGroupLog.put("jsonAction", action.toJSON());
        parseGroupLog.put("actionId", action.getActionId());
        parseGroupLog.put("creatorId", action.getCreatorId());
        parseGroupLog.saveEventually();
    }

    @Override
    public void save() {
        super.save();

        for (Action action : actions) {
            action.save();
        }
    }

}

