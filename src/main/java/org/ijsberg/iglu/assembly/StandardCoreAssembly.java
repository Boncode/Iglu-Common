package org.ijsberg.iglu.assembly;

import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.module.BasicAssembly;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.invocation.RootConsole;
import org.ijsberg.iglu.logging.Logger;
import org.ijsberg.iglu.logging.module.RotatingFileLogger;
import org.ijsberg.iglu.logging.module.StandardOutLogger;
import org.ijsberg.iglu.util.properties.IgluProperties;

import java.util.Properties;

public class StandardCoreAssembly extends BasicAssembly {

    protected RotatingFileLogger logger;

    public StandardCoreAssembly(Properties properties) {
        super(properties);
        createInfraLayer();
    }

    protected void createInfraLayer() {
        logger = new RotatingFileLogger("logs/" + this.getClass().getSimpleName());
        Component loggerComponent = new StandardComponent(logger);
        core.connect("Logger", loggerComponent);
        Properties loggerProperties = IgluProperties.loadProperties(configDir + "/logger.properties");
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
