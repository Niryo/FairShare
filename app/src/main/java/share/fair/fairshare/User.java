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

    public static ArrayList<User> parseUsers(String rawString){
        ArrayList<User> resultUsers=new ArrayList<>();
        try {
            JSONObject jsonUsers = new JSONObject(rawString);
            Iterator<?> userNames = jsonUsers.keys();
            while(userNames.hasNext()){
                String name = (String) userNames.next();
                int balance= jsonUsers.getInt(name);
                resultUsers.add(new User(name,balance));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultUsers;
    }
}
