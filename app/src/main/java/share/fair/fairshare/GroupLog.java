package share.fair.fairshare;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Nir on 13/10/2015.
 */
public class GroupLog {
    ArrayList<Action> actions=new ArrayList<>();

    public GroupLog(){
    }

    public GroupLog(JSONObject jsonLog){
        try {
            Iterator keys = jsonLog.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                actions.add(new Action((JSONObject)(jsonLog.get(key))));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() {

        JSONObject jsonObject = new JSONObject();
        try {
            for (int i = 0; i < this.actions.size(); i++) {
                jsonObject.put("action" + i, actions.get(i).toJSON());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

public void AddAction(Action action){
    this.actions.add(action);
}

}

