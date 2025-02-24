package org.ijsberg.iglu.configuration.model;

public class ApplicationSettingsDto {

    private boolean messagingEnabled;
    private String messageMode;
    private boolean mail2FAEnabled;
    private boolean googleAuth2FAEnabled;
    private boolean forcePasswordChange;
    private String passwordRegex;
    private String passwordTooltip;
    private int sessionTimeout;
    private int sessionTimeoutLoggedIn;
    private boolean mailCriticalLogs;
    private boolean sslEnabled;
    private String sslKeystoreLocation;
    private String sslKeystorePassword;
    private boolean lockInactiveAccounts;
    private int lockAfterInactivityInDays;


    public ApplicationSettingsDto() {

    }

    public ApplicationSettingsDto(
            boolean messagingEnabled,
            String messageMode,
            boolean mail2FAEnabled,
            boolean googleAuth2FAEnabled,
            boolean forcePasswordChange,
            String passwordRegex,
            String passwordTooltip,
            int sessionTimeout,
            int sessionTimeoutLoggedIn,
            boolean mailCriticalLogs,
            boolean sslEnabled,
            String sslKeystoreLocation,
            String sslKeystorePassword,
            boolean lockInactiveAccounts,
            int lockAfterInactivityInDays
    ) {
        this.messagingEnabled = messagingEnabled;
        this.messageMode = messageMode;
        this.mail2FAEnabled = mail2FAEnabled;
        this.googleAuth2FAEnabled = googleAuth2FAEnabled;
        this.forcePasswordChange = forcePasswordChange;
        this.passwordRegex = passwordRegex;
        this.passwordTooltip = passwordTooltip;
        this.sessionTimeout = sessionTimeout;
        this.sessionTimeoutLoggedIn = sessionTimeoutLoggedIn;
        this.mailCriticalLogs = mailCriticalLogs;
        this.sslEnabled = sslEnabled;
        this.sslKeystoreLocation = sslKeystoreLocation;
        this.sslKeystorePassword = sslKeystorePassword;
        this.lockInactiveAccounts = lockInactiveAccounts;
        this.lockAfterInactivityInDays = lockAfterInactivityInDays;
    }


    public boolean isMessagingEnabled() {
        return messagingEnabled;
    }

    public String getMessageMode() {
        return messageMode;
    }

    public boolean isMail2FAEnabled() {
        return mail2FAEnabled;
    }

    public boolean isGoogleAuth2FAEnabled() {
        return googleAuth2FAEnabled;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public String getPasswordRegex() {
        return passwordRegex;
    }

    public String getPasswordTooltip() {
        return passwordTooltip;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public int getSessionTimeoutLoggedIn() {
        return sessionTimeoutLoggedIn;
    }

    public boolean isMailCriticalLogs() {
        return mailCriticalLogs;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public boolean isLockInactiveAccounts() {
        return lockInactiveAccounts;
    }

    public int getLockAfterInactivityInDays() {
        return lockAfterInactivityInDays;
    }

    public String getSslKeystoreLocation() {
        return sslKeystoreLocation;
    }

    public String getSslKeystorePassword() {
        return sslKeystorePassword;
    }

    public void setSslKeystorePassword(String sslKeystorePassword) {
        this.sslKeystorePassword = sslKeystorePassword;
    }

    @Override
    public String toString() {
        return
        "{\n" +
            "\tmessagingEnabled: " + messagingEnabled + ",\n" +
            "\tmessageMode: " + messageMode + ",\n" +
            "\tmail2FAEnabled: " + mail2FAEnabled + ",\n" +
            "\tgoogleAuth2FAEnabled: " + googleAuth2FAEnabled + ",\n" +
            "\tforcePasswordChange: " + forcePasswordChange + ",\n" +
            "\tpasswordRegex: " + passwordRegex + ",\n" +
            "\tpasswordTooltip: " + passwordTooltip + ",\n" +
            "\tsessionTimeout: " + sessionTimeout + ",\n" +
            "\tsessionTimeoutLoggedIn: " + sessionTimeoutLoggedIn + ",\n" +
            "\tmailCriticalLogs: " + mailCriticalLogs + ",\n" +
            "\tsslEnabled: " + sslEnabled + ",\n" +
            "\tsslKeystoreLocation: " + sslKeystoreLocation + ",\n" +
            "\tsslKeystorePassword: " + "[redacted]" + ",\n" +
            "\tlockInactiveAccounts: " + lockInactiveAccounts + ",\n" +
            "\tlockAfterInactivityInDays: " + lockAfterInactivityInDays + "\n" +
        "}";
    }
}
