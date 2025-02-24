package org.ijsberg.iglu.configuration.component;

import org.ijsberg.iglu.configuration.model.ApplicationSettingsDto;

public interface ApplicationSettingsProvider {

    ApplicationSettingsDto getApplicationSettings();
}
