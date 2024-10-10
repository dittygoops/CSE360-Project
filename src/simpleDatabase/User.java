/**
 * This file contains the class hierarchy for the user management and role system of the CSE 360 Help Application.
 * 
 * The hierarchy includes:
 * - User: The base class for all user types, containing common attributes and methods.
 * - Admin: Extends User, adding administrative capabilities like user management and account operations.
 * - Student: Extends User, representing a student user with specific student-related functionalities
 * 		(not included in Phase I deliverable).
 * - Instructor: Extends User, representing an instructor user with specific instructor-related functionalities
 * 		(not included in Phase I deliverable).
 *
 * @author Isabella Swanson
 * @version 1.0
 * @since 10/9/2024
 */

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/* .......... User Superclass ............*/
public class User {
	private String username;
	private String password;
	private String email;
	private String firstName;
	private String middleName;
	private String lastName;
	private String prefName;
	private List<String> roles;
	private boolean otpFlag;
	private LocalDateTime otpExpiration;
	private Map<String, String> topicExpertise; 
	
	public User(String username, String password, String email, String firstName, 
			String middleName, String lastName, String prefName, List<String> roles,
			 boolean otpFlag, LocalDateTime otpExpiration, 
			 Map<String, String> topicExpertise) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.prefName = prefName;
        this.roles = roles;
        this.otpFlag = otpFlag;
        this.otpExpiration = otpExpiration;
        this.topicExpertise = topicExpertise;
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

    public List<String> getRoles() {
        return roles;
    }

    public boolean isOneTimePasswordFlag() {
        return otpFlag;
    }

    public LocalDateTime getOneTimePasswordExpiration() {
        return otpExpiration;
    }

    public Map<String, String> getTopicExpertise() {
        return topicExpertise;
    }
    // Method to get the full name
    public String getFullName() {
        return firstName + " " + (middleName != null && !middleName.isEmpty() ? middleName + " " : "") + lastName;
    }

    // Method to get the display name (preferred name if set, otherwise first name)
    public String getDisplayName() {
        return prefName != null && !prefName.isEmpty() ? prefName : firstName;
    }
    
    public void logout() {
        // Logic for logging out
    }

    public void finishAccountSetup() {
        // Logic to complete the account setup process
    }

    public void selectRole(String role) {
        // Logic to select a role for the current session
    }
}
/* .......... End Of User Superclass ............*/


/* .......... Admin Subclass ............*/
class Admin extends User {
	public Admin(String username, String password, String email,
            String firstName, String middleName, String lastName, String prefName,
            List<String> roles, boolean otpFlag, LocalDateTime otpExpiration,
            Map<String, String> topicExpertise) {
	super(username, password, email, firstName, middleName, lastName, prefName,
         roles, otpFlag, otpExpiration, topicExpertise);
	}
   
	public String inviteUser(String email, List<String> roles) {
       // Generate a one-time invitation code
       String invitationCode = generateInvitationCode();
       // Logic to send invitation email with the code
       // Store the invitation code and associated roles
       return invitationCode;
   }
	
	public void resetUserAccount(String username) {
       // Generate a one-time password
       String oneTimePassword = generateOneTimePassword();
       // Set expiration date and time (30 min from now)
       LocalDateTime expiration = LocalDateTime.now().plusHours(24);
       // Update user account with one-time password and expiration
       // Logic to send the one-time password to the user
   }

	public boolean deleteUserAccount(String username) {
       // Display "Are you sure?" message and check for "Yes" response
       if (confirmDeletion()) {
           // Logic to delete the user account
           return true;
       }
       return false;
   }

	public List<UserSummary> listUserAccounts() {
       // Logic to retrieve and return a list of user summaries
       // Each summary contains username, full name, and role codes
       return null; // Placeholder
   }

	public boolean addUserRole(String username, String role) {
       // Logic to add a role to a user
       return false; // Placeholder
   }

	public boolean removeUserRole(String username, String role) {
       // Logic to remove a role from a user
       return false; // Placeholder
   }

	private String generateInvitationCode() {
       // Logic to generate a unique invitation code
       return ""; // Placeholder
   }

	private String generateOneTimePassword() {
       // Logic to generate a secure one-time password
       return ""; // Placeholder
   }

	private boolean confirmDeletion() {
       // Logic to display confirmation message and get user response
       return false; // Placeholder
   }
}
/* .......... End of Admin Subclass ............*/


/* .......... Student Subclass ............*/
class Student extends User {
	public Student(String username, String password, String email,
            String firstName, String middleName, String lastName, String prefName,
            List<String> roles, boolean otpFlag, LocalDateTime otpExpiration,
            Map<String, String> topicExpertise) {
	super(username, password, email, firstName, middleName, lastName, prefName,
         roles, otpFlag, otpExpiration, topicExpertise);
	}

	public void accessHomePage() {
		// Logic to display the student's home page
	}
}
/* .......... End of Student Subclass ............*/


/* .......... Instructor Subclass ............*/
class Instructor extends User {
	public Instructor(String username, String password, String email,
            String firstName, String middleName, String lastName, String prefName,
            List<String> roles, boolean otpFlag, LocalDateTime otpExpiration,
            Map<String, String> topicExpertise) {
	super(username, password, email, firstName, middleName, lastName, prefName,
         roles, otpFlag, otpExpiration, topicExpertise);
	}
	
	public void accessHomePage() {
        // Logic to display the instructor's home page
    }
	
}
/* .......... End of Instructor Subclass ............*/


// Utility class for user account summaries
class UserSummary {
	private String username;
	private String fullName;
	private List<String>roleCodes;
	
	// Constructor, getters, setters
}
