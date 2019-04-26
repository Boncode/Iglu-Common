package org.ijsberg.iglu.assembly;

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.module.BasicAssembly;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.invocation.RootConsole;
import org.ijsberg.iglu.logging.Logger;
import org.ijsberg.iglu.logging.module.RotatingFileLogger;
import org.ijsberg.iglu.util.properties.PropertiesSupport;

import java.io.IOException;
import java.util.Map;

public class StandardCoreAssembly extends BasicAssembly {

    protected RotatingFileLogger logger;

    public StandardCoreAssembly() {

        createInfraLayer();
    }

    protected void createInfraLayer() {
        logger = new RotatingFileLogger("logs/" + this.getClass().getSimpleName());
        Component loggerComponent = new StandardComponent(logger);
        core.connect("Logger", loggerComponent);
        loggerComponent.setProperties(PropertiesSupport.loadProperties("conf/logger.properties"));

        Component rootConsole = new StandardComponent(new RootConsole(this));
        core.connect("RootConsole", rootConsole);
    }

    public void addLogAppender(Logger logger) {
        this.logger.addAppender(logger);
    }

}
