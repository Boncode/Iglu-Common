package org.ijsberg.iglu.assembly;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.module.BasicAssembly;
import org.ijsberg.iglu.configuration.module.StandardCluster;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.logging.Logger;
import org.ijsberg.iglu.logging.module.RotatingFileLogger;
import org.ijsberg.iglu.util.properties.PropertiesSupport;

public abstract class CommonAssembly extends BasicAssembly {

    protected RotatingFileLogger logger;

    public void initialize(String[] args) {
        super.initialize(args);
        core = new StandardCluster();
    }

    protected Cluster createInfraLayer() {
        if(PropertiesSupport.propertiesExist("conf/logger.properties")) {
            logger = new RotatingFileLogger("logs/" + this.getClass().getSimpleName());
            Component loggerComponent = new StandardComponent(logger);
            core.connect("Logger", loggerComponent);
            loggerComponent.setProperties(PropertiesSupport.loadProperties("conf/logger.properties"));
        } else {
            System.out.println(new LogEntry(Level.CRITICAL, "cannot load properties for logging"));
        }
        return core;
    }

    public void addLogAppender(Logger logger) {
        this.logger.addAppender(logger);
    }

}