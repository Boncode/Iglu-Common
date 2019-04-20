package org.ijsberg.iglu.assembly;

import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.module.BasicAssembly;
import org.ijsberg.iglu.configuration.module.StandardCluster;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.logging.Logger;
import org.ijsberg.iglu.logging.module.RotatingFileLogger;
import org.ijsberg.iglu.util.properties.PropertiesSupport;

public abstract class CommonAssembly extends BasicAssembly {

    protected RotatingFileLogger logger;

    public void initialize(String[] args) {
        super.initialize(args);
        core = new StandardCluster();
    }

    protected void createInfraLayer() {
        logger = new RotatingFileLogger("logs/" + this.getClass().getSimpleName());
        Component loggerComponent = new StandardComponent(logger);
        core.connect("Logger", loggerComponent);
        loggerComponent.setProperties(PropertiesSupport.loadProperties("conf/logger.properties"));
    }

    public void addLogAppender(Logger logger) {
        this.logger.addAppender(logger);
    }

}