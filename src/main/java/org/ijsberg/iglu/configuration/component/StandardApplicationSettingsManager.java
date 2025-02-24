package org.ijsberg.iglu.configuration.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.model.ApplicationSettingsDto;

import java.io.File;
import java.io.IOException;

public class StandardApplicationSettingsManager implements ApplicationSettingsManager, ApplicationSettingsProvider {

    private final String dataFilePath;

    private ApplicationSettingsDto settings;

    public StandardApplicationSettingsManager(String configDir) {
        dataFilePath = configDir + "/" + "app_settings.json";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            settings = objectMapper.readValue(new File(dataFilePath), ApplicationSettingsDto.class);
        } catch (IOException e) {
            throw new ConfigurationException("Could not read " + dataFilePath, e);
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
