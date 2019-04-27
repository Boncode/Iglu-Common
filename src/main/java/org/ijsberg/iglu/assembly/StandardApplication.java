package org.ijsberg.iglu.assembly;

import org.ijsberg.iglu.Application;
import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.module.BasicAssembly;
import org.ijsberg.iglu.configuration.module.ShutdownProcess;
import org.ijsberg.iglu.configuration.module.StandardComponent;

import java.util.LinkedHashMap;

public class StandardApplication implements Application {

    private Thread shutdownHook;
    private boolean isRunning;

    private BasicAssembly coreAssembly;
    //private BasicAssembly[] assemblies;

    private LinkedHashMap<String, Assembly> assemblies = new LinkedHashMap<>();

    public StandardApplication(BasicAssembly coreAssembly) {
        this.coreAssembly = coreAssembly;
        coreAssembly.getCoreCluster().connect("CoreAssembly", new StandardComponent(coreAssembly));

        initializeShutdownHook();
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
}
