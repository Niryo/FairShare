package share.fair.fairshare;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Nir on 13/10/2015.
 */
public class Action implements Serializable {
    private String description;
    ArrayList<Operation> operations;
    private String creatorId;


    public Action() {
        this.description = "...";
        this.operations = new ArrayList<Operation>();
    }
    public Action(String description) {
        this.description = description;
        this.operations = new ArrayList<Operation>();
    }
    public Action(JSONObject jsonAction){
        try {
            this.description= jsonAction.getString("description");
            JSONObject jsonOperations= jsonAction.getJSONObject("operations");
            Iterator keys = jsonOperations.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                operations.add(new Operation(jsonOperations.getJSONObject(key)));
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

    public ArrayList<Operation> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<Operation> operations) {
        this.operations = operations;
    }

    public void addOperation(String id, String username, double paid, double share) {
        this.operations.add(new Operation(id, username, paid,share) );
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("description", this.description);
            JSONObject jsonOperations= new JSONObject();
            for(int i=0; i< operations.size(); i++){
                jsonOperations.put("operation"+i, operations.get(i).toJSON());
            }
            jsonObject.put("operations", jsonOperations);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
