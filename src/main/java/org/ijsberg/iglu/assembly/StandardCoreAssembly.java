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

    protected RotatingFileLogger logger;
    protected Component scheduler;

    public StandardCoreAssembly(Properties properties) {
        super(properties);
        createInfraLayer();
    }

    protected void createInfraLayer() {
        if(scheduler == null) {
            scheduler = new StandardComponent(new StandardScheduler());
        }
        core.connect("Scheduler", scheduler);

        logger = new RotatingFileLogger("logs/" + this.getClass().getSimpleName());
        Component loggerComponent = new StandardComponent(logger);
        core.connect("Logger", loggerComponent);
        Properties loggerProperties;
        if(IgluProperties.propertiesExist(home + "/" + configDir + "/logger.properties")) {
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
        loggerComponent.setProperties(loggerProperties);

        if(loggerProperties.getProperty("log_to_standard_out") != null) {
            logger.addAppender(new StandardOutLogger());
        }

        Component rootConsole = new StandardComponent(new RootConsole(this));
        core.connect("RootConsole", rootConsole);
    }

    public void addLogAppender(Logger logger) {
        this.logger.addAppender(logger);
    }

}
