package org.ijsberg.iglu.assembly;

import org.ijsberg.iglu.Application;
import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.module.BasicAssembly;
import org.ijsberg.iglu.configuration.module.ShutdownProcess;

public class StandardApplication implements Application {

    private Thread shutdownHook;
    private boolean isRunning;

    private BasicAssembly coreAssembly;
    private BasicAssembly[] assemblies;

    public StandardApplication(BasicAssembly coreAssembly, BasicAssembly ... assemblies) {
        this.coreAssembly = coreAssembly;
        this.assemblies = assemblies;

        initializeShutdownHook();
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
