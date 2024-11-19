import java.util.List;

package simpleDatabase;

public class HelpSystem {
    private static List<Message> helpMessages;

    public static void sendGenericMessage(String message, User user) {
        Message msg = new Message(message, user);
        helpMessages.add(msg);
    }

    public static void sendSpecificMessage(String message, User user) {
        Message msg = new Message(message, user);
        helpMessages.add(msg);
    }
}

class Message {
    private String message;
    private User user;

    public Message(String message, User user) {
        this.message = message;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}
