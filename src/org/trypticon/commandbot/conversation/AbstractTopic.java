package org.trypticon.commandbot.conversation;

import org.jdom.Element;

/**
 * Base implementation of a {@link Topic}.
 */
public abstract class AbstractTopic implements Topic
{
    /**
     * Configures the topic.
     * <p>
     * This base implementation does nothing.
     *
     * @param config the XML configuration.
     */
    public void configure(Element config)
    {
    }
}
