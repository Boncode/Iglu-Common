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

import org.ijsberg.iglu.configuration.*;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.properties.IgluProperties;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 */
public class BasicAssembly implements Assembly, Startable {

	private Map<Component, String> propertyFileNamesByComponents = new HashMap<Component, String>();
	protected String configDir = "conf";
	protected Cluster core;// = new StandardCluster();
	protected Map<String, Cluster> clusters = new LinkedHashMap<String, Cluster>();

	private ComponentStarter assemblyStarter = new ComponentStarter();
	private Component assemblyStarterComponent = new StandardComponent(assemblyStarter);

	protected Properties properties;
	protected String home;

	public BasicAssembly(Properties properties) {

		this.properties = properties;
		home = properties.getProperty("home", ".");

		configDir = properties.getProperty("configDir", configDir);
		System.out.println(new LogEntry(Level.VERBOSE, "working directory is " + new File(configDir).getAbsolutePath()));
		core = createCluster("core");
		clusters.put("core", core);
	}

	public void setProperties(Component component, String fileName) {
		System.out.println(new LogEntry("loading properties for component " + component + " from " + fileName));
		Properties properties = IgluProperties.loadProperties(home + '/' + configDir + "/" + fileName);
		if(properties == null) {
			throw new ConfigurationException("properties file " + fileName + " not found");
		}
		propertyFileNamesByComponents.put(component, fileName);
		component.setProperties(properties);
	}

	@Override
	public void saveProperties() throws IOException {
		for(Component component : propertyFileNamesByComponents.keySet()) {
			saveProperties(component);
		}
	}

	@Override
	public void saveProperties(Class<?> componentInterface) throws IOException {
		for(Component component : propertyFileNamesByComponents.keySet()) {
			if(component.implementsInterface(componentInterface)) {
				saveProperties(component);
			}
		}
	}

	private void saveProperties(Component component) throws IOException {
		String fileName = propertyFileNamesByComponents.get(component);
		System.out.println(new LogEntry("" + propertyFileNamesByComponents));
		System.out.println(new LogEntry("saving properties to " + fileName));
		IgluProperties.saveProperties(component.getProperties(), configDir + "/" + fileName);
	}

	public void savePropertiesForComponent(Class<?> componentInterface) throws IOException {
		for(Component component : core.getInternalComponents().values()) {
			if(component.implementsInterface(Assembly.class)) {
				component.getProxy(Assembly.class).saveProperties(componentInterface);
			}
		}
	}

	public final Map<String, Cluster> getClusters() {
		return clusters;
	}

	protected Cluster createCluster(String name) {
		if(clusters.containsKey(name)) {
			throw new ConfigurationException("cluster with name " + name + " already registered");
		}
		Cluster cluster = new StandardCluster();
		clusters.put(name, cluster);
		cluster.connect("AssemblyStarter", assemblyStarterComponent);
		if(core != null) {
			core.connect(name, new StandardComponent(cluster));
		}
		return cluster;
	}


	@Override
	public final Cluster getCoreCluster() {
		return core;
	}

	@Override
	public void start() {
		assemblyStarter.start();
	}

	@Override
	public boolean isStarted() {
		return assemblyStarter.isStarted;
	}

	@Override
	public void stop() {
		assemblyStarter.stop();
	}
}
