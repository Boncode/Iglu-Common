package org.ijsberg.iglu.messaging.module;

import junit.framework.TestCase;
import org.ijsberg.iglu.messaging.UserConsumableMessage;
import org.ijsberg.iglu.messaging.message.EventMessage;
import org.junit.Test;

import java.util.List;

public class BasicServiceBrokerTest extends TestCase {

    @Test
    public void testInvocation() {
        BasicServiceBroker serviceBroker = new BasicServiceBroker();
        EventMessage eventMessage = new EventMessage("bogus","Hello");
        serviceBroker.registerService(UserConsumableMessage.class, eventMessage);

        List<UserConsumableMessage> registeredMessages = serviceBroker.getServices(UserConsumableMessage.class);

        assertEquals("Hello", registeredMessages.get(0).getMessageText());
    }
}