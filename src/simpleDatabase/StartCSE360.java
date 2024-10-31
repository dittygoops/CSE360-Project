package simpleDatabase;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * This file contains the controller for the Project, all of the ways the user will interact with our service.
 * 
 *
 * @author Shiva Rudra
 * @version 1.0
 * @since 10/9/2024
 */
public class StartCSE360 {

	private static final DatabaseHelper databaseHelper = new DatabaseHelper();
	private static final Scanner scanner = new Scanner(System.in);

	/*
	 * Blank Constructor
	 */
	public StartCSE360() {}
	
	/*
	 * Main method that runs the databases and does the main login
	 */
	public static void main( String[] args )
	{

		try { 
			
			databaseHelper.connectToDatabase();  // Connect to the database

			// Check if the database is empty (no users registered)
			if (databaseHelper.isDatabaseEmpty()) {
				System.out.println( "In-Memory Database  is empty" );
				//set up administrator access
				setupAdministrator();
				databaseHelper.register("hi2", "bruh2", "as");
			}
			//called here as need to reroute to main login after initial setup or if there are other users
			mainLogin();
			
		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
			e.printStackTrace();
		}
		finally {
			System.out.println("Good Bye!!");
			databaseHelper.closeConnection();
		}
	}

	/*
	 * Sets up administrator on first registration to the system
	 */
	private static void setupAdministrator() throws SQLException {
		System.out.println("Setting up the Administrator access");
		System.out.print("Enter Admin Username: ");
		String userName = scanner.nextLine();
		System.out.print("Enter Admin Password: ");
		String password = scanner.nextLine();
		System.out.print("Confirm Admin Password: ");
		String confirmPassword = scanner.nextLine();
		//Must have matching password and confirm password so loop till it is right
		while(!password.equals(confirmPassword)) {
			System.out.print("Invalid. Please re-enter Admin Password to confirm: ");
			confirmPassword = scanner.nextLine();
		}
		databaseHelper.register(userName, password, "a");
		System.out.println("Administrator setup completed.");

	}
	
	/*
	 * pass in User object here which contains all their role info assigned by Admin at invitation (if reset account - come back in as student)
	 */
	private static void settingUpAccount(User currentUser) throws SQLException {
		
		//fields we need information for
		String first = "";
		String preferred = "";
		String last = "";
		String middle = "";
		String email = "";
		
		System.out.println("Finish Setting up Your Account");
		
		//Asking for user input to get the information required
		System.out.print("Enter Your First Name: ");
		first = scanner.nextLine();
		System.out.print("Enter Your Middle Name: ");
		middle = scanner.nextLine();
		System.out.print("Enter Your Last Name: ");
		last = scanner.nextLine();
		System.out.print("Enter Your Preferred First Name: ");
		preferred = scanner.nextLine();
		System.out.print("Enter Your Email: ");
		email = scanner.nextLine();
		
		
		
		currentUser.setFirstName(first);
		currentUser.setMiddleName(middle);
		currentUser.setLastName(last);
		currentUser.setPreferredName(preferred);
		currentUser.setEmail(email);
		currentUser.setOTPFlag(false);

		databaseHelper.updateUser(currentUser);

		//  * Below this line goes after the congrats message!
		if (currentUser.getRoles().length() == 1) {
	            		if(currentUser.getRoles().contains("a")) adminHome();
	            		else regHome();
	            	} else sessionRoleSelection(currentUser);
		
		System.out.println("Congrats! You have finished setting up your account.");
	}
	
	/*
	 * want a User object from a user class passed as a parameter here to check roles
	 */
	private static void sessionRoleSelection(User currentUser) throws SQLException {
		
		//From the User object that was a parameter - find the roles or maybe from DB 
		String roles = currentUser.getRoles();
		String choice = "";
		
		System.out.println("You have multiple roles, but may only use the system through the view of one of them.");
		System.out.println("Your Roles: ");
		if(roles.contains("a")) System.out.println("1. Administrator");
		if(roles.contains("s")) System.out.println("2. Student");
		if(roles.contains("t")) System.out.println("3. Instructor");
		

		// allow user to select which user profile they would like to view
		System.out.print("Please select your role for the session: ");
		choice = scanner.nextLine();
		while(!choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
			System.out.print("Invalid option. Please select your role from the list above again: ");
			choice = scanner.nextLine();
		}
		
		//navigation to the proper method based on which role was selected
		System.out.print("You have successfully selected the role: ");
		switch(choice) {
			case "1" -> {
                            System.out.println("Administrator.");
                            adminHome();
                }
			case "2" -> {
                            System.out.println("Student.");
                            regHome();
                }
			case "3" -> {
                            System.out.println("Instructor.");
                            regHome();
                }
			default -> System.out.println("There was an error on our end. We are navigation you back to the login page. Please try again at a later time.");
		}
	}
	
	
	
