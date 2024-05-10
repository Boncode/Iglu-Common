package org.ijsberg.iglu.messaging.module;

import junit.framework.TestCase;
import org.ijsberg.iglu.event.messaging.UserConsumableMessage;
import org.ijsberg.iglu.event.messaging.message.StatusMessage;
import org.ijsberg.iglu.event.module.BasicServiceBroker;
import org.junit.Test;

import java.util.List;

public class BasicServiceBrokerTest extends TestCase {

    @Test
    public void testInvocation() {
        BasicServiceBroker serviceBroker = new BasicServiceBroker();
        StatusMessage statusMessage = new StatusMessage("bogus","Hello");
        serviceBroker.registerService(UserConsumableMessage.class, statusMessage);

        List<UserConsumableMessage> registeredMessages = serviceBroker.getServices(UserConsumableMessage.class);

        assertEquals("Hello", registeredMessages.get(0).getMessageText());
    }
}