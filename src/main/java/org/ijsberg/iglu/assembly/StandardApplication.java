package org.ijsberg.iglu.assembly;

import org.ijsberg.iglu.Application;
import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.module.BasicAssembly;
import org.ijsberg.iglu.configuration.module.ShutdownProcess;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.io.FileSupport;
import org.ijsberg.iglu.util.properties.IgluProperties;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        System.out.println("adding core assembly " + coreAssembly.getClass().getSimpleName());
        this.coreAssembly = coreAssembly;
        try {
            coreAssembly.getCoreCluster().connect("CoreAssembly", new StandardComponent(coreAssembly));
            System.out.println("core assembly connected to core cluster");
        } catch (ConfigurationException ce) {
            System.out.println("error when connecting core assembly connected to core cluster with message: " + ce.getMessage());
        }
        initializeShutdownHook();
    }

    private BasicAssembly instantiateAssembly(String className, Properties properties) {
        BasicAssembly assembly;
        try {
            String provider;
            List<Object> initArgs = new ArrayList<>();
            if((provider = properties.getProperty("access_manager_provider")) != null) {
                if(!assemblies.containsKey(provider)) {
                    throw new ConfigurationException("accessManagerProvider '" + provider + "' not (yet) added");
                }
                initArgs.add(assemblies.get(provider).getCoreCluster().getInternalComponents().get("AccessManager"));
            }
            if((provider = properties.getProperty("scheduler_provider")) != null) {
                if(!assemblies.containsKey(provider)) {
                    throw new ConfigurationException("scheduler '" + provider + "' not (yet) added");
                }
                initArgs.add(assemblies.get(provider).getCoreCluster().getInternalComponents().get("Scheduler"));
            }
            if(initArgs.size() == 0) {
                assembly = (BasicAssembly) ReflectionSupport.instantiateClass(className, properties);
            } else if (initArgs.size() == 1) {
                assembly = (BasicAssembly) ReflectionSupport.instantiateClass(className, properties, initArgs.get(0));
            } else {
                assembly = (BasicAssembly) ReflectionSupport.instantiateClass(className, properties, initArgs.get(0), initArgs.get(1));
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
            System.out.println("instantiating assembly " + assemblyId);
            if(regardAsCoreAssembly) {
                addCoreAssembly(instantiateAssembly(properties.getProperty(assemblyId + ".class"), properties.getSubsection(assemblyId + ".properties")));
                regardAsCoreAssembly = false;
            } else {
                addAssembly(assemblyId, instantiateAssembly(properties.getProperty(assemblyId + ".class"), properties.getSubsection(assemblyId + ".properties")));
            }
        }
        writeAppReport();
    }

    private void writeAppReport() {
        try {
            StringBuffer data = new StringBuffer();
            Cluster coreCluster = coreAssembly.getCoreCluster();
            data.append(coreAssembly.getClass().getSimpleName() + ".coreCluster\n");
            data.append("INTERNAL COMPONENTS\n");
            for(String componentName : coreCluster.getInternalComponents().keySet()) {
                Component component = coreCluster.getInternalComponents().get(componentName);
                //((StandardComponent)component).get
                data.append(componentName + " : " + component.getClass().getSimpleName() + "\n");
            }
            data.append("EXTERNAL COMPONENTS\n");
            for(Component component : coreCluster.getExternalComponents()) {
                data.append(component.getClass().getSimpleName() + "\n");
            }

            FileSupport.saveTextFile(data.toString(), "app_report.txt");

        } catch (Exception e) {
            System.out.println(new LogEntry(Level.CRITICAL,"cannot write application report", e));
        }
    }

    public void addAssembly(String name, Assembly assembly) {
        System.out.println("adding assembly " + name + " " + assembly.getClass().getSimpleName());
        coreAssembly.getCoreCluster().connect(name, new StandardComponent(assembly));
        System.out.println("assembly " + name + " connected to core cluster");
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

/*
    public StandardApplication(Properties properties) {
        IgluProperties igluProperties = IgluProperties.copy(properties);
    }
*/
    public Assembly getCoreAssembly() {
        return coreAssembly;
    }

}
