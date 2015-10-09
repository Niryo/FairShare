package share.fair.fairshare;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Nir on 09/10/2015.
 */
public class User{
    private String name;
    private int balance;

    private User(String name, int balance){
        this.name=name;
        this.balance = balance;
    }

    public static ArrayList<User> parseUsers(JSONObject jsonUsers){
        ArrayList<User> resultUsers=new ArrayList<>();
        try {
            Iterator<?> userNames = jsonUsers.keys();
            while(userNames.hasNext()){
                JSONObject user = jsonUsers.getJSONObject((String) userNames.next());
                String name= user.getString("name");
                int balance= user.getInt("balance");
                resultUsers.add(new User(name,balance));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultUsers;
    }

    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("name", this.name);
            result.put("balance", this.balance);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
