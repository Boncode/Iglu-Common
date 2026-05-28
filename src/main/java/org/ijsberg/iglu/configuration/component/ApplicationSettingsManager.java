package org.ijsberg.iglu.configuration.component;

import org.ijsberg.iglu.configuration.model.ApplicationSettingsDto;
import org.ijsberg.iglu.configuration.model.WebTrafficSettingsDto;

import java.io.IOException;

public interface ApplicationSettingsManager {

    ApplicationSettingsDto getApplicationSettings();

    void saveApplicationSettings(ApplicationSettingsDto applicationSettingsDto) throws IOException;

    void saveWebTrafficSettings(WebTrafficSettingsDto webTrafficSettings);

    WebTrafficSettingsDto getWebTrafficSettings();
}
