package simpleDatabase;

import java.sql.SQLException;
import java.time.LocalDateTime;

/* .......... User Superclass ............*/
/**
 * The User class serves as the base class for all types of users in the system.
 * It contains common attributes such as username, password, email, and other personal details.
 */
public class User {
    private String username;       // The user's unique identifier
    private String password;       // The user's password
    private String email;          // The user's email address
    private String firstName;      // The user's first name
    private String middleName;     // The user's middle name (optional)
    private String lastName;       // The user's last name
    private String prefName;       // The user's preferred name
    private boolean aFlag;          // The roles assigned to the user
    private boolean tFlag;
    private boolean sFlag;
    private boolean otpFlag;       // Indicates if a one-time password is required
    private LocalDateTime otpExpiration; // The expiration time for the one-time password

    /**
     * Constructor for creating a new User instance.
     *
     * @param username        The user's unique identifier
     * @param password        The user's password
     * @param email           The user's email address
     * @param firstName       The user's first name
     * @param middleName      The user's middle name (optional)
     * @param lastName        The user's last name
     * @param prefName        The user's preferred name
     * @param roles           The roles assigned to the user
     * @param otpFlag         Indicates if a one-time password is required
     * @param otpExpiration    The expiration time for the one-time password
     */
    public User(String username, String password, String email, String firstName,
                String middleName, String lastName, String prefName, boolean aFlag, boolean tFlag, boolean sFlag,
                boolean otpFlag, LocalDateTime otpExpiration) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.prefName = prefName;
        this.aFlag = aFlag;
        this.tFlag = tFlag;
        this.sFlag = sFlag;
        this.otpFlag = otpFlag;
        this.otpExpiration = otpExpiration;
    }

    // Getter methods
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPreferredName() {
        return prefName;
    }

    public boolean[] getRoles() {
        boolean[] res = {aFlag, tFlag, sFlag};
        return res;
    }

    public String getPassword() {
        return password;
    }

    // Setter methods
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPreferredName(String prefName) {
        this.prefName = prefName;
    }

    public void setRoles(boolean[] flags) {
        this.aFlag = flags[0];
        this.tFlag = flags[1];
        this.sFlag = flags[2];
    }

    public void setOTPFlag(boolean otpFlag) {
        this.otpFlag = otpFlag;
    }

    public void setOneTimePasswordExpiration(LocalDateTime otpExpiration) {
        this.otpExpiration = otpExpiration;
    }
    
    public boolean getOTP() {
        return otpFlag;
    }

    public String getPreferredFirst() {
        return prefName;
    }

    public LocalDateTime getOneTimePasswordExpiration() {
        return otpExpiration;
    }
    

    /**
     * Method to retrieve the full name of the user, combining first, middle (if present), and last names.
     *
     * @return The full name of the user
     */
    public String getFullName() {
        return firstName + " " + (middleName != null && !middleName.isEmpty() ? middleName + " " : "") + lastName;
    }

    /**
     * Method to get the display name of the user.
     * Returns the preferred name if set; otherwise, returns the first name.
     *
     * @return The display name of the user
     */
    public String getDisplayName() {
        return prefName != null && !prefName.isEmpty() ? prefName : firstName;
    }
    
    /**
     * Method to log out the user. 
     * (Logic for logging out should be implemented.)
     */
    public void logout() {
        // Logic for logging out
    }

    /**
     * Method to complete the account setup process.
     * (Logic for account setup should be implemented.)
     */
    public void finishAccountSetup() {
        // Logic to complete the account setup process
    }

    /**
     * Method to select a role for the current session.
     * 
     * @param role The role to be selected
     */
    public void selectRole(String role) {
        // Logic to select a role for the current session
    }
}
/* .......... End Of User Superclass ............*/


/* .......... Admin Subclass ............*/
/**
 * The Admin class extends the User class to provide additional administrative capabilities.
 * This includes managing users, sending invitations, and handling account operations.
 */
class Admin extends User {
    public DatabaseHelper databaseHelper;
    /**
     * Constructor for creating a new Admin instance.
     *
     * @param username        The admin's unique identifier
     * @param password        The admin's password
     * @param email           The admin's email address
     * @param firstName       The admin's first name
     * @param middleName      The admin's middle name (optional)
     * @param lastName        The admin's last name
     * @param prefName        The admin's preferred name
     * @param roles           The roles assigned to the admin
     * @param otpFlag         Indicates if a one-time password is required
     * @param otpExpiration    The expiration time for the one-time password
     */
    public Admin(String username, String password, String email, String firstName, String middleName, String lastName, String prefName,
    boolean aFlag, boolean tFlag, boolean sFlag, boolean otpFlag, LocalDateTime otpExpiration) {
        super(username, password, email, firstName, middleName, lastName, prefName,
     aFlag, tFlag, sFlag, otpFlag, otpExpiration);
        // initiliaze DatabaseHelper object
        databaseHelper = new DatabaseHelper();
    }

    /**
     * Invites a new user by generating a one-time invitation code and sending an invitation email.
     *
     * @param email The email address of the user to invite
     * @param roles A list of roles to assign to the invited user
     * @return The generated invitation code
     */
    public String inviteUser(String email, String roles) {
        // generate otp code and return
        String invitationCode = databaseHelper.createOTP(roles);
        return invitationCode;
    }

    /**
     * Resets a user account by generating a one-time password and setting an expiration time.
     *
     * @param username The username of the account to reset
     */
    public String resetUserAccount(String username) {
        // get roles from the username
        String roles = databaseHelper.getUserRoles(username);
        // generate otp code and return
        databaseHelper.createOTP(roles);
        return "";
    }

    /**
     * Deletes a user account after confirming deletion with the admin.
     *
     * @param username The username of the account to delete
     * @return True if the account was successfully deleted, otherwise false
     */
    public boolean deleteUserAccount(String username) throws SQLException {
        // delete user acount
        return databaseHelper.deleteUserAccount(username);
    }
}
    /**
     * Retrieves a list of user summaries containing basic information about all users.
     * @return A list of user summaries
     */
    /*
    public List<UserSummary> getUserSummaries() {
        // get user summaries
        return databaseHelper.getUserSummaries();
    }
    */
