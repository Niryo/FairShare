package share.fair.fairshare;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by Nir on 22/10/2015.
 */
public class Alert {
    public static class AlertObject {
    public String description;
    public double paid;
        public String useNrame;

        public AlertObject(String description, double paid, String useNrame) {
            this.description = description;
            this.paid = paid;
            this.useNrame = useNrame;
        }

}

    public static class NotifiedId extends SugarRecord<NotifiedId>{
         public String userId;

        public NotifiedId(){

        }
        public NotifiedId(String userId){
            this.userId=userId;
        }
    }

}
