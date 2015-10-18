package share.fair.fairshare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Created by Nir on 13/10/2015.
 */
public class Action implements Serializable {
    ArrayList<Operation> operations = new ArrayList<Operation>();
    long timeStamp;
    private String description;
    private String creatorName;
    private String creatorId;
    private String actionId;


    public Action(String creatorName, String creatorId,String description) {
        this.timeStamp = System.currentTimeMillis();
        this.description = description;
        this.actionId =new BigInteger(130, new SecureRandom()).toString(32).substring(0,10);
        this.creatorName=creatorName;
        this.creatorId=creatorId;
    }
    public Action(JSONObject jsonAction) {
        try {
            this.description = jsonAction.getString("description");
            this.timeStamp = jsonAction.getLong("timeStamp");
            this.actionId = jsonAction.getString("actionId");
            this.creatorName = jsonAction.getString("creatorName");
            this.creatorId = jsonAction.getString("creatorId");
            JSONArray jsonOperations = jsonAction.getJSONArray("operations");
            for (int i = 0; i < jsonOperations.length(); i++) {
                operations.add(new Operation(jsonOperations.getJSONObject(i)));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getActionId() {
        return actionId;
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
            jsonObject.put("actionId",this.actionId);
            jsonObject.put("creatorName", this.creatorName);
            jsonObject.put("creatorId", this.creatorId);
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

