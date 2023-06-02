package org.ijsberg.iglu.messaging.module;

import junit.framework.TestCase;
import org.ijsberg.iglu.messaging.UserConsumableMessage;
import org.ijsberg.iglu.messaging.message.EventMessage;
import org.junit.Test;

import java.util.List;

public class BasicMessageBrokerTest extends TestCase {

    @Test
    public void testInvocation() {
        BasicMessageBroker messageBroker = new BasicMessageBroker();
        EventMessage eventMessage = new EventMessage("bogus","Hello");
        messageBroker.registerService(UserConsumableMessage.class, eventMessage);

        List<UserConsumableMessage> registeredMessages = messageBroker.getServices(UserConsumableMessage.class);

        assertEquals("Hello", registeredMessages.get(0).getMessageText());
    }
}