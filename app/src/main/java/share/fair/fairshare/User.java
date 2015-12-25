package share.fair.fairshare;

import com.orm.SugarRecord;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * This class represents a user
 */

public class User extends SugarRecord<User> implements Serializable {
    private String userName; //user's name
    private double balance; //user's balance
    private String userId; //user's Id
    private boolean isNotified; //indicates if we need to be notified on changes to this user's balance
    private boolean isGhost; //indicates if the user is ghost
    // private List<Long> ghostActionIdHistory;
    private String belongingGroupId; //the group this user belongs to


    public User() {
    }

    /**
     * Constructor
     *
     * @param userName
     * @param userId
     * @param balance
     */
    public User(String userName, String userId, double balance, boolean isGhost) {
        if (userId == null) {
            this.userId = new BigInteger(130, new SecureRandom()).toString(32).substring(0, 6);
        } else {
            this.userId = userId;
        }
        if (isGhost) {
            this.userName = userName + " (ghost)";
        } else {
            this.userName = userName;
        }
        this.isGhost = isGhost;
        this.balance = balance;
        //    this.ghostActionIdHistory=null;
    }


    /**
     * Get isGhost
     *
     * @return isGhost
     */
    public boolean isGhost() {
        return isGhost;
    }

    /**
     * Returns true if the user is marked as notified user
     *
     * @return true if the user is marked as notified user
     */
    public boolean isNotified() {
        return isNotified;
    }

    /**
     * Set isNotified
     *
     * @param isNotified
     */
    public void setIsNotified(boolean isNotified) {
        this.isNotified = isNotified;
    }

    /**
     * Set belonging group ID
     *
     * @param belongingGroupId
     */
    public void setBelongingGroupId(String belongingGroupId) {
        this.belongingGroupId = belongingGroupId;
    }

    /**
     * Get user's ID
     *
     * @return user's ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get user's name
     *
     * @return user's name
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Get user's balance
     *
     * @return user's balance
     */
    public double getBalance() {
        return this.balance;
    }

    /**
     * Add value to the user balance
     *
     * @param value the value to add
     */
    public void addToBalance(double value) {
        this.balance += value;
        save();
    }
}
