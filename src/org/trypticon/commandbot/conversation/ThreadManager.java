package org.trypticon.commandbot.conversation;

import java.util.Map;
import java.util.HashMap;

import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.Message;
import org.jabberstudio.jso.Stream;
import org.jabberstudio.jso.PacketRouter;
import org.jabberstudio.jso.util.Utilities;
import org.jabberstudio.jso.event.PacketListener;
import org.jabberstudio.jso.event.PacketEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import examples.EchoTopic;
import examples.ProgramDTopic;

/**
 * A class which maps sender JIDs and thread IDs from messages to a {@link Conversation}.
 */
public class ThreadManager implements PacketListener
{
    /**
     * Log.
     */
    private static final Log log = LogFactory.getLog(ThreadManager.class);

    /**
     * The map of topics.
     */
    private Map<String, Topic> topicMap;

    /**
     * The map of conversations.
     */
    private Map<PeerThreadPair, Conversation> conversationMap;

    /**
     * Default constructor.
     */
    public ThreadManager()
    {
        topicMap = new HashMap<String, Topic>();
        conversationMap = new HashMap<PeerThreadPair, Conversation>();

        try
        {
            // By default, everything is ignored.  TODO: Figure out how to build up the topic map... probably from configuration.
            topicMap.put("", new IgnoreAllTopic());
            //topicMap.put("", new EchoTopic());
            //topicMap.put("", new ProgramDTopic());
        }
        catch (Throwable t)
        {
            log.error("Unexpected error creating conversation topic of type ProgramD", t);
            // TODO: Better handling after it works.
            System.exit(1);
        }
    }

    /**
     * Method called when a <tt>Packet</tt> is received or sent.</p>
     *
     * @param event The <tt>PacketEvent</tt> object.
     */
    public void packetTransferred(PacketEvent event)
    {
        if (event.getType() == PacketEvent.RECEIVED && event.getData() instanceof Message)
        {
            Message message = (Message) event.getData();

            // Only pay attention to chat messages which aren't empty.
            if (message.getType() == Message.CHAT && Utilities.isValidString(message.getBody()))
            {
                handle(event.getContext().getRouter(), message);
            }
        }
    }

    /**
     * Handles a message by handing it off to the correct conversation.
     *
     * @param router the router to route responses to.
     * @param message the XMPP message.
     */
    protected void handle(PacketRouter router, Message message)
    {
        PeerThreadPair key = new PeerThreadPair(message.getFrom(), message.getThread());
        Conversation conversation = conversationMap.get(key);

        // TODO: Logic to look up the bare JID in case a conversation with the bare JID exists, and replace with the full one.

        if (conversation == null)
        {
            conversation = new Conversation(router, message.getFrom(), message.getThread(), topicMap);
            conversationMap.put(key, conversation);
        }

        if (!conversation.handle(message))
        {
            conversationMap.remove(key);
        }
    }

    /**
     * A pair formed by the "from" JID and the thread ID of a message.
     */
    private class PeerThreadPair
    {
        /**
         * The JID which we're communicating with.
         */
        private JID peer;

        /**
         * The ID of the thread.
         */
        private String thread;

        /**
         * Constructs the pair.
         *
         * @param peer the JID which we're communicating with.
         * @param thread the ID of the thread.
         * @throws IllegalArgumentException if <code>peer</code> is <code>null</code>.
         */
        private PeerThreadPair(JID peer, String thread)
        {
            if (peer == null)
            {
                throw new IllegalArgumentException("Peer cannot be null.");
            }

            this.peer = peer;
            this.thread = thread;
        }

        /**
         * Compare for equality.
         *
         * @param other the other object.
         * @return <code>true</code> if equal, <code>false</code> if not.
         */
        public boolean equals(Object other)
        {
            if (!(other instanceof PeerThreadPair))
            {
                return false;
            }
            PeerThreadPair that = (PeerThreadPair) other;
            return this.peer.equals(that.peer) &&
                   ((this.thread == null) ? that.thread == null : this.thread.equals(that.thread));
        }

        /**
         * Generates a hashcode.
         *
         * @return the hash code.
         */
        public int hashCode()
        {
            int hashCode = peer.hashCode();
            if (thread != null)
            {
                hashCode *= 27 + thread.hashCode();
            }
            return hashCode;
        }
    }
}
