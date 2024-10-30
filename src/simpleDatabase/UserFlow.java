package simpleDatabase;


import java.sql.SQLException;
import java.util.Scanner;



public class UserFlow {
    public static final Scanner scanner = new Scanner(System.in);

    // Constructor for UserFlow class
    public UserFlow() {
        

    }


    /*
	 * Homepage for students/instructors
	 */
	private static void commonHome() throws SQLException {
		System.out.println("Welcome to the home page of either a Student or Instructor.");
		System.out.println("At this time, you can only perform one action - Logout. However, you are welcome to sit here for however long you like.");
		System.out.print("To Logout, Enter q: ");
		String logout = scanner.nextLine();
		if(logout.equals("q")) {
			System.out.println("You have successfully logged out. See you next time!");
			mainLogin();
		} 
		while(!logout.equals("q")) {
			System.out.print("Invalid input. To Logout, Enter q: ");
			logout  = scanner.nextLine();
		}
		
	}

    /**
     * Main login page for the CSE 360 Help Application.
     * 
     */
    private static void mainLogin() throws SQLException {
	    String choice = "";
	    String userName = "";
	    String password = "";
	    String oTP = "";

	    // Input for returning user and deals with invalid input
	    System.out.print("Are you a returning user? (Note - If you had your account reset, choose the second option) 1. Yes 2. No: ");
	    choice = scanner.nextLine();

	    while (!choice.equals("1") && !choice.equals("2")) {
	        System.out.println("Invalid option selected. Please try again");
	        System.out.print("Are you a returning user? 1. Yes 2. No: ");
	        choice = scanner.nextLine();
	    }

	    // Choice 1: Returning user
	    if (choice.equals("1")) {
	        while (true) {
	            String[] credentials = get_user_credentials();  // Get username and password
	            userName = credentials[0];
	            password = credentials[1];

	            // Check if user exists and credentials are valid
	            boolean doesUserExist = databaseHelper.doesUserExist(userName);
	            if(doesUserExist) {
	            	User user = databaseHelper.login(userName, password);
	            	System.out.println("You have successfully logged in.");
	            	
	            	
	            	 if(user.isOneTimePasswordFlag()){
	            	 		settingUpAccount(user);
	            	 		break;
	            	 }
	            	 
	            	//routes to different pages depending on permissions of a user
	            	if (user.getRoles().length() == 1) {
	            		if(user.getRoles().contains("a")) adminHome();
	            		else regHome();
	            	} else sessionRoleSelection(user);
	            	break;
	            } else System.out.println("Invalid credentials! Try again");

	        }
	    }
	    
	    // Choice 2: First-time user or account reset using OTP
	    else if (choice.equals("2")) {
	        System.out.println("You have been invited to the system or had your account reset by an administrator.");
	        
	        while (true) {
	        	System.out.print("Enter your One Time Password: ");
		        oTP = scanner.nextLine();
		        
		        if (databaseHelper.verifyOTP(oTP)) {
		        	System.out.println("If you had your account reset, Please re-enter your current username and new password.");
			        System.out.println("If you are a first-time user, continue on to set up your initial username and password.");
			        
			        String[] credentials = get_user_credentials();
			        databaseHelper.register(credentials[0], credentials[1], "s");
			        
			        break;
			        
		        } else {
		        	System.out.println("OTP is invalid");
		        }
	        }
	        // You will likely want to implement functionality for OTP verification and account setup here.
	    }
	}
}
