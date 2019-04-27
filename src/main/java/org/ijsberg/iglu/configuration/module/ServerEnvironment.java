/*
 * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
 *
 * This file is part of Iglu.
 *
 * Iglu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Iglu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ijsberg.iglu.configuration.module;

import org.ijsberg.iglu.Application;
import org.ijsberg.iglu.configuration.*;
import org.ijsberg.iglu.configuration.classloading.ExtendedClassPathClassLoader;
import org.ijsberg.iglu.exception.ResourceException;
import org.ijsberg.iglu.invocation.RootConsole;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.io.FileSupport;
import org.ijsberg.iglu.util.properties.PropertiesSupport;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 * Provides a number of functions that enables an assembly of clusters
 * and components to behave as stand-alone server that can be controlled
 * by shell scripts.
 * <p/>
 * ServerEnvironment does a number of things:
 * <ul>
 * <li>It loads a specific assembly</li>
 * <li>It places itself as component "ServerEnvironment" on the core cluster</li>
 * <li>It fulfills the role of ComponentStarter</li>
 * <li>It provides a shutdown hook for a clean application shutdown</li>
 * </ul>
 *
 * @author jmeetsma
 * @see Assembly
 * @see ComponentStarter
 */
public class ServerEnvironment extends ComponentStarter implements Runnable, SystemUpdater, Application {

	private Thread shutdownHook;
	private boolean isRunning;
	private Assembly assembly;
	private ExtendedClassPathClassLoader extClassLoader;


	/**
	 *
	 */
	public ServerEnvironment(Assembly assembly) {

		super();
		this.assembly = assembly;
		Component serverComponent = new StandardComponent(this);
		assembly.getCoreCluster().connect("ServerEnvironment", serverComponent);

		initializeShutdownHook();
	}

