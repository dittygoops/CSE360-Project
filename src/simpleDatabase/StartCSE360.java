package simpleDatabase;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * This file contains the user interface for the Project. All database
 * interactions will be processed by the DatabaseHelper class.
 * This is a program that emulates a help system that contains articles and can
 * be interacted with by administrators, instructors, and users all with varying
 * permissions.
 * Additionally, any given user can have one or a combination of multiple roles
 * as an education system is complex.
 * (ex. A Phd student accessing articles for their high level classes and also
 * overlooking articles for ones they are teaching)
 * 
 *
 * @author Shiva Rudra, Abhave Abhilash, Aditya Gupta, Isabella Swanson, Justin
 *         Miller
 * @version 1.0
 * @since 10/9/2024
 */
public class StartCSE360 {

	/**
	 * The global databaseHelper object so that we can pass values to methods in
	 * that file and interact with the database
	 */
	private static final DatabaseHelper databaseHelper = new DatabaseHelper();

	/**
	 * The scanner used to take in all text inputs for the entire system. Core of
	 * the console-based UI
	 */
	private static final Scanner scanner = new Scanner(System.in);

	// P3: Removed constructor since Java makes a blank one by default

	/**
	 * This is the start of the application that connects to an H2 database that
	 * stores all critical information.
	 * This sets up the flow for user interaction with the system.
	 * 
	 * @param args This parameter holds the arguments from the command line
	 * @throws Exception Throws an Exception if there is a SQL error from the helper
	 *                   file and logs the issue
	 */
	public static void main(String[] args) throws Exception {

		try {

			databaseHelper.connectToDatabase(); // Connect to the database

			// Check if the database is empty (no users registered)
			if (databaseHelper.isDatabaseEmpty()) {
				System.out.println("In-Memory Database  is empty");
				// set up administrator access
				setupAdministrator();
			}
			// called here as need to reroute to main login after initial setup or if there
			// are other users
			mainLogin();

			// catch any database errors and log
		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
			e.printStackTrace();
		}
		// after all methods have returned - exit and close the connection
		finally {
			System.out.println("Good Bye!!");
			databaseHelper.closeConnection();
		}
	}

	/**
	 * Asks for and stores first admin login info
	 * <p>
	 * This sets up the login information for the first user of the system who will
	 * become the initial administrator.
	 * Any personal information will be collected on their next login.
	 * </p>
	 * 
	 * 
	 * @throws SQLException Throws error if there is a SQL error from our
	 *                      interaction with the database
	 */
	private static void setupAdministrator() throws SQLException {
		System.out.println("Setting up the Administrator access");
		System.out.print("Enter Admin Username: ");
		String userName = scanner.nextLine();
		System.out.print("Enter Admin Password: ");
		String password = scanner.nextLine();
		System.out.print("Confirm Admin Password: ");
		String confirmPassword = scanner.nextLine();

		// Must have matching password and confirm password so loop till it is right
		while (!password.equals(confirmPassword)) {
			System.out.print("Invalid. Please re-enter Admin Password to confirm: ");
			confirmPassword = scanner.nextLine();
		}

		// insert login info to the table
		int firstAdminId = databaseHelper.firstAdmin(userName, password);
		if(firstAdminId != -1) System.out.println("Administrator setup completed.");
	}

	/**
	 * Sets up account for users invited by admin
	 * <p>
	 * An incomplete user, one who is invited by an admin gets their information
	 * fully populated in this method.
	 * This is called when the user logins in after already setting up their login
	 * information from an admin invitation and associated OTP
	 * </p>
	 * 
	 * @param currentUser This parameter is an user object that holds all the
	 *                    information for the current User
	 * @throws SQLException Throws error if there is a SQL error from our
	 *                      interaction with the database
	 * @throws Exception    Throws an Exception if there is a SQL error from the
	 *                      helper file and logs the issue
	 */
	private static void settingUpAccount(User currentUser) throws SQLException, Exception {

		// fields we need information for
		String first = "";
		String preferred = "";
		String last = "";
		String middle = "";
		String email = "";

		System.out.println("Finish Setting up Your Account");

		// Asking for user input to get the information required
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

		// populates a User object we can send to the helper method
		currentUser.setFirstName(first);
		currentUser.setMiddleName(middle);
		currentUser.setLastName(last);
		currentUser.setPreferredName(preferred);
		currentUser.setEmail(email);
		currentUser.setOTPFlag(false);

		// User object sent to helper method to be put into the database
		if(!databaseHelper.updateUser(currentUser)) {
			System.out.println("No user was properly updating with the personal information above. Please try again later.");
			return;
		}

			System.out.println("Congrats! You have finished setting up your account.");
			// Routes to a specific user home if they only have one role
			boolean[] curUserRoles = currentUser.getRoles();
			if (curUserRoles[0] && (!curUserRoles[1] && !curUserRoles[2])) 
					adminHome();
			else if (curUserRoles[1] && (!curUserRoles[0] && !curUserRoles[2]))
					instructorHome();
			else if (curUserRoles[2] && (!curUserRoles[1] && !curUserRoles[0]))
					studentHome();
				// if not - the user gets to choose which home menu to go to
			else
				sessionRoleSelection(currentUser);

		
	}

