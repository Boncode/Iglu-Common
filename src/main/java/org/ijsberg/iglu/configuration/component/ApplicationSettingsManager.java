package org.ijsberg.iglu.configuration.component;

import org.ijsberg.iglu.configuration.model.ApplicationSettingsDto;

import java.io.IOException;

public interface ApplicationSettingsManager {

    ApplicationSettingsDto getApplicationSettings();

    void saveApplicationSettings(ApplicationSettingsDto applicationSettingsDto) throws IOException;
}
