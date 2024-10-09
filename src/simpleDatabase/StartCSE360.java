package simpleDatabase;

import java.sql.SQLException;
import java.util.Scanner;

public class StartCSE360 {

	private static final DatabaseHelper databaseHelper = new DatabaseHelper();
	private static final Scanner scanner = new Scanner(System.in);

	public static void main( String[] args )
	{

		try { 
			
			databaseHelper.connectToDatabase();  // Connect to the database

			// Check if the database is empty (no users registered)
			if (databaseHelper.isDatabaseEmpty()) {
				System.out.println( "In-Memory Database  is empty" );
				//set up administrator access
				setupAdministrator();
			}
			else {
				System.out.println( "If you are an administrator, then select A\nIf you are an user then select U\nEnter your choice:  " );
				String role = scanner.nextLine();

				switch (role) {
				case "U":
					userFlow();
					break;
				case "A":
					adminFlow();
					break;
				default:
					System.out.println("Invalid choice. Please select 'a', 'u'");
					databaseHelper.closeConnection();
				}

			}
		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
			e.printStackTrace();
		}
		finally {
			//call main user page here + not sure to add option on this page to close the connection as well
			System.out.println("Good Bye!!");
			databaseHelper.closeConnection();
		}
	}

	//Sets up administrator on first registration to the system
	private static void setupAdministrator() throws SQLException {
		System.out.println("Setting up the Administrator access.");
		System.out.print("Enter Admin Email: ");
		String email = scanner.nextLine();
		System.out.print("Enter Admin Password: ");
		String password = scanner.nextLine();
		System.out.print("Confirm Admin Password: ");
		String confirmPassword = scanner.nextLine();
		//Must have matching password and confirm password so loop till it is right
		while(!password.equals(confirmPassword)) {
			System.out.println("Invalid. Please re-enter Admin Password to confirm: ");
			confirmPassword = scanner.nextLine();
		}
		databaseHelper.register(email, password, "admin");
		System.out.println("Administrator setup completed.");

	}

	private static void userFlow() throws SQLException {
		String email = null;
		String password = null;
		System.out.println("user flow");
		System.out.print("What would you like to do 1.Register 2.Login  ");
		String choice = scanner.nextLine();
		switch(choice) {
		case "1": 
			System.out.print("Enter User Email: ");
			email = scanner.nextLine();
			System.out.print("Enter User Password: ");
			password = scanner.nextLine(); 
			// Check if user already exists in the database
		    if (!databaseHelper.doesUserExist(email)) {
		        databaseHelper.register(email, password, "user");
		        System.out.println("User setup completed.");
		    } else {
		        System.out.println("User already exists.");
		    }
			break;
		case "2":
			System.out.print("Enter User Email: ");
			email = scanner.nextLine();
			System.out.print("Enter User Password: ");
			password = scanner.nextLine();
			if (databaseHelper.login(email, password, "user")) {
				System.out.println("User login successful.");
//				databaseHelper.displayUsers();

			} else {
				System.out.println("Invalid user credentials. Try again!!");
			}
			break;
		}
	}

	private static void adminFlow() throws SQLException {
		System.out.println("admin flow");
		System.out.print("Enter Admin Email: ");
		String email = scanner.nextLine();
		System.out.print("Enter Admin Password: ");
		String password = scanner.nextLine();
		if (databaseHelper.login(email, password, "admin")) {
			System.out.println("Admin login successful.");
			databaseHelper.displayUsersByAdmin();

		} else {
			System.out.println("Invalid admin credentials. Try again!!");
		}
	}
	
	private static void settingUpAccount() throws SQLException {
		
		String first = "";
		String preferred = "";
		String last = "";
		String middle = "";
		String email = "";
		
		System.out.println("Finish Setting up Your Account");
		System.out.print("Enter Your First Name: ");
		first = scanner.nextLine();
		System.out.print("Enter Your Middle Name: ");
		middle = scanner.nextLine();
		System.out.print("Enter Your Last Name: ");
		last = scanner.nextLine();
		System.out.print("Enter Your Preferred First Name: ");
		preferred = scanner.nextLine();
		
		//Maybe put some more new lines here
		System.out.print("Enter Your Email: ");
		email = scanner.nextLine();
		
		//have a db function here that updates their account 
		System.out.println("Congrats! You have finished setting up your account.");
		//check roles - if only one - route to that home | if multiple - route to role selection page
		
	}
	
	//want a User object from a user class returned here to check roles
	private static void sessionRoleSelection() throws SQLException {
		String roles = "ast";
		String choice = "";
		
		System.out.println("You have multiple roles, but may only use the system through the view of one of them.");
		System.out.println("Your Roles: ");
		if(roles.indexOf("a") != -1) System.out.println("1. Administrator");
		if(roles.indexOf("s") != -1) System.out.println("2. Student");
		if(roles.indexOf("t") != -1) System.out.println("3. Instructor");
		System.out.print("Please select your role: ");
		choice = scanner.nextLine();
		while(!choice.equals("1") || !choice.equals("2") || !choice.equals("3")) {
			System.out.print("Invalid option. Please select your role from the list above again: ");
			choice = scanner.nextLine();
		}
		
		System.out.print("You have successfully selected the role: ");
		switch(choice) {
			case "1": 
				System.out.println("Administrator.");
				break;
			case "2": 
				System.out.println("Student.");
				break;
			case "3": 
				System.out.println("Instructor.");
				break;
			default:
				System.out.println("There was an error on our end. We are navigation you back to the login page. Please try again at a later time.");
				break;
		}
		/*
		 * Based on choice take to either Student/Instructor Home or Admin Home -
		 */
	}
	
