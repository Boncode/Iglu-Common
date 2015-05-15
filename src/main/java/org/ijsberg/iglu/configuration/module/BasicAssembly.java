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

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.properties.PropertiesSupport;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 */
public abstract class BasicAssembly implements Assembly {

	private Map<Component, String> propertyFileNamesByComponents = new HashMap<Component, String>();
	protected String configDir = "conf";
	protected static Cluster core;


	public void setProperties(Component component, String fileName) {
		System.out.println(new LogEntry("loading properties for component " + component + " from " + fileName));
		Properties properties = PropertiesSupport.loadProperties(fileName);
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
		PropertiesSupport.saveProperties(component.getProperties(), fileName);
	}

	public void initialize(String[] args) {
		Properties settings = PropertiesSupport.getCommandLineProperties(args);
		if(settings.containsKey("configdir")) {
			configDir = settings.getProperty("configdir");
			System.out.println(new LogEntry(Level.VERBOSE, "working directory is " + new File(configDir).getAbsolutePath()));
		} else {
			System.out.println(new LogEntry(Level.VERBOSE, "setting -configdir not found: working directory is " + new File(configDir).getAbsolutePath()));
		}

	}


	public static void savePropertiesForComponent(Class<?> componentInterface) throws IOException {
		for(Component component : core.getInternalComponents().values()) {
			if(component.implementsInterface(Assembly.class)) {
				component.getProxy(Assembly.class).saveProperties(componentInterface);
			}
		}
	}
}
