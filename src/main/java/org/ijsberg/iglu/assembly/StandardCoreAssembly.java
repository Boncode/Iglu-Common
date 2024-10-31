package org.ijsberg.iglu.assembly;

import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.module.BasicAssembly;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.invocation.RootConsole;
import org.ijsberg.iglu.logging.Logger;
import org.ijsberg.iglu.logging.module.RotatingFileLogger;
import org.ijsberg.iglu.logging.module.StandardOutLogger;
import org.ijsberg.iglu.scheduling.module.StandardScheduler;
import org.ijsberg.iglu.util.properties.IgluProperties;

import java.io.IOException;
import java.util.Properties;

public class StandardCoreAssembly extends BasicAssembly {

    protected Component logger;
    protected Component scheduler;
    protected Component rootConsole;

    public StandardCoreAssembly(Properties properties) {
        super(properties);
        createInfraLayer();
    }

    protected void createInfraLayer() {
        if(scheduler == null) {
            scheduler = new StandardComponent(new StandardScheduler());
        }
        core.connect("Scheduler", scheduler);

        if(logger == null) {
            RotatingFileLogger rotatingFileLogger = new RotatingFileLogger("logs/" + this.getClass().getSimpleName());
            logger = new StandardComponent(rotatingFileLogger);
            core.connect("Logger", logger);
            Properties loggerProperties;
            if (IgluProperties.propertiesExist(home + "/" + configDir + "/logger.properties")) {
                loggerProperties = IgluProperties.loadProperties(home + "/" + configDir + "/logger.properties");
            } else {
                loggerProperties = new IgluProperties();
                loggerProperties.setProperty("log_level", "DEBUG");
                loggerProperties.setProperty("log_to_standard_out", "true");
                try {
                    IgluProperties.saveProperties(loggerProperties, home + "/" + configDir + "/logger.properties");
                } catch (IOException e) {
                    System.err.println("could not save logger.properties with message: " + e.getMessage());
                }
            }
            logger.setProperties(loggerProperties);

            if (Boolean.parseBoolean(loggerProperties.getProperty("log_to_standard_out", "false"))) {
                rotatingFileLogger.addAppender(new StandardOutLogger());
            }
        }

        if(rootConsole == null) {
            rootConsole = new StandardComponent(new RootConsole(this));
            core.connect("RootConsole", rootConsole);
        }
    }

}
