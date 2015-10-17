package share.fair.fairshare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Nir on 13/10/2015.
 */
public class Action implements Serializable {
    ArrayList<Operation> operations = new ArrayList<Operation>();
    long timeStamp;
    private String description;
    private String creatorId;


    public Action() {
        this.timeStamp = System.currentTimeMillis();
        this.description = "...";

    }

    public Action(String description) {
        this.timeStamp = System.currentTimeMillis();
        this.description = description;

    }

    public Action(JSONObject jsonAction) {
        try {
            this.description = jsonAction.getString("description");
            this.timeStamp = jsonAction.getLong("timeStamp");
            JSONArray jsonOperations = jsonAction.getJSONArray("operations");
            for (int i = 0; i < jsonOperations.length(); i++) {
                operations.add(new Operation(jsonOperations.getJSONObject(i)));
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
        this.operations.add(new Operation(id, username, paid, share));
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("description", this.description);
            jsonObject.put("timeStamp", this.timeStamp);
            JSONArray jsonOperations = new JSONArray();
            for (Operation operation : operations) {
                jsonOperations.put(operation.toJSON());
            }
            jsonObject.put("operations", jsonOperations);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public long getTimeStamp() {
        return timeStamp;
    }
}

