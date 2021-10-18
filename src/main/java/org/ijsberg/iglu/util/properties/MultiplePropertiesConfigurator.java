package org.ijsberg.iglu.util.properties;

import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;

import java.io.IOException;
import java.util.*;

public class MultiplePropertiesConfigurator {

    private List<String> files;
    private List<String> configurableProperties;

    private IgluProperties currentPortalConfigurationProperties;

    public MultiplePropertiesConfigurator(IgluProperties properties) {
        files = Arrays.asList(properties.getPropertyAsArray("files", "[]"));
        configurableProperties = Arrays.asList(properties.getPropertyAsArray("properties", "[]"));

        initializeConfigurableProperties();
    }

    private void initializeConfigurableProperties() {
        IgluProperties initialPortalConfigurationProperties = new IgluProperties();

        for(String fileName : files) {
            if(IgluProperties.propertiesExist(fileName)) {
                IgluProperties fileProperties = IgluProperties.loadProperties(fileName);
                for(String propertyKey : configurableProperties) {
                    if(fileProperties.containsKey(propertyKey)) {
                        initialPortalConfigurationProperties.setProperty(propertyKey, fileProperties.getProperty(propertyKey));
                    }
                }
            } else {
                System.out.println(new LogEntry(Level.CRITICAL, "Configurable portal settings not found: " + fileName));
            }
        }
        currentPortalConfigurationProperties = initialPortalConfigurationProperties;
    }


    public IgluProperties getPortalConfigurationProperties() {
        return currentPortalConfigurationProperties;
    }

    public void setPortalConfigurationProperties(IgluProperties newProperties) {
        for(String key : currentPortalConfigurationProperties.stringPropertyNames()) {
            if(newProperties.containsKey(key)) {
                currentPortalConfigurationProperties.setProperty(key, newProperties.getProperty(key));
            }
        }
        savePortalProperties();
    }

    private void savePortalProperties() {
        for(String fileName : files) {
            if (IgluProperties.propertiesExist(fileName)) {
                IgluProperties fileProperties = IgluProperties.loadProperties(fileName);
                for(String key : currentPortalConfigurationProperties.stringPropertyNames()) {
                    if(fileProperties.containsKey(key)) {
                        fileProperties.setProperty(key, currentPortalConfigurationProperties.getProperty(key));
                    }
                }
                try {
                    IgluProperties.saveProperties(fileProperties, fileName);
                } catch (IOException e) {
                    System.out.println(new LogEntry(Level.CRITICAL, "failed to save portal configuration properties in file " + fileName, e));
                }
            }
        }
    }
}
