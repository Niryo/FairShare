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
import java.util.ArrayList;

/**
 * Created by Nir on 09/10/2015.
 */



public class Group {
    private String name;
    private ArrayList<User> users;
    private String id;
    private int index;
    private Group(String name, String id, ArrayList<User> users,int index){
    this.name= name;
        this.users=users;
        this.id=id;
        this.index=index;
    }

    private Group(String name,int index){
        this.name= name;
        this.users=new ArrayList<>();
        this.id="";
        this.index=index;
    }



    public static ArrayList<String> getSavedGroupNames(Context context){
        ArrayList<String> groupNames=new ArrayList<>();
        File file = new File(context.getFilesDir(),"groups_names");
        if(file.exists()){
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String name = line.replace("(\\r|\\n)", ""); //remove new line character
                    groupNames.add(name);
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
            return groupNames;
    }
    public static Group loadGroupFromStorage(Context context, int index) {
        File file = new File(context.getFilesDir(), "groups");
        if (file.exists()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line = null;
                for (int i = 0; i <= index; i++) {
                    line = bufferedReader.readLine();
                }

                JSONObject jsonGroup = new JSONObject(line);
                String id = jsonGroup.getString("id");
                String name = jsonGroup.getString("name");
                ArrayList<User> users = User.parseUsers(jsonGroup.getJSONObject("users"));
                return new Group(name, id, users, index);
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
            jsonGroup.put("index",this.index);

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
        File file = new File(context.getFilesDir(), "groups");
        File tempFile = new File("tempFile");
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
                String line = null;
                int lineNumber=0;
                while((line = reader.readLine()) != null) {
                    if(lineNumber==index){
                        writer.write(toJSONObject().toString()+System.getProperty("line.separator"));
                        continue;
                    }
                    writer.write(line);
                }
                writer.close();
                reader.close();
                boolean successful = tempFile.renameTo(file);
                if(!successful){
                    //todo: handle problem;
                    Log.w("custom", "can't rename file");
                    throw new Exception();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


//        public void setName(String name){
//        this.name=name;
//    }
//    public void setUsers(ArrayList<User> users ){
//        this.users=users;
//    }




}


