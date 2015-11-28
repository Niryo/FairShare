package share.fair.fairshare;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nir on 13/10/2015.
 */
public class Action extends SugarRecord<Action> implements Serializable {
    @Ignore
    List<Operation> operations = new ArrayList<Operation>();
    private Long groupLogId;
    private long timeStamp;
    private String description;
    private String creatorName;
    private String creatorId;
    private String actionId;
    private boolean isEditable;

    public Action(String creatorName, String creatorId, String description) {
        this.timeStamp = System.currentTimeMillis();
        this.description = description;
        this.actionId = new BigInteger(130, new SecureRandom()).toString(32).substring(0, 10);
        this.creatorName = creatorName;
        this.creatorId = creatorId;
        this.isEditable=true;
        operations = new ArrayList<>();
    }

    public Action() {
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

    public boolean isEditable() {
        return isEditable;
    }

    public void makeUneditable() {
        this.isEditable = false;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setGroupLogId(Long groupLogId) {
        this.groupLogId = groupLogId;
    }

    public void init() {
        if(getId()==null){save();};
        operations = Operation.find(Operation.class, "belonging_action_id = ?", Long.toString(getId()));


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

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<Operation> operations) {
        this.operations = operations;
    }

    public void addOperation(String id, String username, double paid, double share , boolean hasUserAddedShare) {
        this.operations.add(new Operation(id, username, paid, share, hasUserAddedShare));
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("description", this.description);
            jsonObject.put("timeStamp", this.timeStamp);
            jsonObject.put("actionId", this.actionId);
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

    @Override
    public void save() {
        super.save();

        for (Operation operation : operations) {
            operation.setBelongingActionId(getId());
            operation.save();
        }
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public void delete() {
        super.delete();
        for(Operation operation : operations){
            operation.delete();
        }
    }
}


