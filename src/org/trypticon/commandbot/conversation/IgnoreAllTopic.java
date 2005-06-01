package org.trypticon.commandbot.conversation;

import org.jabberstudio.jso.Message;

/**
 * A basic topic which treats all messages at the end of the conversation.
 */
public class IgnoreAllTopic implements Topic
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
        // Conversation is over.  Snubbed!
        return false;
    }
}
