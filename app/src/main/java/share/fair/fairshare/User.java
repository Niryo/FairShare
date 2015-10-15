package share.fair.fairshare;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Nir on 09/10/2015.
 */

public class User implements Serializable{

    private String name;
    private double balance;
    private String email;
    private String id;
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
                String id = user.getString("id");
                String email = user.has("email")? user.getString("email") : "";
                User newUser= new User(name,balance);
                newUser.setEmail(email);
                newUser.setId(id);
                resultUsers.add(newUser);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultUsers;
    }

    public String getEmail() {
        return email;
    }

    public  void setEmail(String email){
        this.email=email;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("name", this.name);
            result.put("balance", this.balance);
            result.put("email",this.email);
            result.put("id",this.id);

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

    public void addToBalance(double val){
        this.balance += val;

    }

}