	/**
	 * Where user selects which role's home they want to go to
	 * <p>
	 * The user is routed here after logging in or setting up their account if they
	 * have multiple roles.
	 * Here they are allowed to select the role for their session and will route to
	 * the appropriate home menu.
	 * </p>
	 * 
	 * @param currentUser This parameter is an user object that holds all the
	 *                    information for the current User
	 * @throws SQLException Throws error if there is a SQL error from our
	 *                      interaction with the database
	 * @throws Exception    Throws an Exception if there is a SQL error from the
	 *                      helper file and logs the issue
	 */
	private static void sessionRoleSelection(User currentUser) throws SQLException, Exception {

		// Get roles from the curUser
		boolean[] roles = currentUser.getRoles();
		String choice = "";

		// Displays which roles they have
		System.out.println("You have multiple roles, but may only use the system through the view of one of them.");
		System.out.println("Your Roles: ");
		if (roles[0])
			System.out.println("1. Administrator");
		if (roles[2])
			System.out.println("2. Student");
		if (roles[1])
			System.out.println("3. Instructor");

		// Allow user to select which profile or home menu they would like to view
		System.out.print("Please select your role for the session: ");
		choice = scanner.nextLine();

		// Wait for valid choice
		while (!choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
			System.out.print("Invalid option. Please select your role from the list above again: ");
			choice = scanner.nextLine();
		}

		// Navigate to the proper menu based on which role was selected
		// P3: Get rid of regHome once Student home fully done
		System.out.print("You have successfully selected the role: ");
		switch (choice) {
			case "1": {
				System.out.println("Administrator.");
				adminHome();
				break;
			}
			case "2": {
				System.out.println("Student.");
				studentHome();
				break;
			}
			case "3": {
				System.out.println("Instructor.");
				instructorHome();
				break;
			}
			default: {
				System.out.println(
						"There was an error on our end. We are navigation you back to the login page. Please try again at a later time.");
				break;
			}
		}
	}

	/**
	 * Student Home that allows them to mainly send messages about their issues,
	 * filter articles by group/content level/id, and search via limited avenues.
	 * 
	 * <p>
	 * This is the home for users who are only students or have selected to
	 * currently access the system as a student.
	 * Here students are allowed to:
	 * - Quit the application
	 * - Send a generic or specific message to the system about it
	 * - View articles by content level or group
	 * - Search for articles
	 * - View a specific article by ID
	 * Their actions will yield different results based on which groups they are
	 * allowed access to.
	 * </p>
	 * 
	 * @throws SQLException Throws error if there is a SQL error from our
	 *                      interaction with the database
	 * @throws Exception    Throws an Exception if there is a SQL error from the
	 *                      helper file and logs the issue
	 */
	private static void studentHome() throws SQLException, Exception {
		String option = "";
		do {

			// Main Menu of options
			System.out.println("Welcome to the student home.");
			System.out.println("Here are your options: ");
			System.out.println("1. Exit this session");
			System.out.println("2. Send a generic message");
			System.out.println("3. Send a specific message");
			System.out.println("4. View Articles by Content Level (Returns all unless level specified later)");
			System.out.println("5. View Articles by Group (Returns all unless level specified later)");
			System.out.println("6. Search for an article");
			System.out.println("7. View Article by ID");
			option = scanner.nextLine();

			// Processes option chosen
			switch (option) {

				// Exit session
				case "1": {
					System.out.println("You are now ending your session. Hope to see you soon!");
					break;
				}

				// Generic Message
				case "2": {
					System.out.println("Please type your general message below: ");
					String genMessage = scanner.nextLine();
					// P3: pass genMessage to db method to be stored for future
					System.out.println("Your message has been sent and stored to improve our system in the future.");
					break;
				}

				// Specific Message - Must contain exact issue
				case "3": {
					System.out.println(
							"Please enter your specific message below. Make sure to include exactly what you need and/or cannot find: ");
					String genMessage = scanner.nextLine();
					// P3: pass genMessage to db method to be stored for future
					System.out.println("Your message has been sent and stored to improve our system in the future.");
					break;
				}
				// P3: Change phrasing so that student knows that they can intentionally enter
				// anything besides a specified content level or group ID to view all articles
				// Filter by Content Level - default = all
				case "4": {
					System.out.println(
							"Please enter the content level of articles you would like. Any other input besides Beginner, Intermediate, Advanced, or Expert will return all articles: ");
					String contentLevel = scanner.nextLine();
					if (!contentLevel.equals("Beginner") && !contentLevel.equals("Intermediate")
							&& !contentLevel.equals("Advanced") && !contentLevel.equals("Expert")) {
						System.out.println(
								"Your input did not match any specified content level. Here are all the articles in the system: ");
						// P3: return all articles
					} else {
						System.out.println("Here are the articles in the content level: " + contentLevel);
						// P3: dbHelper function call with level passed in
					}
					break;
				}

				// Filter by group - default = all
				case "5": {
					System.out.println(
							"Please enter the group of articles you would like. If your input does not match any existing groups, all articles will be returned: ");
					String groupChosen = scanner.nextLine();
					// P3: See if group exists by passing groupChosen to dbHelper function - store
					// in a flag (stored to false for now)
					boolean groupExists = false;
					if (groupExists) {
						System.out.println("Here are the articles in the group: " + groupChosen);
						// P3: Get all articles in groupChosen and display
					} else {
						System.out.println(
								"We could not find any group of articles matching the criteria you entered. Here are all the articles in the system: ");
						// P3: return all articles
					}
					break;
				}
				// P3: Potential to change based on how we implement search in DB
				// Search for an article
				case "6": {
					System.out.println(
							"Please search for an article via words, names, or phrases in the Title, Author(s), or Abstract: ");
					String searchCond = scanner.nextLine();
					// P3: Send to DB to find all associated articles - need condition block to say
					// whether any articles matching criteria were found
					break;
				}

				// View article in detail via ID
				case "7": {
					System.out.println("Please enter the ID of the article you would like to view: ");
					long idChosen = scanner.nextLong();
					// P3: Pass id to db to get it and view in detail - only place to return
					// everything/not in short form
					break;
				}

				// Default message for invalid input
				default: {
					System.out.println("You entered an invalid input. Please try again!");
					break;
				}
			}
		} while (!option.equals("1")); // Loops until student exits their session

	}

