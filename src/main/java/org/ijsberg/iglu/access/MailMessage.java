package org.ijsberg.iglu.access;

public class MailMessage extends UserMessage {

    private String subject;

    public MailMessage(String subject, String messageText) {
        super(messageText);
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }
}
