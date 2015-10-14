package share.fair.fairshare;

import android.content.Context;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nir on 09/10/2015.
 */


public class Group {
    private String name;


    private ArrayList<User> users= new ArrayList<>();
    private String localGroupKey = "";
    private String cloudGroupKey = "";
    private int userIdCounter=0;

    private GroupLog groupLog= new GroupLog();

    public Group(JSONObject jsonGroup){
        try {
            String name = jsonGroup.getString("name");
            String cloudGroupKey = jsonGroup.getString("cloudGroupKey");
            ArrayList<User> users = User.parseUsers(jsonGroup.getJSONObject("users"));
            int userIdCounter = jsonGroup.getInt("userIdCounter");
            GroupLog groupLog = new GroupLog(jsonGroup.getJSONObject("groupLog"));


            this.setCloudGroupKey(cloudGroupKey);
            this.setUsers(users);
            this.setLocalGroupKey(localGroupKey);
            this.setUserIdCounter(userIdCounter);
            this.setGroupLog(groupLog);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Group(String name) {
        this.name = name;
    }

    public static ArrayList<NameAndKey> getSavedGroupNames(Context context) {
        ArrayList<NameAndKey> groupNames = new ArrayList<>();
        File file = new File(context.getFilesDir(), "groups_names");
        if (file.exists()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] nameAndKey = line.split(",");
                    String name = nameAndKey[0]; //remove new line character
                    String localGroupKey = nameAndKey[1].replace("(\\r|\\n)", "");
                    groupNames.add(new NameAndKey(name, localGroupKey));
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return groupNames;
    }

    public static Group loadGroupFromStorage(Context context, String localGroupKey) {
        Group loadedGroup=null;
        ParseQuery<ParseObject> query = ParseQuery.getQuery(localGroupKey);
        query.fromLocalDatastore();
        try {
             List<ParseObject> object =  query.find();
             loadedGroup = new Group(object.get(0).getJSONObject("group"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  loadedGroup;
    }

    public GroupLog getGroupLog() {
        return groupLog;
    }

    public void setGroupLog(GroupLog groupLog) {
        this.groupLog = groupLog;
    }

    public int getUserIdCounter() {
        return userIdCounter;
    }

    public void setUserIdCounter(int userIdCounter) {
        this.userIdCounter = userIdCounter;
    }

    public ArrayList<User> getUsers() {
        return this.users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    private JSONObject toJSONObject() {
        JSONObject jsonGroup = new JSONObject();
        try {
            jsonGroup.put("cloudGroupKey", this.cloudGroupKey);
            jsonGroup.put("name", this.name);
            jsonGroup.put("localGroupKey", this.localGroupKey);
            jsonGroup.put("userIdCounter", this.userIdCounter);
            jsonGroup.put("groupLog", this.groupLog.toJSON());

            JSONObject users = new JSONObject();
            for (int i = 0; i < this.users.size(); i++) {
                users.put("user" + i, this.users.get(i).toJSON());
            }
            jsonGroup.put("users", users);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonGroup;
    }

    public void saveGroupToStorage(Context context) {
        //if this is a new group:
        if (this.localGroupKey == "") {
            try {
                this.localGroupKey = Long.toString(System.currentTimeMillis());
                File groupNamesFile = new File(context.getFilesDir(), "groups_names");
                if (!groupNamesFile.exists()) {
                    groupNamesFile.createNewFile();
                }

                BufferedWriter groupsNamesWriter = new BufferedWriter(new FileWriter(groupNamesFile, true));
                groupsNamesWriter.write(this.name + "," + this.localGroupKey + "\n");
                groupsNamesWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ParseObject groupToSave = new ParseObject(this.localGroupKey);
        groupToSave.put("group", toJSONObject());
        try {
            groupToSave.pin();
        } catch (ParseException e) {
            e.printStackTrace();
        }

            groupToSave.saveInBackground();

    }

    public String getName() {
        return this.name;
    }

    public void addUser(Context context, User user) {
        user.setId(String.valueOf(this.userIdCounter++));
        this.users.add(user);
        saveGroupToStorage(context);
    }

    public String getCloudGroupKey() {
        return this.cloudGroupKey;

    }

    public void setCloudGroupKey(String cloudKey) {
        this.cloudGroupKey = cloudKey;
    }

    public String getLocalGroupKey() {
        return this.localGroupKey;
    }

    public void setLocalGroupKey(String localGroupKey) {
        this.localGroupKey = localGroupKey;
    }
}