	/**
	 * Main Login for all users entering the system
	 * 
	 * <p>
	 * This is the main login for the system where all users besides the initial
	 * administrator begins.
	 * A user can either login as normal, have to set up their account, or exit the
	 * system.
	 * A user will be routed to set up their account because it is their first time
	 * logging in after either being invited or having their account reset by an
	 * administrator and sent a one-time password.
	 * A user can also exit the entire system from here after ending their session.
	 * </p>
	 * 
	 * @throws SQLException Throws error if there is a SQL error from our
	 *                      interaction with the database
	 * @throws Exception    Throws an Exception if there is a SQL error from the
	 *                      helper file and logs the issue
	 */
	private static void mainLogin() throws SQLException, Exception {
		String choice = "";
		String userName = "";
		String password = "";
		String oTP = "";

		// Input for returning user and deals with invalid input
		System.out.print(
				"Are you a returning user? (Note - If you had your account reset, choose 2) 1. Yes 2. No 3. Exit System ");
		choice = scanner.nextLine();

		// Deals with invalid options - loops till valid choice
		while (!choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
			System.out.println("Invalid option selected. Please try again");
			System.out.print("Are you a returning user? 1. Yes 2. No 3. Exit the System: ");
			choice = scanner.nextLine();
		}

		// Choice 1: Returning user
		if (choice.equals("1")) {
			while (true) {
				String[] credentials = get_user_credentials(); // Get username and password
				userName = credentials[0];
				password = credentials[1];

				// Check if user exists and credentials are valid
				if (databaseHelper.doesUserExistBoth(userName, password)) {

					User user = databaseHelper.login(userName, password);
					// if a user exists in the database and had an OTP - they must finish setting up
					// their account and will be routed there
					if (user != null) {
						System.out.println("You have successfully logged in.");
						if (user.getOTP()) {
							settingUpAccount(user);
							break;
						}
					} else
						System.out.println("User is being returned as null."); // Error Message log as user was already
																				// shown to be in system yet is not
																				// being returned properly

					// routes to different home pages depending on roles of the user
					// P3: Change to different Homes once setup and get rid of regHome
					boolean[] curUserRoles = user.getRoles();
			if (curUserRoles[0] && (!curUserRoles[1] && !curUserRoles[2])) 
					adminHome();
			else if (curUserRoles[1] && (!curUserRoles[0] && !curUserRoles[2]))
					instructorHome();
			else if (curUserRoles[2] && (!curUserRoles[1] && !curUserRoles[0]))
					studentHome();
				// if not - the user gets to choose which home menu to go to
			else
				sessionRoleSelection(user);

					break;
				} else
					System.out.println("Invalid credentials! Try again"); // if user does not exist - will be asked to
																			// log in again

			}
		}

		// Choice 2: First-time user or account reset using OTP
		else if (choice.equals("2")) {
			System.out.println("You have been invited to the system or had your account reset by an administrator.");

			while (true) {
				System.out.print("Enter your One Time Password: ");
				oTP = scanner.nextLine();

				// if OTP is still valid then reset or set up credentials for first time
				int userId = databaseHelper.verifyOTP(oTP);
				if (userId != -1) {
					System.out.println(
							"If you had your account reset, Please re-enter your previous username and new password.");
					System.out.println(
							"If you are a first-time user, continue on to set up your initial username and password.");

					String[] credentials = get_user_credentials();
					if(databaseHelper.register(credentials[0], credentials[1], userId)) {
						System.out.println("Thank you for registering! Please note: ");
						System.out.println("The next time you login with these credentials, you will be directed to finish setting up your account. Bye!");
					} else {
						System.out.println("No user was registered properly. Please try agian later.");
					}

					break;
				} else
					System.out.println("Your OTP is either incorrect or no longer valid. Please try again."); // if OTP is not valid - let them enter again
			}

			mainLogin(); // route back to the top so user can login again to finish setting up account

		// Exit System
		} else {
			System.out.println("You are now leaving the system.");
			return;
		}
	}

