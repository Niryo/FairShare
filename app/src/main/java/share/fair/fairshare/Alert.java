package share.fair.fairshare;

import com.orm.SugarRecord;

/**
 * This class encapsulate that alert mechanism
 */
public class Alert {
    /**
     * This class represents an alert
     */
    public static class AlertObject {
        public String description; //short alert description
        public double paid; //the payment
        public String useNrame; //the user's name

        public AlertObject(String description, double paid, String useNrame) {
            this.description = description;
            this.paid = paid;
            this.useNrame = useNrame;
        }

    }

    /**
     * This class represents a notified ID record
     */
    public static class NotifiedId extends SugarRecord<NotifiedId> {
        public String userId;

        public NotifiedId() {

        }

        public NotifiedId(String userId) {
            this.userId = userId;
        }
    }

}
