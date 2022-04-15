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

import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.util.ResourceException;
import org.ijsberg.iglu.util.collection.CollectionSupport;
import org.ijsberg.iglu.util.collection.ListHashMap;
import org.ijsberg.iglu.util.io.FileData;
import org.ijsberg.iglu.util.io.FileSupport;
import org.ijsberg.iglu.util.misc.Line;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.io.*;
import java.util.*;

/**
 * Iglu properties adds a number of features to Properties such as order preserving and subsection retrieval
 */
public class IgluProperties extends Properties {

	public static char KEY_SEPARATOR = '.';

	private Set<String> orderedPropertyNames = new LinkedHashSet();

	private ListHashMap<String,String> linesOfComment = new ListHashMap<>();

	private List<String> linesGathered = new ArrayList<>();

	public IgluProperties() {
	}

	public IgluProperties(Properties properties) {
		if(properties instanceof IgluProperties) {
			IgluProperties igluProperties = (IgluProperties)properties;
			//this.orderedPropertyNames = new LinkedHashSet<>(igluProperties.orderedPropertyNames);
			this.linesOfComment = new ListHashMap<>(igluProperties.linesOfComment);
			//this.linesGathered = new ArrayList<>(igluProperties.linesGathered);
		}
		merge(properties);
	}

	public static IgluProperties copy(Properties properties) {
		IgluProperties igluProperties = new IgluProperties();
		igluProperties.merge(properties);
		return igluProperties;
	}

    public static Map<String, IgluProperties> getPropertiesFromDir(File propertiesDir) {
        Map<String, IgluProperties> analysisPropertiesMap = new LinkedHashMap<>();
        for(String fileName : propertiesDir.list()) {
            if(fileName.endsWith(".properties")) {
                IgluProperties analysisProperties = loadProperties(propertiesDir.getPath() + "/" + fileName);
                analysisPropertiesMap.put(fileName, analysisProperties);
            }
        }
        return analysisPropertiesMap;
    }

    public void merge(Properties properties) {
		for(String key : properties.stringPropertyNames()) {
			set(key, properties.getProperty(key));
		}
	}

	public void replace(Properties properties) {
		merge(properties);
		for(String key : stringPropertyNames()) {
			if(!properties.containsKey(key)) {
				remove(key);
			}
		}
	}

	public LinkedHashMap<String, String> toOrderedMap() {
		LinkedHashMap result = new LinkedHashMap();
		for(String propertyName : orderedPropertyNames) {
			result.put(propertyName, getProperty(propertyName));
		}
		return result;
	}

	public static void throwIfKeysMissing(Properties properties, String ... keys) {
		List<String> missingKeys = getMissingKeys(properties, keys);
		if(!missingKeys.isEmpty()) {
			throw new ConfigurationException("please provide missing properties (" + CollectionSupport.format(missingKeys, ", ") + ")");
		}
	}

	public static boolean checkKeysMissing(Properties properties, String ... keys) {
		List<String> missingKeys = getMissingKeys(properties, keys);
		return !missingKeys.isEmpty();
	}

	private static List<String> getMissingKeys(Properties properties, String[] keys) {
		List<String> missingKeys = new ArrayList<>();
		for(String key : keys) {
			if(properties.getProperty(key) == null) {
				missingKeys.add(key);
			}
		}
		return missingKeys;
	}

	public IgluProperties set(String key, String value) {
		setProperty(key, value);
		return this;
	}

	public Object setProperty(String key, String value) {
		Object retval = super.setProperty(key, value);
		orderedPropertyNames.add(key);
		return retval;
	}

	/**
	 * @param properties
	 * @param sectionKey
	 * @return
	 */
	public static Properties getSubsection(Properties properties, String sectionKey) {

		IgluProperties retval = new IgluProperties();

		for (String key : properties.stringPropertyNames()) {
			if (key.startsWith(sectionKey + KEY_SEPARATOR)) {
				String subkey = key.substring(sectionKey.length() + 1);
				retval.setProperty(subkey, properties.getProperty(key));
			}
		}
		return retval;
	}

