package share.fair.fairshare;

import com.orm.SugarRecord;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Nir on 09/10/2015.
 */

public class User extends SugarRecord<User> implements Serializable {
    private String name;
    private double balance;
    private String email;
    private String userId;
    private String belongingGroupId;
    private boolean isNotified;

    public User() {
    }

    public User(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public static ArrayList<User> parseUsers(JSONObject jsonUsers) {
        ArrayList<User> resultUsers = new ArrayList<>();
        try {
            Iterator<?> userNames = jsonUsers.keys();
            while (userNames.hasNext()) {
                JSONObject user = jsonUsers.getJSONObject((String) userNames.next());
                String name = user.getString("name");
                int balance = user.getInt("balance");
                String id = user.getString("userId");
                String email = user.has("email") ? user.getString("email") : "";
                User newUser = new User(name, balance);
                newUser.setEmail(email);
                newUser.setUserId(id);
                resultUsers.add(newUser);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultUsers;
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setIsNotified(boolean isNotified) {
        this.isNotified = isNotified;
    }

    public void setBelongingGroupId(String belongingGroupId) {
        this.belongingGroupId = belongingGroupId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        try {
            result.put("name", this.name);
            result.put("balance", this.balance);
            result.put("email", this.email);
            result.put("userId", this.userId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getName() {
        return this.name;
    }

    public double getBalance() {
        return this.balance;
    }

    public void addToBalance(double val) {
        this.balance += val;
        save();
    }

}
