package share.fair.fairshare;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Nir on 09/10/2015.
 */


public class Group implements Serializable {
    private String name;
    private ArrayList<User> users = new ArrayList<>();
    private String localGroupKey = "";
    private String cloudGroupKey = "";
    private String cloudLogKey = "";
    private int userIdCounter = 1;
    private GroupLog groupLog;


    public Group(Context context, JSONObject jsonGroup) {
        try {
            String name = jsonGroup.getString("name");
            String cloudGroupKey = jsonGroup.getString("cloudGroupKey");
            String cloudLogKey = jsonGroup.getString("cloudLogKey");
            String localGroupKey = jsonGroup.getString("localGroupKey");

            ArrayList<User> users = User.parseUsers(jsonGroup.getJSONObject("users"));
            int userIdCounter = jsonGroup.getInt("userIdCounter");
            GroupLog groupLog = new GroupLog(this,jsonGroup.getJSONObject("groupLog"));


            this.name = name;
            this.setCloudGroupKey(cloudGroupKey);
            this.setUsers(users);
            this.setLocalGroupKey(localGroupKey);
            this.setUserIdCounter(userIdCounter);
            this.setGroupLog(groupLog);
            this.setCloudLogKey(cloudLogKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Group(String name) {
        this.name = name;
    }

    private static String createLocalKey(Context context, String name) {
        String localGroupKey = Long.toString(System.currentTimeMillis());
        try {
            File groupNamesFile = new File(context.getFilesDir(), "groups_names");
            if (!groupNamesFile.exists()) {
                groupNamesFile.createNewFile();
            }

            BufferedWriter groupsNamesWriter = new BufferedWriter(new FileWriter(groupNamesFile, true));
            groupsNamesWriter.write(name + "," + localGroupKey + System.getProperty("line.separator"));
            groupsNamesWriter.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return localGroupKey;
    }

    public static Group groupBuilder(Context context, String name) {
        Group group = new Group(name);
        Date zeroDate = new Date();
        zeroDate.setTime(0);
        group.setLocalGroupKey(createLocalKey(context, name));
        String cloudGroupKey = "a" + new BigInteger(130, new SecureRandom()).toString(32);
        String cloudLogKey = "a"+ new BigInteger(130, new SecureRandom()).toString(32);
        group.setCloudGroupKey(cloudGroupKey);
        group.setCloudLogKey(cloudLogKey);
        group.setGroupLog(new GroupLog(group,cloudLogKey));
        initializeCloud(cloudGroupKey, cloudLogKey);
        group.saveGroupToStorage(context);

        return group;
    }

    public static ArrayList<NameAndKey> getSavedGroupNames(Context context) {
        ArrayList<NameAndKey> groupNames = new ArrayList<>();
        File file = new File(context.getFilesDir(), "groups_names");
        Log.w("custom",file.getAbsolutePath().toString());
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
                Group loadedGroup = new Group(context, jsonGroup);
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

    private static void initializeCloud(String cloudGroupKey, String cloudLogKey) {
        ParseObject parseGroup = new ParseObject(cloudGroupKey);
        parseGroup.pinInBackground();
        parseGroup.saveEventually();
        ParseObject parseLog = new ParseObject(cloudLogKey);
        parseLog.pinInBackground();
        parseLog.saveEventually();
    }

    public static Group joinGroupWithKey(Context context, String name, String cloudGroupKey, String cloudLogKey) {
        Group group = new Group(name);
        group.setGroupLog(new GroupLog(group, cloudLogKey));
        group.setLocalGroupKey(createLocalKey(context, name));
        group.setCloudGroupKey(cloudGroupKey);
        initializeCloud(cloudGroupKey, cloudLogKey);
        group.saveGroupToStorage(context);
        return group;
    }

    public void addUserToCloud(final Context context, User user) {
        ParseObject parseGroup = new ParseObject(this.cloudGroupKey);
        parseGroup.put("userId", user.getId());
        parseGroup.put("userName", user.getName());
        parseGroup.put("userEmail", user.getEmail());
        parseGroup.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    Toast.makeText(context, "user saved in cloud", Toast.LENGTH_LONG).show();
                }
                else{
                    e.printStackTrace();
                    Toast.makeText(context, "user hasn't been saved in cloud", Toast.LENGTH_LONG).show();
                }
            }
        });
        saveGroupToStorage(context);
    }

    public void syncUsers(final Context context) {
        final ArrayList<User> usersCopy = new ArrayList<>(users); //a copy so we wan't run on mutable list;
        ParseQuery<ParseObject> query = ParseQuery.getQuery(this.cloudGroupKey);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null) {
                    boolean dirty = false;
                    Hashtable<String, String> currentUserTable = new Hashtable<String, String>();
                    Hashtable<String, String> cloudUserTable = new Hashtable<String, String>();
                    for (User user : usersCopy) {
                        currentUserTable.put(user.getId(), ""); //name is not relevant here
                    }
                    for (ParseObject parseObject : list) {
                        String userId = parseObject.getString("userId");
                        String userName = parseObject.getString("userName");
                        if (userId != null && userName != null) {
                            cloudUserTable.put(parseObject.getString("userId"), parseObject.getString("userName"));
                        }
                    }

                    for (User user : usersCopy) {
                        if (!cloudUserTable.containsKey(user.getId())) {
                            users.remove(user);
                            dirty = true;
                        }
                    }
                    Enumeration<String> keys = cloudUserTable.keys();
                    while (keys.hasMoreElements()) {
                        String id = keys.nextElement();
                        if (!currentUserTable.containsKey(id)) {
                            User newUser = new User(cloudUserTable.get(id), 0);
                            newUser.setId(id);
                            users.add(newUser);
                            dirty = true;
                        }
                    }
                    if (dirty) {
                        saveGroupToStorage(context);
                    }
                }

            }
        });
    }

    public String getCloudLogKey() {
        return cloudLogKey;
    }

    public void setCloudLogKey(String cloudLogKey) {
        this.cloudLogKey =  cloudLogKey;
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
            jsonGroup.put("cloudLogKey", this.cloudLogKey);
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
        addUserToCloud(context, user);
    }

    public String getCloudGroupKey() {
        return this.cloudGroupKey;

    }

    public void setCloudGroupKey(String cloudKey) {
        this.cloudGroupKey = cloudKey;
    }

    public void consumeAction(Action action) {
        Hashtable<String, User> usersTable = new Hashtable<String, User>();
        for (User user : users) {
            usersTable.put(user.getId(), user);
        }
        for (Operation operation : action.getOperations()) {
            usersTable.get(operation.userId).addToBalance(operation.getPaid() - operation.getShare());//todo: check!!
        }

    }

    public String getLocalGroupKey() {
        return this.localGroupKey;
    }

    public void setLocalGroupKey(String localGroupKey) {
        this.localGroupKey = localGroupKey;
    }
}