	private void initializeShutdownHook() {
		shutdownHook = new Thread(new ShutdownProcess(this));
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	private String[] assemblyArgs;

	/**
	 * @param args
	 * @throws InstantiationException
	 * @throws ConfigurationException
	 */
	public ServerEnvironment(String... args) throws InstantiationException, ConfigurationException {

		super();
		if(args.length == 0) {
			throw new ConfigurationException("please provide an assembly class name as the first argument");
		}
		this.assemblyArgs = new String[args.length - 1];
		System.arraycopy(args, 1, assemblyArgs, 0, args.length - 1);
		String className = args[0];
		Properties settings = PropertiesSupport.getCommandLineProperties(args);
		this.settings = settings;
		assembly = instantiateAssembly(className, settings);
		//assembly.initialize(assemblyArgs);
		Component rootConsole = new StandardComponent(new RootConsole(assembly));
		assembly.getCoreCluster().connect("RootConsole", rootConsole);
		Component serverComponent = new StandardComponent(this);
		assembly.getCoreCluster().connect("ServerEnvironment", serverComponent, SystemUpdater.class);

		initializeShutdownHook();
	}

	@Override
	public void unzipUpdate() {

		File file = new File("./uploads/admin/update.zip");
		if(file.exists()) {
			try {
				FileSupport.unzip("../", new ZipFile(file));
				FileSupport.deleteFile(file);
			} catch (Exception e) {
				System.out.println(new LogEntry(Level.CRITICAL, "unzipping update failed", e));
			}
		} else {
			System.out.println(new LogEntry(Level.CRITICAL, "./uploads/admin/update.zip does not exist"));
		}


	}

	private boolean reloadRequested = false;


	@Override
	public void reload() {
		//async reload triggered
		reloadRequested = true;
	}

	private synchronized void doReload() {
		reloadRequested = false;
		stop();


		System.gc();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

		extClassLoader = null;
		assembly = null;
		registeredStartables = new LinkedHashMap<Integer, Startable>();

		System.gc();


		try {
			assembly = instantiateAssembly(className, settings);
			//assembly.initialize(assemblyArgs);
			Component rootConsole = new StandardComponent(new RootConsole(assembly));
			assembly.getCoreCluster().connect("RootConsole", rootConsole);
			Component serverComponent = new StandardComponent(this);
			assembly.getCoreCluster().connect("ServerEnvironment", serverComponent);
			start();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void resume() {
		//To change body of implemented methods use File | Settings | File Templates.
	}


	@Override
	public synchronized void start() {
		if(assembly instanceof BasicAssembly) {
			((BasicAssembly)assembly).start();
		} else {
			super.start();
		}
		if (!isRunning) {
			new Thread(this).start();
		}
	}

	@Override
	public synchronized void stop() {
		if(assembly instanceof BasicAssembly) {
			((BasicAssembly)assembly).stop();
		} else {
			super.stop();
		}
		isRunning = false;
	}


	public void run() {
		isRunning = true;
		while (isRunning) {
			try {
				Thread.sleep(333);
				if(reloadRequested) {
					doReload();
				}
			} catch (InterruptedException e) {
				if (isStarted) {
					System.out.println(new LogEntry("Keep-alive thread interrupted..."));
					stop();
				}
			}
		}
	}

	private String className;
	private Properties settings;

	private Assembly instantiateAssembly(String className, Properties settings)
			throws InstantiationException {
		this.className = className;
		this.settings = settings;
		if (settings.containsKey("xcl")) {
			String extendedPath = settings.getProperty("xcl");
			if ("".equals(extendedPath)) {
				throw new ConfigurationException("-xcp : extended class path is missing");
			}
			extClassLoader = new ExtendedClassPathClassLoader(extendedPath, Assembly.class.getPackage());
			Map<String, Set<Object>> multipleResourceLocations = extClassLoader.getMultipleLocationsForResources();
			if (multipleResourceLocations.size() > 0) {
				System.out.println(new LogEntry(Level.CRITICAL, "extended class loader reports multiple locations for " + multipleResourceLocations.size() + " resources"));
			}
			Thread.currentThread().setContextClassLoader(extClassLoader);
			return (Assembly) ReflectionSupport.instantiateClass(extClassLoader, className);
		} else {
			return (Assembly) ReflectionSupport.instantiateClass(className);
		}
	}

	public void reset() {
		stop();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		start();
	}

	public void shutDown() {
		stop();
	}

	public Assembly getAssembly() {
		return assembly;
	}

	private static void printUsage() {
		System.out.println("Usage: java ServerEnvironment <assembly class name>");
		System.out.println("                              [-xcl <extended class path>]");
		System.out.println("                              [-rou]");
		System.out.println("");
		System.out.println("-xcl: enable extended class loader");
		System.out.println("      provide path to jars or directories e.g.: \"./lib:./classes\"");
		System.out.println("-rou: reset or reload on update of classes in extended class path");
	}

	private static ServerEnvironment server;

	/**
	 * Instantiates an assembly and starts startable components.
	 * If an extended class path is provided, eligible classes will be loaded by ExtendedClassPathClassLoader.
	 * <p/>
	 * Note: classes in package org.ijsberg.iglu.configuration will always be loaded by the default class loader.
	 * <p/>
	 * Argument "-rou", reset-on-update enables automatic reset of the application if updated classes are detected
	 * by ExtendedClassPathClassLoader. This function may be used in a development environment.
	 *
	 * @param args <assembly class> [-xcl <extended class path>] [-rou]
	 * @throws Exception
	 * @see ExtendedClassPathClassLoader
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			printUsage();
		} else {
			try {
				System.out.println("Creating server environment for assembly " + args[0]);
				server = new ServerEnvironment(args);
				System.out.println("Starting server ...");
				server.start();
				System.out.println("... Server started");
			} catch(Throwable t) {
				t.printStackTrace();
				System.out.println("clean start of ServerEnvironment failed: shutting down ...");
				stopAsWindowsService(new String[0]);
			}
		}
	}

	public static void startAsWindowsService(String[] args) throws Exception {
		main(args);
		while(server.isRunning) {
			Thread.sleep(1000);
		}
	}

	public static void stopAsWindowsService(String[] args) throws Exception {
		if(server != null/* && server.isRunning*/) {
			try {
				server.stop();
			} catch (ResourceException e) {
				System.out.println(new LogEntry(Level.CRITICAL, "ServerEnvironment is unable to stop cleanly as service", e));
				try {
					server.forceStop();
				} catch (Throwable t) {
					System.out.println("regular service stop is not possible: exiting");
					t.printStackTrace();
					System.exit(1);
				}
			}
		}
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}
}
