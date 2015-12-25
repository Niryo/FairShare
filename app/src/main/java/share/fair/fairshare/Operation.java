package share.fair.fairshare;

import com.orm.SugarRecord;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * A class that represents a single operation on user balance
 */
public class Operation extends SugarRecord<Operation> implements Serializable {
    public String username; //the user's name this operation will act on
    public String userId; // the user's Id
    public double paid; //how much the user paid
    public double share; //user's share
    boolean hasUserAddedShare; //is the user added share manually or is the share calculated automatically
    String belongingActionId; //the action this operation belongs to

    public Operation() {
    }

    /**
     * Constructs new operations
     *
     * @param userId            the user's Id
     * @param username          the user's name
     * @param paid              amount paid
     * @param share             user's share
     * @param hasUserAddedShare should be true if the share entered manually
     */
    public Operation(String userId, String username, double paid, double share, boolean hasUserAddedShare) {
        this.userId = userId;
        this.username = username;
        this.paid = paid;
        this.share = share;
        this.hasUserAddedShare = hasUserAddedShare;
    }

    /**
     * Constructs new operation from JSON objects
     *
     * @param jsonOperation An operation in JSON format
     */
    public Operation(JSONObject jsonOperation) {
        try {
            this.hasUserAddedShare = jsonOperation.getBoolean("hasUserAddedShare");
            this.username = jsonOperation.getString("username");
            this.userId = jsonOperation.getString("userId");
            this.paid = jsonOperation.getDouble("paid");
            this.share = jsonOperation.getDouble("share");
            this.belongingActionId = jsonOperation.getString("belongingActionId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get user's Id
     *
     * @return user's Id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets belonging action Id
     *
     * @param belongingActionId
     */
    public void setBelongingActionId(String belongingActionId) {
        this.belongingActionId = belongingActionId;
    }

    /**
     * Get amount paid
     *
     * @return amount paid
     */
    public double getPaid() {
        return paid;
    }

    /**
     * Sets amount paid
     *
     * @param paid amount paid
     */
    public void setPaid(double paid) {
        this.paid = paid;
    }

    /**
     * Get share
     *
     * @return share
     */
    public double getShare() {
        return share;
    }

    /**
     * Set share
     *
     * @param share share
     */
    public void setShare(double share) {
        this.share = share;
    }


    /**
     * Get hasShare
     *
     * @return true if the user entered the share manually
     */
    public boolean getHasShare() {
        return this.hasUserAddedShare;
    }

    /**
     * Convert this opertaion to JSON format
     *
     * @return
     */
    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("hasUserAddedShare", this.hasUserAddedShare);
            jsonObject.put("username", this.username);
            jsonObject.put("userId", this.userId);
            jsonObject.put("paid", this.paid);
            jsonObject.put("share", this.share);
            jsonObject.put("belongingActionId", belongingActionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}

