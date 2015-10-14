package share.fair.fairshare;

import android.content.Context;
import android.util.Log;

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
        File file = new File(context.getFilesDir(), localGroupKey);
        if (file.exists()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String rawLine = bufferedReader.readLine();
                JSONObject jsonGroup = new JSONObject(rawLine);
                String name = jsonGroup.getString("name");
                String cloudGroupKey = jsonGroup.getString("cloudGroupKey");
                ArrayList<User> users = User.parseUsers(jsonGroup.getJSONObject("users"));
                int userIdCounter = jsonGroup.getInt("userIdCounter");
                GroupLog groupLog= new GroupLog(jsonGroup.getJSONObject("groupLog"));

                Group loadedGroup = new Group(name);
                loadedGroup.setCloudGroupKey(cloudGroupKey);
                loadedGroup.setUsers(users);
                loadedGroup.setLocalGroupKey(localGroupKey);
                loadedGroup.setUserIdCounter(userIdCounter);
                loadedGroup.setGroupLog(groupLog);

                return loadedGroup;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
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
        File oldFile = new File(context.getFilesDir(), this.localGroupKey);
        File newFile = new File(context.getFilesDir(), "tempFile");
        try {
            newFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
            writer.write(toJSONObject().toString());
            writer.close();
            if (oldFile.exists()) {
                oldFile.delete();
            }
            boolean successful = newFile.renameTo(oldFile);
            if (!successful) {
                //todo: handle problem;
                Log.w("custom", "can't rename file");
                throw new Exception();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


