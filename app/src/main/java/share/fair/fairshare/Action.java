package share.fair.fairshare;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

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
    public List<Operation> operations = new ArrayList<Operation>();

    private long timeStamp; //a time stamp for the log
    private String groupId; //the group's ID this action belongs to
    private String groupName; //the group's name this action belongs to
    private String installationId; //the group's installation ID this action belongs to
    private String description; //short description of the action
    private String creatorName; //who created the action
    private String creatorId; //the ID of the creator
    private String actionId; //Unique ction ID
    private boolean isEditable; //is the action editable or not

    /**
     * Constructor
     *
     * @param creatorName the name of the creator of this action
     * @param creatorId   the ID of the creator of this action
     * @param description short description of this action
     */
    public Action(String creatorName, String creatorId, String description) {
        this.timeStamp = System.currentTimeMillis();
        this.description = description;
        this.actionId = new BigInteger(130, new SecureRandom()).toString(32).substring(0, 10);
        this.creatorName = creatorName;
        this.creatorId = creatorId;
        this.isEditable = true;
        operations = new ArrayList<>();
    }

    public Action() {
    }

    /**
     * Constructs an action from a JSON format
     *
     * @param jsonAction the action
     */
    public Action(JSONObject jsonAction) {
        try {
            this.isEditable = jsonAction.getBoolean("isEditable");
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

    /**
     * Check if the action is safe for consume.
     * An action is legal if it effects only users that are currently in the group
     *
     * @param users list of user in the group
     * @return true if the action is safe for being consumed
     */
    public boolean isLegal(List<User> users) {
        for (Operation operation : operations) {
            boolean operationIsLegal = false;
            for (User user : users) {
                if (user.getUserId().equals(operation.getUserId())) {
                    operationIsLegal = true;
                    break;
                }
            }
            if (!operationIsLegal) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method tell us if an action is editable or not
     *
     * @return true if the action is editable
     */
    public boolean isEditable() {
        return isEditable;
    }

    /**
     * Makes the action uneditable
     *
     * @param sendToCloud a flag that indicates if we need to report the action to the cloud
     */
    public void makeUneditable(boolean sendToCloud) {
        this.isEditable = false;
        if (sendToCloud) {
            sendUnEditableCommandToCloud();
        }
        save();
    }

    /**
     * Sends to the cloud an UNEDITABLE command
     */
    private void sendUnEditableCommandToCloud() {
        ParseObject parseGroupLog = new ParseObject(groupId);
        parseGroupLog.put("action", "UNEDITED_ACTION");
        parseGroupLog.put("actionId", getActionId());
        parseGroupLog.put("creatorId", installationId);

        parseGroupLog.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    reportActionViaPush();
                }
            }
        });
    }

    /**
     * Report the other users that an action has been done
     */
    private void reportActionViaPush() {
        ParsePush push = new ParsePush();
        push.setChannel(groupId);
        JSONObject jsonToPush = new JSONObject();
        try {
            jsonToPush.put("alertType", "ACTION_CHANGE");
            jsonToPush.put("installationId", installationId);
            jsonToPush.put("groupName", groupName);
            jsonToPush.put("groupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (Operation operation : getOperations()) {
            try {
                jsonToPush.put(operation.getUserId(), true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        push.setMessage(jsonToPush.toString());
        push.sendInBackground();
    }

    /**
     * Sends a NEW-ACTION command to the cloud
     */
    public void sendActionToCloud() {
        ParseObject parseGroupLog = new ParseObject(groupId);
        parseGroupLog.put("action", "NEW_ACTION");
        parseGroupLog.put("jsonAction", toJSON());
        parseGroupLog.put("actionId", getActionId());
        parseGroupLog.put("creatorId", getCreatorId());
        parseGroupLog.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    reportActionViaPush();
                }
            }
        });
    }

    /**
     * Get creator's name
     *
     * @return creator's name
     */
    public String getCreatorName() {
        return creatorName;
    }

    /**
     * Sets the group this action belongs to
     *
     * @param groupId        group's ID
     * @param groupName      group's name
     * @param installationId group's installation ID
     */
    public void setGroup(String groupId, String groupName, String installationId) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.installationId = installationId;
    }

    /**
     * Init the action, fetch all the operations from memory.
     */
    public void init() {
        operations = Operation.find(Operation.class, "belonging_action_id = ?", actionId);
    }

    /**
     * Get creator's ID
     *
     * @return
     */
    public String getCreatorId() {
        return creatorId;
    }

    /**
     * Get action's ID
     *
     * @return
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Get action's description
     *
     * @return action's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set description
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get operations
     *
     * @return list of operations
     */
    public List<Operation> getOperations() {
        return operations;
    }

    /**
     * Set operations
     *
     * @param operations list of operations
     */
    public void setOperations(ArrayList<Operation> operations) {
        this.operations = operations;
    }

    /**
     * Add new operation
     *
     * @param userId            user Id
     * @param username          user name
     * @param amountPaid        how much the user paid
     * @param userShare         the user's share
     * @param hasUserAddedShare is the user enter share or is the share has been calculated automaticly
     */
    public void addOperation(String userId, String username, double amountPaid, double userShare, boolean hasUserAddedShare) {
        this.operations.add(new Operation(userId, username, amountPaid, userShare, hasUserAddedShare));
    }

    /**
     * Converts the action to JSON format
     *
     * @return a json object representing the action
     */
    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("isEditable", this.isEditable);
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

    /**
     * Save the action and the operation inside it
     */
    @Override
    public void save() {
        super.save();

        for (Operation operation : operations) {
            operation.setBelongingActionId(actionId);
            operation.save();
        }
    }

    /**
     * Get time stamp
     *
     * @return time stamp
     */
    public long getTimeStamp() {
        return timeStamp;
    }


    /**
     * Delete the action and all it's operations
     */
    @Override
    public void delete() {
        super.delete();
        for (Operation operation : operations) {
            operation.delete();
        }
    }

    /**
     * Get installation id.
     * This method returns the installation id.
     * the installationId is a unique string that is being created for every group
     * when we join the or create the group. if we rejoin a group we a new installationId will
     * be created.
     *
     * @return installation ID
     */
    public String getInstallationId(){
        return this.installationId;
    }
}