	public IgluProperties getSubsection(String sectionKey) {
		return (IgluProperties) getSubsection(this, sectionKey);
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
		Set<String> retval = new LinkedHashSet<>();
		for (String key : properties.stringPropertyNames()) {
			if (key.indexOf(KEY_SEPARATOR) != -1) {
				retval.add(key.substring(0, key.indexOf(KEY_SEPARATOR)));
			}
		}
		return retval;
	}

	public Set<String> getSubsectionKeys() {
		return getSubsectionKeys(this);
	}


	public static Set<String> getRootKeys(Properties properties) {
		LinkedHashSet<String> retval = new LinkedHashSet<String>();
		for (String key : properties.stringPropertyNames()) {
			if (key.indexOf(KEY_SEPARATOR) == -1) {
				retval.add(key);
			}
		}
		return retval;
	}

	public Set<String> getRootKeys() {
		return getRootKeys(this);
	}

	/**
	 * @param properties
	 * @return
	 */
	public static <T extends Properties> Map<String, T> getSubsections(T properties) {
		Map<String, Properties> retval = new LinkedHashMap<>();
		for (Object keyObj : properties.keySet()) {
			String key = (String) keyObj;
			if (key.indexOf(KEY_SEPARATOR) != -1) {
				String subsectionkey = key.substring(0, key.indexOf(KEY_SEPARATOR));
				String subkey = key.substring(subsectionkey.length() + 1);
				Properties props = retval.get(subsectionkey);
				if (props == null) {
					props = new IgluProperties();
					retval.put(subsectionkey, props);
				}
				props.setProperty(subkey, properties.getProperty(key));
			}
		}
		return (Map<String, T>) retval;
	}

