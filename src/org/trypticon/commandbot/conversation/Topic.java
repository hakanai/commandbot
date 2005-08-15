package org.trypticon.commandbot.conversation;

import org.jabberstudio.jso.Message;
import org.jdom.Element;

/**
 * Represents a topic of conversation between a user and the bot.
 */
public interface Topic
{
    /**
     * Handles a message for this topic.
     *
     * @param conversation the conversation which is taking place.
     * @param message the message which came in.
     * @return <code>true</code> if the conversation is still in progess, <code>false</code> otherwise.
     */
    public boolean handle(Conversation conversation, Message message);

    /**
     * Configures the topic.
     *
     * @param config the XML configuration.
     */
    void configure(Element config);
}
