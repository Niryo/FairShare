package share.fair.fairshare;

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
import java.util.List;

/**
 * Created by Nir on 13/10/2015.
 */
public class GroupLog implements Serializable {
    String cloudLogKey;
    ArrayList<Action> actions = new ArrayList<>();
    private Date lastActionTimestamp;

    public GroupLog(String cloudLogKey) {
        this.cloudLogKey = cloudLogKey;
        this.lastActionTimestamp = new Date();
        this.lastActionTimestamp.setTime(0);
    }

    public GroupLog(JSONObject jsonLog) {
        try {
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

    public void syncActions() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(this.cloudLogKey);
        query.whereGreaterThan("createdAt", lastActionTimestamp);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e != null && list != null) {
                    for (ParseObject parseObject : list) {
                        Action newAction = new Action(parseObject.getJSONObject("description"));
                        actions.add(newAction);
                    }
                }
            }
        });
    }

    public void addAction(Action action) {
        this.actions.add(action);
        sendActionToCloud(action);
    }

    private void sendActionToCloud(Action action) {
        ParseObject parseGroupLog = new ParseObject(this.cloudLogKey);
        parseGroupLog.put("jsonAction", action.toJSON());
        parseGroupLog.saveEventually();
    }

}

