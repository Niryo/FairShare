package share.fair.fairshare;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nir on 09/10/2015.
 */



public class Group {
    private String name;
    private ArrayList<User> users;
    private String id;
    private String localGroupKey;
    private String cloudGroupKey;

    private Group(String name, String id, ArrayList<User> users,String localGroupKey){
    this.name= name;
        this.users=users;
        this.id=id;
        this.localGroupKey = localGroupKey;
    }

    public Group(String name){
        this.name= name;
        this.users=new ArrayList<>();
        this.id="";
        this.localGroupKey = "";
    }



    public static ArrayList<Map<String, String>> getSavedGroupNames(Context context){
        ArrayList<Map<String,String>> groupNames=new ArrayList<>();
        File file = new File(context.getFilesDir(),"groups_names");
        if(file.exists()){
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Map<String,String> map = new HashMap<>();
                    String[] nameAndKey =line.split(",");
                    String name = nameAndKey[0]; //remove new line character
                    String localGroupKey = nameAndKey[1].replace("(\\r|\\n)", "");
                    map.put(name, localGroupKey);
                    groupNames.add(map);
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
                StringBuilder stringBuilder = new StringBuilder();
                String rawLine = bufferedReader.readLine();
                JSONObject jsonGroup = new JSONObject(rawLine);
                String id = jsonGroup.getString("id");
                String name = jsonGroup.getString("name");
                ArrayList<User> users = User.parseUsers(jsonGroup.getJSONObject("users"));
                return new Group(name, id, users, localGroupKey);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }
    private JSONObject toJSONObject(){
        JSONObject jsonGroup= new JSONObject();
        try {
            jsonGroup.put("id", this.id);
            jsonGroup.put("name", this.name);
            jsonGroup.put("localGroupKey",this.localGroupKey);

            JSONObject users= new JSONObject();
            for(int i=0; i<this.users.size(); i++){
               users.put("user"+i, this.users.get(i).toJSON());
            }
            jsonGroup.put("users",users );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonGroup;
    }



    public void saveGroupToStorage(Context context){
        if(this.localGroupKey==""){
                try {
            this.localGroupKey=Long.toString(System.currentTimeMillis());
            File groupNamesFile = new File(context.getFilesDir(), "groups_names");
            if(!groupNamesFile.exists()){
                    groupNamesFile.createNewFile();
            }
                    BufferedWriter writer = new BufferedWriter(new FileWriter(groupNamesFile));
                    writer.write(this.name+","+this.localGroupKey);

                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        File oldFile = new File(context.getFilesDir(), this.localGroupKey);
        File newFile = new File(context.getFilesDir(),"tempFile");
            try {
                newFile.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
                writer.write(toJSONObject().toString());
                writer.close();
                if(oldFile.exists()){
                oldFile.delete();}
                 boolean successful = newFile.renameTo(oldFile);
                if(!successful){
                    //todo: handle problem;
                    Log.w("custom", "can't rename file");
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }


//        public void setName(String name){
//        this.name=name;
//    }
//    public void setUsers(ArrayList<User> users ){
//        this.users=users;
//    }


}


