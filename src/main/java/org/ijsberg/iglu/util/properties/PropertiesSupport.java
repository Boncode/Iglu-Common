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

package org.ijsberg.iglu.util.properties;

import org.ijsberg.iglu.exception.ResourceException;
import org.ijsberg.iglu.util.io.FileSupport;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.io.*;
import java.util.*;


public class PropertiesSupport {

	public static char KEY_SEPARATOR = '.';


	/**
	 * @param properties
	 * @param sectionKey
	 * @return
	 */
	public static Properties getSubsection(Properties properties, String sectionKey) {
		Properties retval = new Properties();

		for (String key : properties.stringPropertyNames()) {
			if (key.startsWith(sectionKey + KEY_SEPARATOR)) {
				String subkey = key.substring(sectionKey.length() + 1);
				retval.put(subkey, properties.get(key));
			}
		}
		return retval;
	}

	/**
	 * Property trees consists of properties at different levels, names and subnames are separated by dots (.).
	 * If property keys contain dots they are assumed to be composed keys, consisting of subsection names and
	 * a property name.
	 * <p/>
	 * If a property key is composed, such as "settings.username", this method assumes there's a subsection
	 * "settings" containing a property "user name"
	 *
	 * @return a list of keys of subsections (of type String) defined by the first part of a composed property key
	 */
	public static Set<String> getSubsectionKeys(Properties properties) {
		Set<String> retval = new HashSet<String>();
		for (Object keyObj : properties.keySet()) {
			String key = (String) keyObj;
			if (key.indexOf(KEY_SEPARATOR) != -1) {
				retval.add(key.substring(0, key.indexOf(KEY_SEPARATOR)));
			}
		}
		return retval;
	}

	public static Set<String> getRootKeys(Properties properties) {
		Set<String> retval = new HashSet<String>();
		for (Object keyObj : properties.keySet()) {
			String key = (String) keyObj;
			if (key.indexOf(KEY_SEPARATOR) == -1) {
				retval.add(key);
			}
		}
		return retval;
	}

	/**
	 * @param properties
	 * @return
	 */
	public static Map<String, Properties> getSubsections(Properties properties) {
		Map<String, Properties> retval = new TreeMap<>();
		for (Object keyObj : properties.keySet()) {
			String key = (String) keyObj;
			if (key.indexOf(KEY_SEPARATOR) != -1) {
				String subsectionkey = key.substring(0, key.indexOf(KEY_SEPARATOR));
				String subkey = key.substring(subsectionkey.length() + 1);
				Properties props = retval.get(subsectionkey);
				if (props == null) {
					props = new Properties();
					retval.put(subsectionkey, props);
				}
				props.setProperty(subkey, properties.getProperty(key));
			}
		}
		return retval;
	}

	/**
	 * @param properties
	 * @param sectionkey
	 * @return
	 */
	public static Map<String, Properties> getSubsections(Properties properties, String sectionkey) {
		return getSubsections(getSubsection(properties, sectionkey));
	}

	/**
	 * Collects command line properties of the following form:
	 * java Command -key value
	 *
	 * @param args
	 * @return
	 */
	public static Properties getCommandLineProperties(String... args) {
		Properties retval = new Properties();
		for (int i = 0; i < args.length; i++) {
			if (args[i] != null && args[i].startsWith("-") && args[i].length() > 1) {
				String key = args[i].substring(1);
				String value = "";
				if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
					value = args[++i];
				}
				retval.setProperty(key, value);
			}
		}
		return retval;
	}

	public static Properties loadProperties(String fileName) {

		Properties retval = new Properties();
		try {
			File file = new File(fileName);
			InputStream fis = null;
			if (file.exists() && file.length() != 0) {
				try {
					FileSupport.copyFile(file, file.getAbsolutePath() + ".bak", true);
				} catch(IOException e) {
					//process may not have write permission
					System.out.println("creation of backup file " + file.getAbsolutePath() + ".bak failed with message: " + e.getMessage());
				}
				fis = new FileInputStream(file);
			} else {
				file = new File(file.getAbsolutePath() + ".bak");
				if(file.exists() && file.length() != 0) {
					FileSupport.copyFile(file, fileName, true);
					fis = new FileInputStream(file);
				} else {
					fis = FileSupport.getInputStreamFromClassLoader(fileName);
				}
			}
			retval.load(fis);
			fis.close();
		} catch (IOException ioe) {
			throw new ResourceException("can not load properties from file '" + fileName + "'", ioe);
		}
		return retval;
	}


	public static void saveProperties(Properties properties, String fileName) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(fileName);
		PrintStream printStream = new PrintStream(outputStream);

		TreeSet<String> propertyNames = new TreeSet<String>(properties.stringPropertyNames());

		for(String propertyName : propertyNames) {
			printStream.println(propertyName + "=" + properties.getProperty(propertyName));
		}

		printStream.close();
		outputStream.close();

	}

	public static Properties removeSubSection(Properties properties, String sectionKey) {
		Properties subsection = getSubsection(properties, sectionKey);
		for(String subsectionKey : subsection.stringPropertyNames()) {
			properties.remove(sectionKey + KEY_SEPARATOR + subsectionKey);
		}
		return subsection;
	}

	public static void addSubsection(Properties properties, String sectionKey, Properties subsection) {
		for(String subsectionKey : subsection.stringPropertyNames()) {
			properties.setProperty(sectionKey + KEY_SEPARATOR + subsectionKey, subsection.getProperty(subsectionKey));
		}

	}
}
