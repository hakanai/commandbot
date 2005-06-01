package examples;

import org.trypticon.commandbot.conversation.Topic;
import org.trypticon.commandbot.conversation.Conversation;

import org.jabberstudio.jso.Message;

/**
 * A simple topic which relays everything back to the user.
 */
public class EchoTopic implements Topic
{
    /**
     * Handles a message for this topic.
     *
     * @param conversation the conversation which is taking place.
     * @param message      the message which came in.
     * @return <code>true</code> if the conversation is still in progess, <code>false</code> otherwise.
     */
    public boolean handle(Conversation conversation, Message message)
    {
        conversation.sendMessage(message.getBody());
        return true;
    }
}
