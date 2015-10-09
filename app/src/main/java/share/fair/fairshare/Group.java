package share.fair.fairshare;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Nir on 09/10/2015.
 */



public class Group {
    private String name;
    private ArrayList<User> users;
    private String id;

    public Group(){}
    public ArrayList<Group> loadAllGroupsFromStorage(Context context){
        ArrayList<Group> resultGroups=new ArrayList<>();
        SharedPreferences sharedPref = context.getSharedPreferences("LOCAL_STORAGE", Context.MODE_PRIVATE);
        String rawGroup= sharedPref.getString("GROUPS","");
        if(!rawGroup.isEmpty()){
            try {
                JSONObject jsonGroups = new JSONObject(rawGroup);
                Iterator<?> groupNames = jsonGroups.keys();
                while(groupNames.hasNext()){
                    Group newGroup= new Group();
                    String name = (String) groupNames.next();
                    newGroup.setName(name);
                    newGroup.setUsers(User.parseUsers(jsonGroups.getString(name)));
                    resultGroups.add(newGroup);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
            return resultGroups;
    }
        public void setName(String name){
        this.name=name;
    }
    public void setUsers(ArrayList<User> users ){
        this.users=users;
    }




}


