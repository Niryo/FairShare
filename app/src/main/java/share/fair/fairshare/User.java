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
    private double balance;
    private double paid;
    private double share;

    public User(String name, double balance){
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
    public String getName(){
        return this.name;
    }
    public double getBalance(){
        return this.balance;
    }
    public void setPaid(double paid){
        this.paid = paid;
    }
    public void setShare(double share){
        this.share = share;
    }
    public void resetPaidAndShare(){
        this.paid = 0.0;
        this.share = 0.0;
    }
    public double getPaid(){ return this.paid;}
    public double getShare(){ return this.share;}
}
