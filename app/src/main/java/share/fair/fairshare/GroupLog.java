package share.fair.fairshare;

import android.content.Context;
import android.content.SharedPreferences;

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
public class GroupLog implements Serializable {
    String cloudLogKey;
    ArrayList<Action> actions = new ArrayList<>();
    private Date lastActionTimestamp;
    private Group parentGroup;

    public GroupLog(Group parentGroup, String cloudLogKey) {
        this.parentGroup = parentGroup;
        this.cloudLogKey = cloudLogKey;
        this.lastActionTimestamp = new Date();
        this.lastActionTimestamp.setTime(0);
        this.actions = new ArrayList<Action>(); // i added(Ori)
    }

    public GroupLog(Group parentGroup, JSONObject jsonLog) {
        try {
            this.parentGroup = parentGroup;
            this.lastActionTimestamp = new Date(jsonLog.getLong("lastActionTimestamp"));
            this.cloudLogKey = jsonLog.getString("cloudLogKey");
            JSONArray actionArray = jsonLog.getJSONArray("actions");
            for (int i = 0; i < actionArray.length(); i++) {
                actions.add(new Action(actionArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cloudLogKey", this.cloudLogKey);
            jsonObject.put("lastActionTimestamp", this.lastActionTimestamp.getTime());
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

    //public ParseObject toParseObject(){
//    ParseObject groupLogParse = new ParseObject("GroupLog");
//    for(Action action : this.actions){
//            action.toParseObject();
//    }
//}
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
        query.whereGreaterThan("createdAt", lastActionTimestamp);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null) {
                    Hashtable<String, Boolean> actionsIdTable = getActionsIdTable();
                    for (ParseObject parseObject : list) {
                        JSONObject jsonObject = parseObject.getJSONObject("jsonAction");
                        String actionId = parseObject.getString("actionId");
                        if (jsonObject != null &&!actionsIdTable.containsKey(actionId) ) { //check if we don't already have this action
                                Action newAction = new Action(jsonObject);
                                actions.add(newAction);
                                lastActionTimestamp = parseObject.getCreatedAt();
                                parentGroup.consumeAction(newAction);
                                parentGroup.saveGroupToStorage(context);
                        }
                    }
                }
            }
        });
    }

    public void addAction(Context context, Action action) {
        this.actions.add(action);
        parentGroup.saveGroupToStorage(context);
        sendActionToCloud(action);
    }

    private void sendActionToCloud(Action action) {
        ParseObject parseGroupLog = new ParseObject(this.cloudLogKey);
        parseGroupLog.put("jsonAction", action.toJSON());
        parseGroupLog.put("actionId", action.getActionId());
        parseGroupLog.put("creatorId", action.getCreatorId());
        parseGroupLog.saveEventually();
    }

}

