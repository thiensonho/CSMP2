package cs.chat.client;

/**
 * Interface for the Client Frame
 */
public interface ClientListener {
    /**
     * Called when the send button is pressed
     * @param msg The message in the text box
     */
    public void messageSend(String msg);
}