	public Map<String, IgluProperties> getSubsections() {
		return getSubsections(this);
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
	public static IgluProperties getCommandLineProperties(String... args) {
		IgluProperties retval = new IgluProperties();
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

	public static boolean propertiesExist(String fileName) {
		File file = new File(fileName);
		return file.exists() || FileSupport.class.getClassLoader().getResourceAsStream(fileName) != null;
	}


	public static IgluProperties loadPropertiesFromText(String text) {

		IgluProperties retval = new IgluProperties();
		try {
			InputStream isSuper = new ByteArrayInputStream(text.getBytes());
			InputStream isThis = new ByteArrayInputStream(text.getBytes());
			retval.load(isSuper, isThis);
			isThis.close();
			isSuper.close();
		} catch (IOException ioe) {
			throw new ResourceException("can not load properties from text '" + text + "'");
		}
		return retval;
	}

	public static IgluProperties loadPropertiesFromMap(Map<String, String> propertiesMap) {

		IgluProperties retval = new IgluProperties();
		for(String key : propertiesMap.keySet()) {
			retval.set(key, propertiesMap.get(key));
		}
		return retval;
	}

	public static IgluProperties loadProperties(String fileName) {

		IgluProperties retval = new IgluProperties();
		File basefile = new File(fileName);
		try {
			InputStream isSuper = getInputStream(fileName);
			InputStream isThis = getInputStream(fileName);
			retval.load(isSuper, isThis);
			isThis.close();
			isSuper.close();
		} catch (IOException ioe) {
			throw new ResourceException("can not load properties from file '" + fileName + "'" +
					" (full path: " + basefile.getAbsolutePath() + ")", ioe);
		}
		return retval;
	}

	public static InputStream getInputStream(String fileName) throws IOException {

		File file = new File(fileName);
		InputStream fis = null;
		if (file.exists() /*&& file.length() != 0*/) {
/*			try {
				FileSupport.copyFile(file, file.getAbsolutePath() + ".bak", true);
			} catch(IOException e) {
				//process may not have write permission
				System.out.println("creation of backup file " + file.getAbsolutePath() + ".bak failed with message: " + e.getMessage());
			}*/
			fis = new FileInputStream(file);
		} else {
/*			file = new File(file.getAbsolutePath() + ".bak");
			if(file.exists() && file.length() != 0) {
				FileSupport.copyFile(file, fileName, true);
				fis = new FileInputStream(file);
			} else {*/
				fis = FileSupport.getInputStreamFromClassLoader(fileName);
			//}
		}
		return fis;
	}

	private void load(InputStream inputStreamForSuper, InputStream inputStreamForThis) throws IOException {
		orderedPropertyNames.clear();
		linesOfComment.clear();
		linesGathered.clear();

		super.load(inputStreamForSuper);
		List<Line> lines = FileSupport.getLinesFromText("bogus", new InputStreamReader(inputStreamForThis));
		for(Line line : lines) {
			processCommentAndEmpty(line);
			String key = getKey(line);
			if(key != null) {
				orderedPropertyNames.add(key);
				linesOfComment.put(key, new ArrayList<>(linesGathered));
				linesGathered.clear();
			}
		}
	}

	public Set<String> stringPropertyNames() {
		LinkedHashSet<String> retval = new LinkedHashSet<>(orderedPropertyNames);
		return retval;
	}

	public boolean isMarkedAsArray(String key) {
		String value = getProperty(key);
		return value != null && value.startsWith("[") && value.endsWith("]");
	}

	public String[] getPropertyAsArray(String key) {
		return getPropertyAsArray(key, null);
	}

	public String[] getPropertyAsArray(String key, String defaultVal) {
		String value = getProperty(key, defaultVal);
		return convertToArray(value);
	}

	public static String[] convertToArray(String value) {
	 	List<String> list = convertToList(value);
		String[] retval = new String[list.size()];
		for(int i = 0; i < list.size(); i++) {
			retval[i] = list.get(i).trim();
		}
		return retval;
	}

	public static List<String> convertToList(String value) {
		if(value == null) {
			return new ArrayList<>();
		}
		if(value.startsWith("[") && value.endsWith("]")) {
			value = value.substring(1, value.length() - 1);
		}
		return StringSupport.split(value, ",");
	}



	private void processCommentAndEmpty(Line line) {
		String s = line.getLine();
		if(s.trim().startsWith("#") || "".equals(s.trim())) {
			linesGathered.add(line.getLine());
		}
	}

	private static String getKey(Line line) {
		String s = line.getLine();
		if(!s.trim().startsWith("#") && s.contains("=")) {
			return s.substring(0, s.indexOf("=")).trim();
		}
		return null;
	}

	public static void saveProperties(Properties properties, String fileName) throws IOException {
		FileData fileData = new FileData(fileName);
		String tmpFileName = fileData.getPath() + "/." + System.currentTimeMillis() + "." + fileData.getFileName();
		FileOutputStream outputStream = new FileOutputStream(tmpFileName);
		properties.store(outputStream, null);
		outputStream.close();
		FileSupport.renameFile(tmpFileName, fileName, true);
	}

	public void store(OutputStream outputStream, String comments) {
		PrintStream printStream = new PrintStream(outputStream);
		for(String propertyName : stringPropertyNames()) {
			printComment(printStream, linesOfComment.get(propertyName));
			printStream.println(propertyName + "=" + getProperty(propertyName));
		}
		printComment(printStream, linesGathered);
		printStream.close();
	}

	private void printComment(PrintStream printStream, List<String> lines) {
		if(lines != null) {
			for (String line : lines) {
				printStream.println(line);
			}
		}
	}

	public Object remove(Object key) {
		orderedPropertyNames.remove(key);
		return super.remove(key);
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

	public void addSubsection(String sectionKey, Properties subsection) {
		addSubsection(this, sectionKey, subsection);
	}

/*	public String toString() {
		StringBuffer result = new StringBuffer();
		for(String propertyName : this.stringPropertyNames()) {
			result.append(propertyName + "=" + getProperty(propertyName) + "\n");
		}
		return result.toString();
	}*/
	public String toString() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		store(outputStream, null);
		return outputStream.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IgluProperties)) return false;
		if (!super.equals(o)) return false;
		IgluProperties that = (IgluProperties) o;
		boolean opnEquals = Objects.equals(orderedPropertyNames, that.orderedPropertyNames);
		boolean locEquals = Objects.equals(linesOfComment, that.linesOfComment);
		return locEquals && opnEquals;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), orderedPropertyNames, linesOfComment);
	}
}
