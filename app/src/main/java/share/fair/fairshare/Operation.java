package share.fair.fairshare;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Ori on 10/15/2015.
 */
public class Operation implements Serializable{
    public String username;
    public String userId;
    public double paid;
    public double share;

    public Operation(JSONObject jsonOperation){
        try {
            this.username= jsonOperation.getString("username");
            this.userId =jsonOperation.getString("userId");
            this.paid = jsonOperation.getDouble("paid");
            this.share= jsonOperation.getDouble("share");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Operation(String id, String username,double paid, double share){ //user id?
        this.userId = id;
        this.username = username;
        this.paid = paid;
        this.share = share;
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", this.username);
            jsonObject.put("userId",this.userId);
            jsonObject.put("paid",this.paid);
            jsonObject.put("share",this.share);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}

