package org.trypticon.commandbot.conversation;

import java.util.Map;

import org.jabberstudio.jso.Message;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamException;
import org.jabberstudio.jso.PacketRouter;
import org.jabberstudio.jso.util.Utilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a conversation from a single user to the bot.
 */
public class Conversation
{
    /**
     * Log.
     */
    private static final Log log = LogFactory.getLog(Conversation.class);

    /**
     * The router to route responses to.
     */
    private PacketRouter router;

    /**
     * The JID at the other end of the conversation.
     */
    private JID otherJID;

    /**
     * The thread ID for the conversation.
     */
    private String thread;

    /**
     * The map of topics available to the conversation.
     */
    private Map<String, Topic> topicMap;

    /**
     * The current topic.
     */
    private Topic currentTopic;

    /**
     * Constructs the conversation.
     *
     * @param router the router to route responses to.
     * @param otherJID the JID at the other end of the conversation.
     * @param thread the thread ID for the conversation.
     * @param topicMap the map of topics available to the conversation.  The map must at least contain a
     *        key which is the empty string (<code>""</code>), which becomes the initial topic for the
     *        conversation.
     */
    public Conversation(PacketRouter router, JID otherJID, String thread, Map<String, Topic> topicMap)
    {
        this.router = router;
        this.otherJID = otherJID;
        this.thread = thread;
        this.topicMap = topicMap;

        changeTopic("");
    }

    /**
     * Handles a message for this conversation.
     *
     * @param message the message.
     * @return <code>true</code> if the conversation is still taking place, <code>false</code> if handling
     *         this message brought an end to the conversation..
     */
    public boolean handle(Message message)
    {
        log.debug("Handling message for conversation: "  + message);

        boolean result = currentTopic.handle(this, message);

        // TODO: Stack the topics so that it can return to the "next topic up"?  Or handle that in the topics by changing topic to ""?

        return result;
    }

    /**
     * Changes the topic to the topic with the given name.
     *
     * @param name the name of the new topic.
     * @throws IllegalArgumentException if the topic with the given name does not exist.
     */
    public void changeTopic(String name)
    {
        Topic newTopic = topicMap.get(name);

        if (newTopic == null)
        {
            throw new IllegalArgumentException("Topic \"" + name + "\" does not exist");
        }

        currentTopic = newTopic;
    }

    /**
     * Sends a reply to the user at the other end of the conversation.
     *
     * @param message the message to send.
     */
    public void sendMessage(String message)
    {
        if (Utilities.isValidString(message))
        {
            StreamDataFactory factory = router.getDataFactory();
            Message packet = (Message) factory.createPacketNode(factory.createNSI("message", router.getDefaultNamespace()));
            packet.setType(Message.CHAT);
            packet.setTo(otherJID);
            packet.setThread(thread);
            packet.setBody(message);

            try
            {
                router.send(packet);
            }
            catch (StreamException e)
            {
                log.error("Stream exception sending message \"" + message + "\" to " + otherJID);
            }
        }
        else
        {
            log.debug("Empty message received at Conversation", new Exception());
        }
    }
}
