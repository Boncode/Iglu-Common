package org.ijsberg.iglu.util.properties;

import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.ResourceException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MultiplePropertiesConfigurator {

    private List<String> files;
    private List<String> configurableProperties;

    private IgluProperties currentConfigurationProperties;

    public MultiplePropertiesConfigurator(IgluProperties properties) {
        files = Arrays.asList(properties.getPropertyAsArray("files", "[]"));
        configurableProperties = Arrays.asList(properties.getPropertyAsArray("properties", "[]"));

        initializeConfigurableProperties();
    }

    private void initializeConfigurableProperties() {
        IgluProperties initialConfigurationProperties = new IgluProperties();

        for(String fileName : files) {
            if(IgluProperties.propertiesExist(fileName)) {
                IgluProperties fileProperties = IgluProperties.loadProperties(fileName);
                for(String propertyKey : configurableProperties) {
                    if(fileProperties.containsKey(propertyKey)) {
                        initialConfigurationProperties.setProperty(propertyKey, fileProperties.getProperty(propertyKey));
                    }
                }
            } else {
                System.out.println(new LogEntry(Level.CRITICAL, "Configurable settings not found: " + fileName));
            }
        }
        currentConfigurationProperties = initialConfigurationProperties;
    }


    public IgluProperties getConfigurationProperties() {
        return currentConfigurationProperties;
    }

    public void setConfigurationProperties(IgluProperties newProperties) {
        for(String key : currentConfigurationProperties.stringPropertyNames()) {
            if(newProperties.containsKey(key)) {
                currentConfigurationProperties.setProperty(key, newProperties.getProperty(key));
            }
        }
        saveConfigurationProperties();
    }

    private void saveConfigurationProperties() {
        for(String fileName : files) {
            if (IgluProperties.propertiesExist(fileName)) {
                IgluProperties fileProperties = IgluProperties.loadProperties(fileName);
                for(String key : currentConfigurationProperties.stringPropertyNames()) {
                    if(fileProperties.containsKey(key)) {
                        fileProperties.setProperty(key, currentConfigurationProperties.getProperty(key));
                    }
                }
                try {
                    IgluProperties.saveProperties(fileProperties, fileName);
                } catch (IOException e) {
                    System.out.println(new LogEntry(Level.CRITICAL, "failed to save configuration properties in file " + fileName, e));
                    throw new ResourceException("failed to save configuration properties in file " + fileName, e);
                }
            }
        }
    }
}