	/*
	 * Home for admin
	 */
	private static void adminHome() throws SQLException {
		String choice = "";
		
		System.out.print("Welcome to the Home Page for Admins!");
		do {
			
			System.out.println("Here are the actions you can perform: ");
		
			System.out.println("1. Invite a user to the system"); //After selected we can give the permissions down there + OTP and little statement that OTP was sent out
			System.out.println("2. Reset a user account"); //Ask after selection for OTP and little statement that OTP was sent out
			System.out.println("3. Delete a user account"); //Ask after selected if they are sure to actually delete
			System.out.println("4. List the user accounts"); //db display users
			System.out.println("5. Add or Remove a role from a user"); //decide add vs remove later
			System.out.println("6. Logout");
			
			choice = scanner.nextLine();			
			switch(choice) {
			
			//User invitation to the system
			case "1": {
				String rolesToGive = "";
				String roleSelect = "";
				
				System.out.println("Here are the possible roles this user can have: ");
				
				//Options for role combos
				System.out.println("1. Administrator only");
				System.out.println("2. Student only");
				System.out.println("3. Instructor only");
				System.out.println("4. Administrator and Student only");
				System.out.println("5. Administrator and Instructor only");
				System.out.println("6. Student and Instructor only");
				System.out.println("7. Administrator, Student, and Instructor");
				
				
				System.out.print("Please select an option: ");
				roleSelect = scanner.nextLine();
				
				switch(roleSelect) {
					case "1": {
						rolesToGive = "a";
						 
						break;
					}
					case "2": {
						rolesToGive = "s";
						 
						break;
					}
					case "3": {
						rolesToGive = "t";
						 
						break;
					}
					case "4": {
						rolesToGive = "as";
						 
						break;
					}
					case "5": {
						rolesToGive = "at";
						 
						break;
					}
					case "6": {
						rolesToGive = "st";
						 
						break;
					}
					case "7": {
						rolesToGive = "ast";
						 
						break;
					}
					default:
						System.out.println("Invalid option.");
						break;
						
				}
				
				//OTP Generation + Sending
				databaseHelper.createOTP();
				if(rolesToGive.length() > 0) {
					//generate OTP + flag on DB side
					System.out.println("You have successfully invited a student to join the system!");
					System.out.println("One Time Password has been sent to this user to enable their registration.");
				}
				break;
			}
			
			//User Account Reset
			case "2": {
				
				System.out.print("Enter username for the user you would like to reset: ");
				String usernameReset = scanner.nextLine();
				System.out.print("Enter email for the user you would like to reset: ");
				String emailReset = scanner.nextLine();
				if(!databaseHelper.doesUserExist(emailReset)) {
					System.out.println("There is no user with the provided specifications.");
					break;
				}
				
				//revert back to initial registration or user invitation state for the db
				//generates OTP
				System.out.println("You have successfully reset a user in the system.");
				//OTP generation
				System.out.println("One Time Password has been sent to this user to enable their registration.");
				 
				break;
			}
			
			//User Deletion
			case "3": {
				
				System.out.print("Are you sure? 1. Yes 2. No");
				String confirmDelete = scanner.nextLine();
				if(confirmDelete == "1") {
					System.out.print("Enter username for the user you would like to delete: ");
					String usernameDelete = scanner.nextLine();
					System.out.print("Enter email for the user you would like to delete: ");
					String emailDelete = scanner.nextLine();
					if(!databaseHelper.doesUserExist(emailDelete)) {
						System.out.println("There is no user with the provided specifications.");
						break;
					}
					else {
						//db user delete - check if exists first
						System.out.println("You have successfully deleted a user.");
						 
					}
				} else if(confirmDelete == "2") System.out.println("You have not deleted a user.");
				else System.out.println("Invalid Option. You have not deletd a user.");
				break;
			}
			
			//List of all Users
			case "4": {
				databaseHelper.displayUsersByAdmin();
				 
				break;
			}
			
			//User Role Adjustment
			case "5": {
				
				//Find User in System - put into a User object after found
				System.out.print("Enter username for the user whose roles you would like to adjust: ");
				String usernameAdjust = scanner.nextLine();
				System.out.print("Enter email for the user whose roles you would like to adjust: ");
				String emailAdjust = scanner.nextLine();
				if(!databaseHelper.doesUserExist(emailAdjust)) {
					System.out.println("There is no user with the provided specifications.");
					break;
				}
				
				//have this from User object that we have found via db query I assume
				String userRoles = "";
				
				System.out.print("Please choose if you would like to add or remove a role from this user? 1. Add 2. Remove");
				String option = scanner.nextLine();
				if(option == "1") {
					
					if(userRoles.length() == 3) {
						System.out.println("This user already has all the roles.");
						break;
					}
					//Pick Roles to add - must check if a user does not have one to add it
					System.out.println("You are now adding a role to this user. Here are the options: ");
					if(userRoles.indexOf("a") == -1) System.out.println("1. Administrator");
					if(userRoles.indexOf("s") == -1) System.out.println("2. Student");
					if(userRoles.indexOf("t") == -1) System.out.println("3. Instructor");
					System.out.print("Please select a role: ");
					String rolePick = scanner.nextLine();
					
					
					switch(rolePick) {
					
					//Adding a role to the user
					case "1": {
						
						//Initial Check is in case they input a number between 1 and 3 but the user already has the role (accidental input that would be valid in other cases)
						if(userRoles.indexOf("a") != -1) {
							System.out.println("You have selected a role that this user already has. The user's roles will remain the same");
							break;
						}
						
						//Add role to the local String - used for DB update later
						userRoles += "a";
						System.out.println("You have successfully added the Administrator role to this user");
						 
						break;
					}
					case "2": {
						
						//same as case 1 but for student role
						if(userRoles.indexOf("s") != -1) {
							System.out.println("You have selected a role that this user already has. The user's roles will remain the same");
							break;
						}
						userRoles += "s";
						System.out.println("You have successfully added the Student role to this user");
						 
						break;
					}
					case "3": {
						
						//same as case 1 but for instructor role
						if(userRoles.indexOf("t") != -1) {
							System.out.println("You have selected a role that this user already has. The user's roles will remain the same");
							break;
						}
						userRoles += "t";
						System.out.println("You have successfully added the Instructor role to this user");
						 
						break;
					}
					default:
						System.out.println("Invalid option. The user will remain unchanged.");
						break;
					}
				
				//Removing a role from the user
				} else if (option == "2") {
					
					if(userRoles.length() == 0) {
						System.out.println("This user has no roles.");
						break;
					}
					
					//Pick Roles to remove - must check if a user has one to remove it
					System.out.println("You are now removing a role to this user. Here are the options: ");
					if(userRoles.indexOf("a") != -1) System.out.println("1. Administrator");
					if(userRoles.indexOf("s") != -1) System.out.println("2. Student");
					if(userRoles.indexOf("t") != -1) System.out.println("3. Instructor");
					System.out.print("Please select a role: ");
					String rolePick = scanner.nextLine();
					
					
					switch(rolePick) {
					
					case "1": {
						
						//Initial Check is in case they input a number between 1 and 3 but the user already has the role (accidental input that would be valid in other cases)
						if(userRoles.indexOf("a") == -1) {
							System.out.println("You have selected a role that this user does not have. The user's roles will remain the same");
							break;
						}
						
						//Remove role from string - again this string will be used for a DB update later
						userRoles = userRoles.replace("a", "");
						System.out.println("You have successfully removed the Administrator role from this user");
						 
						break;
					}
					
					//Same as case 1 but for Student role
					case "2": {
						if(userRoles.indexOf("s") == -1) {
							System.out.println("You have selected a role that this user does not have. The user's roles will remain the same");
							break;
						}
						userRoles = userRoles.replace("s", "");
						System.out.println("You have successfully removed the Student role from this user");
						 
						break;
					}
					
					//Same as case 1 but for Instructor role
					case "3": {
						if(userRoles.indexOf("t") != -1) {
							System.out.println("You have selected a role that this user does not have. The user's roles will remain the same");
							break;
						}
						userRoles = userRoles.replace("t", "");
						System.out.println("You have successfully removed the Instructor role from this user");
						 
						break;
					}
					default:
						System.out.println("Invalid option. The user will remain unchanged.");
						break;
					}
					
				//Final Check is for invalid input on Adding or Removing roles	
				} else System.out.println("Invalid Option. The user will remain unchanged.");
				
				break;
			}
			
			//Logout
			case "6": {
				
				System.out.println("To Logout, Enter q: ");
				String logout = scanner.nextLine();
				if(logout.equals("q")) {
					System.out.println("You have successfully been logged out of the system.");
					 
				} else System.out.println("Invalid option.");
				break;
			}
			default: 
				System.out.print("Invalid choice. Please try again.");
				break;
			}
		} while(!choice.equals("6")); 
		
		mainLogin();
	}
	
	/**
	 * Gets username and password from user input
	 * 
	 * @return String array with username and password
	 */
	 /*
	 * Homepage for students/instructors
	 */
	private static void regHome() throws SQLException {
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

	// get user credentials
	private static String[] get_user_credentials() {
		String[] credentials = new String[2];
		System.out.print("Enter your username: ");
		credentials[0] = scanner.nextLine();
		System.out.print("Enter your password: ");
		credentials[1] = scanner.nextLine();
		return credentials;
	}
	
}