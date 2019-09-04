package org.ijsberg.iglu.assembly;

import org.ijsberg.iglu.Application;
import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.module.BasicAssembly;
import org.ijsberg.iglu.configuration.module.ShutdownProcess;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.util.properties.IgluProperties;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.util.LinkedHashMap;
import java.util.Properties;

public class StandardApplication implements Application {

    private Thread shutdownHook;
    private boolean isRunning;

    private BasicAssembly coreAssembly;

    private LinkedHashMap<String, Assembly> assemblies = new LinkedHashMap<>();

    public StandardApplication(BasicAssembly coreAssembly) {
        addCoreAssembly(coreAssembly);
    }

    public void addCoreAssembly(BasicAssembly coreAssembly) {
        this.coreAssembly = coreAssembly;
        coreAssembly.getCoreCluster().connect("CoreAssembly", new StandardComponent(coreAssembly));
        initializeShutdownHook();
    }

    private BasicAssembly instantiateAssembly(String className, Properties properties) {
        BasicAssembly assembly;
        try {
            String accessManagerProvider;
            if((accessManagerProvider = properties.getProperty("access_manager_provider")) != null) {
                if(!assemblies.containsKey(accessManagerProvider)) {
                    throw new ConfigurationException("accessManagerProvider '" + accessManagerProvider + "' not (yet) added");
                }
                assembly = (BasicAssembly) ReflectionSupport.instantiateClass(className, properties, assemblies.get(accessManagerProvider).getCoreCluster().getInternalComponents().get("AccessManager"));
            } else {
                assembly = (BasicAssembly) ReflectionSupport.instantiateClass(className, properties);
            }
        } catch (InstantiationException e) {
            throw new ConfigurationException("cannot instantiate assembly", e);
        }
        return assembly;
    }

    public StandardApplication(String configFile) {
        IgluProperties properties = IgluProperties.loadProperties(configFile);
        boolean regardAsCoreAssembly = true;
        for(String assemblyId : properties.getSubsectionKeys()) {
            if(regardAsCoreAssembly) {
                addCoreAssembly(instantiateAssembly(properties.getProperty(assemblyId + ".class"), properties.getSubsection(assemblyId + ".properties")));
                regardAsCoreAssembly = false;
            } else {
                addAssembly(assemblyId, instantiateAssembly(properties.getProperty(assemblyId + ".class"), properties.getSubsection(assemblyId + ".properties")));
            }
        }
    }

    public void addAssembly(String name, Assembly assembly) {
        coreAssembly.getCoreCluster().connect(name, new StandardComponent(assembly));
        assemblies.put(name, assembly);
    }

    private void initializeShutdownHook() {
        shutdownHook = new Thread(new ShutdownProcess(this));
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void start() {
        coreAssembly.start();
    }

    @Override
    public boolean isStarted() {
        return coreAssembly.isStarted();
    }

    @Override
    public void stop() {
        coreAssembly.stop();
    }


    public StandardApplication(Properties properties) {
        IgluProperties igluProperties = IgluProperties.copy(properties);
    }

    public Assembly getCoreAssembly() {
        return coreAssembly;
    }

}