	/**
	 * Admin home - admins can do basically everything from here
	 * 
	 * <p>
	 * This is the administrator home. From here administrators can do almost
	 * anything for the articles, instructors, students, and groups they have rights
	 * for.
	 * They can list all articles, filter articles by various conditions such as
	 * group or ID, grant certain users additional permissions, take permissions
	 * from users, and much more.
	 * </p>
	 * 
	 * @throws SQLException Throws error if there is a SQL error from our
	 *                      interaction with the database
	 * @throws Exception    Throws an Exception if there is a SQL error from the
	 *                      helper file and logs the issue
	 */
	private static void adminHome() throws SQLException, Exception {
		String choice = "";

		System.out.println("Welcome to the Home Page for Admins!");
		do {

			// Main Menu for all their actions
			System.out.println("Here are the actions you can perform: ");

			System.out.println("1. Invite a user to the system");
			System.out.println("2. Reset a user account");
			System.out.println("3. Delete a user account");
			System.out.println("4. List the user accounts");
			System.out.println("5. Add or Remove a role from a user");
			System.out.println("6. Create an Article");
			System.out.println("7. View a group of Articles in short Form");
			System.out.println("8. View all Articles in short Form");
			System.out.println("9. Delete an Article");
			System.out.println("10. Restore Options");
			System.out.println("11. Backup Options");
			System.out.println("12. List all users in a general group");
			System.out.println("13. Add a user to a general group");
			System.out.println("14. Remove a user from a general group");
			System.out.println("15. Create a Special Access Group and Invite an Instructor");
			System.out.println("16. Manage Access Rights to a Special Access Group");
			System.out.println("17. Logout");

			choice = scanner.nextLine();
			switch (choice) {

				// User invitation to the system
				case "1": {
					boolean aFlag = false;
					boolean tFlag = false;
					boolean sFlag = false;	
					String roleSelect = "";

					System.out.println("Here are the possible roles this user can have: ");

					// Options for role combos
					System.out.println("1. Administrator only");
					System.out.println("2. Student only");
					System.out.println("3. Instructor only");
					System.out.println("4. Administrator and Student only");
					System.out.println("5. Administrator and Instructor only");
					System.out.println("6. Student and Instructor only");
					System.out.println("7. Administrator, Student, and Instructor");

					System.out.print("Please select an option: ");
					roleSelect = scanner.nextLine();

					switch (roleSelect) {
						case "1": {
							aFlag = true;	
							break;
						}
						case "2": {
							sFlag = true;
							break;
						}
						case "3": {
							tFlag = true;
							break;
						}
						case "4": {
							aFlag = true;
							sFlag = true;
							break;
						}
						case "5": {
							aFlag = true;
							tFlag = true;
							break;
						}
						case "6": {
							tFlag = true;
							sFlag = true;
							break;
						}
						case "7": {
							aFlag = true;
							tFlag = true;
							sFlag = true;
							break;
						}
						default:
							System.out.println("Invalid option.");
							break;

					}

					if (aFlag || tFlag || sFlag) {
						//First create shell User
						int shellUserID = databaseHelper.insertShellUser(aFlag, tFlag, sFlag);
						if(shellUserID == -1) {
							System.out.println("DB issue");
							break;
						}


						System.out.print("Here is the OTP sent: ");
						System.out.println(databaseHelper.createOTP(shellUserID));
						System.out.println("You have successfully invited a student to join the system!");
						System.out.println("One Time Password has been sent to this user to enable their registration.");
					} else System.out.println("There was an issue with inviting the user. Please try again later.");
					break;
				}

				// User Account Reset
				case "2": {

					String usernameReset, emailReset;
					String[] reset = get_user_identifiers();
					usernameReset = reset[0];
					emailReset = reset[1];
					// check by both user and email only
					if (!databaseHelper.userExist(usernameReset, emailReset)) {
						System.out.println("There is no user with the provided specifications.");
						break;
					}

					// Account will reset with the roles they had before
					boolean[] curRoles = databaseHelper.getUserRoles(usernameReset, emailReset);
					if (databaseHelper.deleteUserAccount(usernameReset, emailReset)) {
						System.out.println("You have successfully reset a user in the system. They will be notified of this change. They will have the same roles as before once signed back in");
						int shellUserID = databaseHelper.insertShellUser(curRoles[0], curRoles[1], curRoles[2]);

						if(shellUserID == -1) {
							System.out.println("DB issue");
							break;
						}

						System.out.print("Here is the OTP sent: ");
						System.out.println(databaseHelper.createOTP(shellUserID));

					} else System.out.println("There was an error on our end and specified user has not been reset - please try again later.");
					break;
				}

				// User Deletion
				case "3": {

					String usernameDelete, emailDelete;
					String[] delete = get_user_identifiers();
					usernameDelete = delete[0];
					emailDelete = delete[1];

					if (!databaseHelper.userExist(usernameDelete, emailDelete)) {
						System.out.println("There is no user with the provided specifications.");
						break;
					} 


					if(databaseHelper.deleteUserAccount(usernameDelete, emailDelete))
						System.out.println("You have successfully deleted a user.");
					else
						System.out.println("There was an error on our end. Please try again later");

					break;
				}

				// List of all Users -
				case "4": {
					databaseHelper.displayUsersByAdmin();
					break;
				}

				// User Role Adjustment
				case "5": {

					String usernameAdjust, emailAdjust;
					String[] adjust = get_user_identifiers();
					usernameAdjust = adjust[0];
					emailAdjust = adjust[1];
					
					if (!databaseHelper.userExist(usernameAdjust, emailAdjust)) {
						System.out.println("There is no user with the provided specifications.");
						break;
					}

					boolean aFlag = false;
					boolean tFlag = false;
					boolean sFlag = false;

					// User object populated from query constructed via fields provided
					User curUser = databaseHelper.findUser(usernameAdjust, emailAdjust);
					boolean[] userRoles = curUser.getRoles();

					System.out.print(
							"Please choose if you would like to add or remove a role from this user? 1. Add 2. Remove");
					String option = scanner.nextLine();

					if (option.equals("1")) {
						
						if (userRoles[0] && (userRoles[1] && userRoles[2])) {
							System.out.println("This user already has all the roles.");
							break;
						}
						// Pick Roles to add - must check if a user does not have one to add it
						System.out.println("You are now adding a role to this user. Here are the options: ");
						if (!userRoles[0])
							System.out.println("1. Administrator");
						if (!userRoles[2])
							System.out.println("2. Student");
						if (!userRoles[3])
							System.out.println("3. Instructor");
						System.out.print("Please select a role: ");
						String rolePick = scanner.nextLine();

						switch (rolePick) {

							// Adding a role to the user
							case "1": {

								// Initial Check is in case they input a number between 1 and 3 but the user
								// already has the role (accidental input that would be valid in other cases)
								if (userRoles[0]) {
									System.out.println(
											"You have selected a role that this user already has. The user's roles will remain the same");
									break;
								}

								// Add role to the local String - used for DB update later
								aFlag = true;
								System.out.println("You have successfully added the Administrator role to this user");

								break;
							}
							case "2": {

								// same as case 1 but for student role
								if (userRoles[2]) {
									System.out.println(
											"You have selected a role that this user already has. The user's roles will remain the same");
									break;
								}
								sFlag = true;
								System.out.println("You have successfully added the Student role to this user");

								break;
							}
							case "3": {

								// same as case 1 but for instructor role
								if (userRoles[1]) {
									System.out.println(
											"You have selected a role that this user already has. The user's roles will remain the same");
									break;
								}
								tFlag = true;
								System.out.println("You have successfully added the Instructor role to this user");

								break;
							}
							default:
								System.out.println("Invalid option. The user will remain unchanged.");
								break;
						}

						// Removing a role from the user
					} else if (option.equals("2")) {

						if (!userRoles[0] && (!userRoles[1] && !userRoles[2])) {
							System.out.println("This user has no roles to remove.");
							break;
						}

						// Pick Roles to remove - must check if a user has one to remove it
						System.out.println("You are now removing a role to this user. Here are the options: ");
						if (userRoles[0])
							System.out.println("1. Administrator");
						if (userRoles[2])
							System.out.println("2. Student");
						if (userRoles[1])
							System.out.println("3. Instructor");
						System.out.print("Please select a role: ");
						String rolePick = scanner.nextLine();

						switch (rolePick) {

							case "1": {

								// Initial Check is in case they input a number between 1 and 3 but the user
								// already has the role (accidental input that would be valid in other cases)
								if (!userRoles[0]) {
									System.out.println(
											"You have selected a role that this user does not have. The user's roles will remain the same");
									break;
								}

								// Remove role from string - again this string will be used for a DB update
								// later
								aFlag = false;
								System.out
										.println("You have successfully removed the Administrator role from this user");

								break;
							}

							// Same as case 1 but for Student role
							case "2": {
								if (!userRoles[2]) {
									System.out.println(
											"You have selected a role that this user does not have. The user's roles will remain the same");
									break;
								}
								sFlag = false;
								System.out.println("You have successfully removed the Student role from this user");

								break;
							}

							// Same as case 1 but for Instructor role
							case "3": {
								if (!userRoles[1]) {
									System.out.println(
											"You have selected a role that this user does not have. The user's roles will remain the same");
									break;
								}
								tFlag = false;
								System.out.println("You have successfully removed the Instructor role from this user");

								break;
							}
							default:
								System.out.println("Invalid option. The user will remain unchanged.");
								break;
						}

						// Final Check is for invalid input on Adding or Removing roles
					} else {
						System.out.println("Invalid Option. The user will remain unchanged.");
						break;
					}

					// Update the user object and the database entry
					userRoles[0] = aFlag;
					userRoles[1] = tFlag;
					userRoles[2] = sFlag;
					curUser.setRoles(userRoles);
					databaseHelper.updateUserRoles(curUser);

					break;
				}

				// Create an Article
				case "6": {

					databaseHelper.createArticle("a");
					break;
				}

				// View a group of Articles
				case "7": {

					System.out.println("Please enter the name of the group of articles you would like to view: ");
					String groupName = scanner.nextLine();
					if(!databaseHelper.groupExist(groupName) || databaseHelper.isGroupSpecial(groupName)) {
						System.out.println("You have either entered a group that does not exist or a Special Access Group which administrators are not allowed to view. Please try again later.");
						break;
					}

					System.out.println("Here are the articles: ");
					databaseHelper.viewGroupedArticles("a", groupName);
					break;
				}

				// View all Articles
				case "8": {

					System.out.println("Here are the articles: ");
					databaseHelper.viewAllArticles("a");
					break;
				}

				// Delete an Article
				case "9": {

					boolean success = databaseHelper.deleteArticle("a");
					if (success)
						System.out.println("Article was properly deleted");
					else
						System.out.println("Article was not able to be deleted");
					break;
				}

				// Restore options
				case "10": {
					System.out.println("Here are your restoration options: ");
					System.out.println("1. Restore all articles");
					System.out.println("2. Restore a general group or Special Access Group of articles");
					System.out.println("Please enter the restoration option you would like to proceed with: ");
					String restoreOption = scanner.nextLine();
					String fileName = "";
					switch (restoreOption) {
						case "1": {

							System.out.println("Please enter the name of the file you would like to restore from: ");
							fileName = scanner.nextLine();
							System.out.println(
									"Would you like to clear all articles in the system before restoring? Please note that if you do not, we will not restore duplicating articles. 1. Yes 2. No");
							String answer = scanner.nextLine();
							if (answer.equals("1"))
								databaseHelper.restore("a", fileName);
							else if (answer.equals("2"))
								databaseHelper.restoreMerge("a", fileName);
							else
								System.out.println("Invalid choice! Try again");
							break;
						}

						// P3: Change restore here to work with groups
						case "2": {

							System.out.println("Please enter the name of the general or Special Access Group: ");
							String group = scanner.nextLine();
							// Check if such a group exists - if no = make group entry into table
							System.out.println("Please enter the name of the file you would like to restore from: ");
							fileName = scanner.nextLine();
							System.out.println(
									"Would you like to clear all articles in the system before restoring? Please note that if you do not, we will not restore duplicating articles. 1. Yes 2. No");
							String answer = scanner.nextLine();
							if (answer.equals("1"))
								databaseHelper.restore("a", fileName);
							else if (answer.equals("2"))
								databaseHelper.restoreMerge("a", fileName);
							else
								System.out.println("Invalid choice! Try again");
							break;
						}

						default: {
							System.out.println("Invalid Option. Try again later.");
							break;
						}

					}

					break;
				}

				// Backup Options
				case "11": {

					System.out.println("Here are your backup options: ");
					System.out.println("1. Backup all articles");
					System.out.println("2. Backup a general group or Special Access Group of articles");
					System.out.println("Please enter the restoration option you would like to proceed with: ");
					String backUp = scanner.nextLine();
					String fileName = "";

					switch (backUp) {

						case "1": {

							System.out.println("Please enter the name of the file you would like to backup to: ");
							fileName = scanner.nextLine();
							// db method to restore all articles to <fileName>.txt
							break;
						}

						case "2": {

							System.out.println(
									"Please enter the name of the general group or Special Access Group of articles you would like to backup: ");
							String group = scanner.nextLine();
							// check if group exists in system - if no - say invalid and exit
							System.out.println("Please enter the name of the file you would like to backup to: ");
							fileName = scanner.nextLine();
							// db method to restore g articles to <fileName>.txt
							break;
						}

						default: {
							System.out.println("Invalid Option. Try again later.");
							break;
						}

					}
					break;
				}

				// List all users in a general group
				case "12": {

					System.out
							.println("Please enter the name of the general group whose users you would like to list: ");
					String group = scanner.nextLine();
					// check if group exists - if no = invalid choice and break
					// if yes = dbmethod to list all users in group (Only display same as what admin
					// sees for all users)
					break;
				}

				// Add a user to a general group
				case "13": {

					System.out.println("Please enter the name of the general group: ");
					String group = scanner.nextLine();
					// check if group exists - if no = invalid choice and break
					String usernameAdd, emailAdd;
					String[] add = get_user_identifiers();
					usernameAdd = add[0];
					emailAdd = add[1];
					// see if there is a valid user with those identifiers
					// db method that handles adding a user to the general group
					break;
				}

				// Remove a user from a general group
				case "14": {

					System.out.println("Please enter the name of the general group: ");
					String group = scanner.nextLine();
					// check if group exists - if no = invalid choice and break
					String usernameRemove, emailRemove;
					String[] remove = get_user_identifiers();
					usernameRemove = remove[0];
					emailRemove = remove[1];
					// see if there is a valid user with those identifiers
					// db methods to check for at least one admin left for the group and removing
					// user from the group
					break;
				}

				// Special Access Group Creation
				case "15": {
					// Check in DB if there is at least one instructor - if no, say you need to have
					// an instructor in the system first
					System.out.println("Please enter the name of the Special Access Group you would like to create: ");
					String groupName = scanner.nextLine();
					// db function that creates group
					System.out.println(
							"Now provide the identifiers for the first Instructor for this Special Access Group");
					String firstUsername, firstEmail;
					String[] firstInstruct = get_user_identifiers();
					firstUsername = firstInstruct[0];
					firstEmail = firstInstruct[1];
					// check if user is an instructor
					// Make sure that this instructor/user can view article bodies and also have
					// admin rights over the group
					System.out.println(
							"A special access group has been set up and an instructor has been given control.");
					System.out.println(
							"You will not be given any rights to this group unless users with administrator permissions for the group grant you the rights.");
					System.out.println(
							"As an administrator, you will never be given the rights to view the bodies of this group.");
					break;
				}

				// Manage Rights to a Special Access Group
				case "16": {

					System.out.println("Please enter the name of the Special Access Group: ");
					String group = scanner.nextLine();
					// check if a valid group and if this user has admin rights over group as an
					// admin
					System.out.println("You now have the following options: ");
					System.out.println("1. Grant a user access to this group");
					System.out.println("2. View all users who have access to this group");
					System.out.println("3. Grant an instructor admin rights to this group");
					System.out.println("4. Delete a user's access to this group");
					System.out.println("5. List of all administrators with admin rights to this group");
					System.out.println("6. List of all instructors with decrypted view rights");
					System.out.println("7. List of all instructors with admin rights");
					System.out.println("8. List of all students with decrypted view rights");
					String access = scanner.nextLine();

					switch (access) {

						// Grant user access
						case "1": {

							String userToAdd, emailToAdd;
							String[] toAdd = get_user_identifiers();
							userToAdd = toAdd[0];
							emailToAdd = toAdd[1];
							// check if real user
							// Check to see what roles they have - set student and instructor Flags
							System.out.println("The user has the following role(s): ");
							// if student =
							System.out.println("1. Student");
							// if instructor =
							System.out.println("2. Instructor");
							String roleToAdd = scanner.nextLine();
							// add && studentFlag or && teachFlag to conditions so that only if option
							// selected and role user has match do they get added - otherwise adding to
							// system without a role
							if (roleToAdd.equals("1")) {
								// add as student = only view rights
							} else if (roleToAdd.equals("2")) {
								// add as instructor - only view rights and no admin rights
							} else
								System.out.println("You picked an invalid role");
							break;
						}

						// View all users with access to SAG
						case "2": {

							// List all users with access to SAG
							// Display as userName, Email, accessRole, Rights: Admin, View (either or both
							// depending on user)
							break;
						}

						// Grant instructor admin rights
						case "3": {

							String instructAdminUser, instructAdminEmail;
							String[] toBeAdmin = get_user_identifiers();
							instructAdminUser = toBeAdmin[0];
							instructAdminEmail = toBeAdmin[1];
							// check if valid user and if they have access to the SAG
							// if so - allow them to have admin rights
							break;
						}

						// Delete a user's access to the system
						case "4": {

							String delUser, delEmail;
							String[] toDel = get_user_identifiers();
							delUser = toDel[0];
							delEmail = toDel[1];
							// check if valid user, then check if last instructor - if yes - do nothing and
							// display issue, if not - remove them from group
							break;
						}

						// List of all admins with admin rights
						case "5": {

							// db method that lists all admins with admin rights
							// only display username, email, prefFirstName
							break;
						}

						// List of instructors with decrypted view rights
						case "6": {

							// db method that lists instructors with these rights
							// only display username, email, prefFirstName
							break;
						}

						// List of all instructors with admin rights
						case "7": {

							// db method that lists all instructors with admin rights
							// only display username, email, prefFirstName
							break;
						}

						// List of all students with decrypted view rights
						case "8": {

							// db method that lists all students that can view bodies
							// only display username, email, prefFirstName
							break;
						}

						default: {
							System.out.println("Invalid option. Try again Later.");
							break;
						}
					}

					break;
				}

				// Logout
				case "17": {

					System.out.println("You have successfully been logged out of the system.");
					break;
				}

				default: {
					System.out.print("Invalid choice. Please try again.");
					break;
				}
			}
		} while (!choice.equals("17")); // Keep looping until admin chooses to logout

		mainLogin(); // route back to main login after admin ends their session
	}

