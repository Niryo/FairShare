package share.fair.fairshare;

import com.orm.SugarRecord;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Ori on 10/15/2015.
 */
public class Operation extends SugarRecord<Operation> implements Serializable {
    public String username;
    public String userId;
    public double paid;
    public double share;
    Long belongingActionId;

    public Operation() {
    }


    public Operation(String userId, String username, double paid, double share, Long actionId) {
        this.userId = userId;
        this.username = username;
        this.paid = paid;
        this.share = share;
        this.belongingActionId = actionId;
    }

    public Operation(JSONObject jsonOperation) {
        try {
            this.username = jsonOperation.getString("username");
            this.userId = jsonOperation.getString("userId");
            this.paid = jsonOperation.getDouble("paid");
            this.share = jsonOperation.getDouble("share");
            this.belongingActionId = jsonOperation.getLong("belongingActionId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public double getPaid() {
        return paid;
    }

    public void setPaid(double paid) {
        this.paid = paid;
    }

    public double getShare() {
        return share;
    }

    public void setShare(double share) {
        this.share = share;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
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

