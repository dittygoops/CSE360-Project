package simpleDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * System for managing help messages between users
 */
public class HelpSystem {
    /** List to store all help messages */
    private static List<Message> helpMessages = new ArrayList<>();

    /**
     * Sends a generic help message from one user to another
     * @param message The message content to send
     * @param user The user sending/receiving the message
     */
    public static void sendGenericMessage(String message, User user) {
        Message msg = new Message(message, user);
        helpMessages.add(msg);
    }

    /**
     * Sends a specific help message from one user to another
     * @param message The message content to send
     * @param user The user sending/receiving the message
     */
    public static void sendSpecificMessage(String message, User user) {
        Message msg = new Message(message, user);
        helpMessages.add(msg);
    }
}

/**
 * Class representing a help message between users
 */
class Message {
    /** The content of the message */
    private String message;
    /** The user associated with this message */
    private User user;

    /**
     * Creates a new Message
     * @param message The content of the message
     * @param user The user associated with the message
     */
    public Message(String message, User user) {
        this.message = message;
        this.user = user;
    }

    /**
     * Gets the message content
     * @return The message content
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the user associated with this message
     * @return The user
     */
    public User getUser() {
        return user;
    }
}