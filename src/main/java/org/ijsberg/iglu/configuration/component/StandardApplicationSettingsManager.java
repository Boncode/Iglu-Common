package org.ijsberg.iglu.configuration.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.model.ApplicationSettingsDto;
import org.ijsberg.iglu.util.io.FileSupport;

import java.io.File;
import java.io.IOException;

public class StandardApplicationSettingsManager implements ApplicationSettingsManager, ApplicationSettingsProvider {

    private static final String DEFAULT_SETTINGS = "{\n" +
            "  \"messagingEnabled\" : true,\n" +
            "  \"messageMode\" : \"polling\",\n" +
            "  \"mail2FAEnabled\" : false,\n" +
            "  \"googleAuth2FAEnabled\" : false,\n" +
            "  \"forcePasswordChange\" : false,\n" +
            "  \"passwordRegex\" : \"^(?!.*:).*(?=.*\\\\d)(?=.*[a-z])(?=.*[A-Z]).{8,64}$\",\n" +
            "  \"passwordTooltip\" : \"The password must contain 8 or more characters, no ':', at least one lowercase character, at least one uppercase character, at least one digit\",\n" +
            "  \"sessionTimeout\" : 300,\n" +
            "  \"sessionTimeoutLoggedIn\" : 1800,\n" +
            "  \"mailCriticalLogs\" : false,\n" +
            "  \"sslEnabled\" : false,\n" +
            "  \"sslKeystoreLocation\" : \"data/boncat.jks\",\n" +
            "  \"sslKeystorePassword\" : \"GhwIWUAEBFo=\",\n" +
            "  \"lockInactiveAccounts\" : true,\n" +
            "  \"lockAfterInactivityInDays\" : 365\n" +
            "}";

    private final String dataFilePath;

    private ApplicationSettingsDto settings;

    public StandardApplicationSettingsManager(String configDir) {
        dataFilePath = configDir + "/" + "app_settings.json";
        if(!FileSupport.fileExists(dataFilePath)) {
            createSettingsFile();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            settings = objectMapper.readValue(new File(dataFilePath), ApplicationSettingsDto.class);
        } catch (IOException e) {
            throw new ConfigurationException("Could not read " + dataFilePath, e);
        }
    }

    private void createSettingsFile() {
        try {
            FileSupport.saveTextFile(DEFAULT_SETTINGS, new File(dataFilePath));
        } catch (IOException e) {
            throw new ConfigurationException("Could not create " + dataFilePath, e);
        }
    }

    @Override
    public ApplicationSettingsDto getApplicationSettings() {
        return new ApplicationSettingsDto( //create a copy
                settings.isMessagingEnabled(),
                settings.getMessageMode(),
                settings.isMail2FAEnabled(),
                settings.isGoogleAuth2FAEnabled(),
                settings.isForcePasswordChange(),
                settings.getPasswordRegex(),
                settings.getPasswordTooltip(),
                settings.getSessionTimeout(),
                settings.getSessionTimeoutLoggedIn(),
                settings.isMailCriticalLogs(),
                settings.isSslEnabled(),
                settings.getSslKeystoreLocation(),
                settings.getSslKeystorePassword(),
                settings.isLockInactiveAccounts(),
                settings.getLockAfterInactivityInDays()
        );
    }

    @Override
    public void saveApplicationSettings(ApplicationSettingsDto applicationSettingsDto) throws IOException {
        if(applicationSettingsDto.getSslKeystorePassword().isEmpty()){
            String sslKeystorePassword = settings.getSslKeystorePassword();
            settings = applicationSettingsDto;
            settings.setSslKeystorePassword(sslKeystorePassword);
        } else {
            settings = applicationSettingsDto;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(dataFilePath), settings);
    }
}
