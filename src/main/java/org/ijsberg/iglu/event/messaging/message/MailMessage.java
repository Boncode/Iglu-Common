package org.ijsberg.iglu.event.messaging.message;

public class MailMessage extends UserMessage {

    private final String subject;

    public MailMessage(String subject, String messageText) {
        super(messageText);
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }
}
