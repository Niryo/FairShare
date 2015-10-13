package share.fair.fairshare;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Nir on 13/10/2015.
 */
public class Action implements Serializable {
    private String description;
    private HashMap<String, Double> operations=new HashMap<>(); //<userId, value to add to balance>

    public Action() {
        this.description = "...";
    }
    public Action(String description) {
        this.description = description;
    }
    public Action(JSONObject jsonAction){
            try {
        String description= jsonAction.getString("description");
        Iterator keys = jsonAction.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
                operations.put(key, (double)(jsonAction.get(key)));
        }
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, Double> getOperations() {
        return operations;
    }

    public void setOperations(HashMap<String, Double> operations) {
        this.operations = operations;
    }

    public void addOperation(String id, double value) {
        this.operations.put(id, value);
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("description", this.description);
            jsonObject.put("operations", new JSONObject(this.operations));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    
  
}