	/**
	 * Instructor Home
	 * 
	 * <p>
	 * This is the instructor home where they can carry out a number of actions that
	 * includes but is not limited to: creating, updating, viewing, deleting,
	 * backing up, and restoring articles.
	 * </p>
	 * 
	 * @throws SQLException Throws error if there is a SQL error from our
	 *                      interaction with the database
	 * @throws Exception    Throws an Exception if there is a SQL error from the
	 *                      helper file and logs the issue
	 */
	private static void instructorHome() throws SQLException, Exception {

		String choice = "";

		do {
			System.out.println("Welcome to the Home Page for Instructors!");
			System.out.println("Here are the actions you can perform: ");
			System.out.println("1. Create an Article");
			System.out.println("2. View an Article");
			System.out.println("3. View a group of Articles");
			System.out.println("4. View Articles by content level");
			System.out.println("5. View all Articles");
			System.out.println("6. Update an Article");
			System.out.println("7. Delete an Article");
			System.out.println("8. Restore Options");
			System.out.println("9. Backup Options");
			System.out.println("10. Search for an Article");
			System.out.println("11. Create a general article group");
			System.out.println("12. List the names of all general article groups");
			System.out.println("13. Delete a general article group");
			System.out.println("14. Add a student to a general article group");
			System.out.println("15. List all students in a general article group");
			System.out.println("16. Remove a student from a general article group");
			System.out.println("17. Special Access Group Management");
			System.out.println("18. Logout");

			choice = scanner.nextLine();

			switch (choice) {

				case "1": {
					databaseHelper.createArticle("t");
					break;
				}
				case "2": {
					System.out.println("Please enter the id of the article you would like to view: ");
					String articleID = scanner.nextLine();
					databaseHelper.viewArticle("t", articleID);
					break;
				}
				case "3": {
					System.out.println("Please enter the name of the group of articles you would like to view: ");
					String groupName = scanner.nextLine();
					System.out.println("Here are the articles: ");
					databaseHelper.viewGroupedArticles("t", groupName);
					break;
				}

				case "4": {
					System.out.println(
							"Please enter the content level of articles you would like. Any other input besides Beginner, Intermediate, Advanced, or Expert will return all articles: ");
					String contentLevel = scanner.nextLine();
					if (!contentLevel.equals("Beginner") && !contentLevel.equals("Intermediate")
							&& !contentLevel.equals("Advanced") && !contentLevel.equals("Expert")) {
						System.out.println(
								"Your input did not match any specified content level. Here are all the articles in the system: ");
						// P3: return all articles
					} else {
						System.out.println("Here are the articles in the content level: " + contentLevel);
						// P3: dbHelper function call with level passed in
					}
					break;
				}

				case "5": {
					System.out.println("Here are the articles: ");
					databaseHelper.viewAllArticles("t");
					break;
				}
				case "6": {
					databaseHelper.updateArticle("t");
					System.out.println("The article was successfully updated.");
					break;
				}
				case "7": {
					boolean success = databaseHelper.deleteArticle("t");
					if (success)
						System.out.println("Article was properly deleted");
					else
						System.out.println("Article was not able to be deleted");
					break;
				}

				case "8": {
					System.out.println("Here are your restoration options: ");
					System.out.println("1. Restore all articles");
					System.out.println("2. Restore a general group of articles");
					System.out.println("Please enter the restoration option you would like to proceed with: ");
					String restoreOption = scanner.nextLine();
					String fileName = "";
					switch (restoreOption) {
						case "1": {

							System.out.println("Please enter the name of the file you would like to restore from: ");
							fileName = scanner.nextLine();
							System.out.println(
									"Would you like to clear all articles in the system before restoring? Please note that if you do not, we will not restore duplicating articles. 1. Yes 2. No");
							String answer = scanner.nextLine();
							if (answer.equals("1"))
								databaseHelper.restore("a", fileName);
							else if (answer.equals("2"))
								databaseHelper.restoreMerge("a", fileName);
							else
								System.out.println("Invalid choice! Try again");
							break;
						}

						// P3: Change restore here to work with groups
						case "2": {

							System.out.println("Please enter the name of the general group: ");
							String group = scanner.nextLine();
							// Check if such a group exists - if no = make group entry into table
							System.out.println("Please enter the name of the file you would like to restore from: ");
							fileName = scanner.nextLine();
							System.out.println(
									"Would you like to clear all articles in the system before restoring? Please note that if you do not, we will not restore duplicating articles. 1. Yes 2. No");
							String answer = scanner.nextLine();
							if (answer.equals("1"))
								databaseHelper.restore("a", fileName);
							else if (answer.equals("2"))
								databaseHelper.restoreMerge("a", fileName);
							else
								System.out.println("Invalid choice! Try again");
							break;
						}

						default: {
							System.out.println("Invalid Option. Try again later.");
							break;
						}

					}

					break;
				}

				case "9": {
					System.out.println("Here are your backup options: ");
					System.out.println("1. Backup all articles");
					System.out.println("2. Backup a general group or Special Access Group of articles");
					System.out.println("Please enter the restoration option you would like to proceed with: ");
					String backUp = scanner.nextLine();
					String fileName = "";

					switch (backUp) {

						case "1": {

							System.out.println("Please enter the name of the file you would like to backup to: ");
							fileName = scanner.nextLine();
							// db method to restore all articles to <fileName>.txt
							break;
						}

						case "2": {

							System.out.println("Please enter the name of the general group or Special Access Group of articles you would like to backup: ");
							String group = scanner.nextLine();
							// check if group exists in system - if no - say invalid and exit
							System.out.println("Please enter the name of the file you would like to backup to: ");
							fileName = scanner.nextLine();
							// db method to restore g articles to <fileName>.txt
							break;
						}

						default: {
							System.out.println("Invalid Option. Try again later.");
							break;
						}

					}
					break;
				}

				case "10": {
					System.out.println("Please search for an article via words, names, or phrases in the Title, Author(s), or Abstract: ");
					String searchCond = scanner.nextLine();
					// P3: Send to DB to find all associated articles - need condition block to say
					// whether any articles matching criteria were found
					break;
				}

				case "11": {

					System.out.println("Please enter the name of the general article group you would like to create: ");
					String group = scanner.nextLine();
					// insert entry into group table
					break;
				}

				case "12": {

					// Print all non-SAG group names - db function
					break;
				}

				case "13": {

					System.out.println("Please enter the name of the general article group you would like to delete: ");
					String group = scanner.nextLine();
					// check if group exists
					// delete on cascade
				}

				// Cases 14-16 ask team about
				case "16": {

					System.out.println("Please enter general article group name: ");
					String group = scanner.nextLine();
					// check if valid group then move on to user
					String userDel, emailDel;
					String[] toDel = get_user_identifiers();
					userDel = toDel[0];
					emailDel = toDel[1];
					// verify user
					// remove student from group
					break;
				}

				// Manage Rights to a Special Access Group
				case "17": {

					System.out.println("Please enter the name of the Special Access Group: ");
					String group = scanner.nextLine();
					// check if a valid group and if this user has admin rights over group as an
					// admin
					System.out.println("You now have the following options: ");
					System.out.println("1. Grant a user access to this group");
					System.out.println("2. View all users who have access to this group");
					System.out.println("3. Grant an instructor admin rights to this group");
					System.out.println("4. Delete a user's access to this group");
					System.out.println("5. List of all administrators with admin rights to this group");
					System.out.println("6. List of all instructors with decrypted view rights");
					System.out.println("7. List of all instructors with admin rights");
					System.out.println("8. List of all students with decrypted view rights");
					String access = scanner.nextLine();

					switch (access) {

						// Grant user access
						case "1": {

							String userToAdd, emailToAdd;
							String[] toAdd = get_user_identifiers();
							userToAdd = toAdd[0];
							emailToAdd = toAdd[1];
							// check if real user
							// Check to see what roles they have - set student and instructor Flags
							System.out.println("The user has the following role(s): ");
							// if student =
							System.out.println("1. Student");
							// if instructor =
							System.out.println("2. Instructor");
							String roleToAdd = scanner.nextLine();
							// add && studentFlag or && teachFlag to conditions so that only if option
							// selected and role user has match do they get added - otherwise adding to
							// system without a role
							if (roleToAdd.equals("1")) {
								// add as student = only view rights
							} else if (roleToAdd.equals("2")) {
								// add as instructor - only view rights and no admin rights
							} else
								System.out.println("You picked an invalid role");
							break;
						}

						// View all users with access to SAG
						case "2": {

							// List all users with access to SAG
							// Display as userName, Email, accessRole, Rights: Admin, View (either or both
							// depending on user)
							break;
						}

						// Grant instructor admin rights
						case "3": {

							String instructAdminUser, instructAdminEmail;
							String[] toBeAdmin = get_user_identifiers();
							instructAdminUser = toBeAdmin[0];
							instructAdminEmail = toBeAdmin[1];
							// check if valid user and if they have access to the SAG
							// if so - allow them to have admin rights
							break;
						}

						// Delete a user's access to the system
						case "4": {

							String delUser, delEmail;
							String[] toDel = get_user_identifiers();
							delUser = toDel[0];
							delEmail = toDel[1];
							// check if valid user, then check if last instructor - if yes - do nothing and
							// display issue, if not - remove them from group
							break;
						}

						// List of all admins with admin rights
						case "5": {

							// db method that lists all admins with admin rights
							// only display username, email, prefFirstName
							break;
						}

						// List of instructors with decrypted view rights
						case "6": {

							// db method that lists instructors with these rights
							// only display username, email, prefFirstName
							break;
						}

						// List of all instructors with admin rights
						case "7": {

							// db method that lists all instructors with admin rights
							// only display username, email, prefFirstName
							break;
						}

						// List of all students with decrypted view rights
						case "8": {

							// db method that lists all students that can view bodies
							// only display username, email, prefFirstName
							break;
						}

						default: {
							System.out.println("Invalid option. Try again Later.");
							break;
						}
					}

					break;
				}

				// Logout
				case "18": {

					System.out.println("You have successfully been logged out of the system.");
					break;
				}
				default:
					System.out.print("Invalid choice. Please try again.");
					break;
			}

		} while (!choice.equals("18")); // Loop until Instructor ends their session

		mainLogin(); // route back to main login as user has ended their current session as
						// instructor

	}

	/**
	 * Asks for login information
	 * <p>
	 * Asks the user for the login information and returns their credentials/login
	 * information
	 * </p>
	 * 
	 * @return Array of 2 Strings containing the username and password in that order
	 */
	private static String[] get_user_credentials() {
		String[] credentials = new String[2];

		System.out.print("Enter Username: ");
		credentials[0] = scanner.nextLine();
		System.out.print("Enter Password: ");
		credentials[1] = scanner.nextLine();

		return credentials;
	}

	/**
	 * Asks for user identifying information
	 * <p>
	 * Asks the admin or instructor to enter a user's username and email to uniquely
	 * identify them
	 * </p>
	 * 
	 * @return Array of 2 Strings containing the username and email in that order
	 */
	private static String[] get_user_identifiers() {
		String[] identifiers = new String[2];

		System.out.print("Enter User's username: ");
		identifiers[0] = scanner.nextLine();
		System.out.print("Enter User's email: ");
		identifiers[1] = scanner.nextLine();

		return identifiers;
	}
}