	private static void regHome() throws SQLException {
		System.out.println("Welcome to the home page of either a Student or Instructor.");
		System.out.println("At this time, you can only perform one action - Logout. However, you are welcome to sit here for however long you like.");
		System.out.print("To Logout, Enter q: ");
		String logout = scanner.nextLine();
		if(logout.equals("q")) {
			System.out.println("You have successfully logged out. See you next time!");
			//route to the main Login Page
		} 
		while(!logout.equals("q")) {
			System.out.print("Invalid input. To Logout, Enter q: ");
			logout  = scanner.nextLine();
		}
	}
	
	private static void adminHome() throws SQLException {
		String choice = "";
		boolean valid = false;
		
		System.out.println("Welcome to the Home Page for Admins!");
		
		
		choice = scanner.nextLine();
		do {
			
			System.out.println("Here are the actions you can perform: ");
		
			System.out.println("1. Invite a user to the system"); //After selected we can give the permissions down there + OTP and little statement that OTP was sent out
			System.out.println("2. Reset a user account"); //Ask after selection for OTP and little statement that OTP was sent out
			System.out.println("3. Delete a user account"); //Ask after selected if they are sure to actually delete
			System.out.println("4. List the user accounts"); //db display users
			System.out.println("5. Add or Remove a role from a user"); //decide add vs remove later
			System.out.println("6. Logout");
			
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
						valid = true;
						break;
					}
					case "2": {
						rolesToGive = "s";
						valid = true;
						break;
					}
					case "3": {
						rolesToGive = "t";
						valid = true;
						break;
					}
					case "4": {
						rolesToGive = "as";
						valid = true;
						break;
					}
					case "5": {
						rolesToGive = "at";
						valid = true;
						break;
					}
					case "6": {
						rolesToGive = "st";
						valid = true;
						break;
					}
					case "7": {
						rolesToGive = "ast";
						valid = true;
						break;
					}
					default:
						System.out.println("Invalid option.");
						break;
						
				}
				
				//OTP Generation + Sending
				if(valid) {
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
				valid = true;
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
						valid = true;
					}
				} else if(confirmDelete == "2") System.out.println("You have not deleted a user.");
				else System.out.println("Invalid Option. You have not deletd a user.");
				break;
			}
			
			//List of all Users
			case "4": {
				databaseHelper.displayUsersByAdmin();
				valid = true;
				break;
			}
			
			//User Role Adjustment
			case "5": {
				
				valid = true;
				break;
			}
			
			//Logout
			case "6": {
				
				System.out.println("To Logout, Enter q: ");
				String logout = scanner.nextLine();
				if(logout == "q") {
					System.out.println("You have successfully been logged out of the system.");
					valid = true;
				} else System.out.println("Invalid option.");
				break;
			}
			default: 
				System.out.print("Invalid choice. Please try again.");
				break;
			}
		} while(!valid); 
			
		
		
		
	}
	
	private static void mainLogin() throws SQLException {
		
		String choice ="";
		String userName = "";
		//char[] password = new char[100];
		String password = "";
		String confirmPassword = "-1";
		int oTP = -1;
		
		//input for returning user and deals with invalid input
		System.out.print("Are you a returning user? 1. Yes 2. No");
		choice = scanner.nextLine();
		while(!choice.equals("1") || !choice.equals("2") ) {
			System.out.println("Invalid option selected. Please try again");
			System.out.print("Are you a returning user? 1. Yes 2. No");
			choice = scanner.nextLine();
		}
		
		//if a first time user - needs an OTP to setup username and password
		if(choice.equals("2")) {
			System.out.println("Congratulations! You have been invited to the system by an administrator.");
			System.out.print("Enter your One Time Password: ");
			oTP = scanner.nextInt();
			System.out.println("Please continue on to set up your initial username and password");
		}
		
		System.out.print("Enter Username: ");
		userName = scanner.nextLine();
		System.out.print("Enter Password: ");
		password = scanner.nextLine();
		//If it is a first time user
		if(choice.equals("2")) {
			System.out.print("Confirm Password: ");
			confirmPassword = scanner.nextLine();
			while(!password.equals(confirmPassword)) {
				System.out.print("Invalid. Please re-enter your password to confirm: ");
				confirmPassword = scanner.nextLine();
			}
			System.out.println("You have just completed the initial set up for your account.");
			System.out.println("The next time you login with this username and password, you will be taken to finish setting up your account.");
		} else System.out.println("You have successfully logged in.");
		/* adjust the register and login sql statements
		 * if choice = 1 = remove role from sql statement (user and password should be enough to get a unique user)
		 * if choice = 2 - register them as a user
		 * Also need something in database to validate the OTP 
		 * 
		 * If new user - route back to main login again
		 * On second log in
		 * 		have db return something that tells us to move to the finish setting up account
		 */

	}
}
