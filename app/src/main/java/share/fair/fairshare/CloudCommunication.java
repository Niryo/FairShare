package share.fair.fairshare;

import com.firebase.client.Firebase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by niryo on 27/02/16.
 */
public class CloudCommunication {

    private String ADDRESS= "https://fairshare.firebaseio.com/";
    private Firebase groupActions;
    private static CloudCommunication instance=null;
    private FairShareGroup group;

     private CloudCommunication(){
         final Firebase ref= new Firebase(ADDRESS);

//         ref.addListenerForSingleValueEvent(new ValueEventListener() {
//             @Override
//             public void onDataChange(DataSnapshot dataSnapshot) {
//                 Log.w("custom", dataSnapshot.getValue().toString());
//             }
//
//             @Override
//             public void onCancelled(FirebaseError firebaseError) {
//                 Log.w("custom", "error reading firebase");
//                 Log.w("custom", firebaseError.getMessage());
//                 Log.w("custom", firebaseError.getDetails());
//
//             }
//         });
     }

    public static CloudCommunication getInstance(){
        if(instance==null){
            instance = new CloudCommunication();
        }
        return instance;
    }

    public void setCurrentGroup(FairShareGroup group){
        this.group=group;
        this.groupActions = new Firebase(ADDRESS+group.getCloudGroupKey()).child("Actions");
    }

//    public void createNewGroup(String groupKey){
//        Firebase ref = new Firebase(ADDRESS);
//        Map<String,String> emptyGroup
//        ref.child(groupKey).setValue();
//    }

    public void sendUserAddedCommand(User user){
        Map<String, String> data =new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("userName", user.getUserName());
        data.put("userBalance", Double.toString(user.getBalance()));
        data.put("action", "USER_ADDED");
        data.put("installationId", this.group.getInstallationId());
        data.put("timeStamp", Long.toString(System.currentTimeMillis()));
        groupActions.push().setValue(data);

//        ParseObject parseGroup = new ParseObject(this.cloudGroupKey);
//        parseGroup.put("userId", user.getUserId());
//        parseGroup.put("userName", user.getUserName());
//        parseGroup.put("userBalance", user.getBalance());
//        parseGroup.put("action", "USER_ADDED");
//        parseGroup.put("creatorId", installationId);
//        parseGroup.saveEventually(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e == null) {
//                    reportUserChangeViaPush();
//                    Toast.makeText(context, "user saved in cloud", Toast.LENGTH_LONG).show();
//                } else {
//                    e.printStackTrace();
//                    Toast.makeText(context, "user hasn't been saved in cloud", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
    }
}
