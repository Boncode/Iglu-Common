/*
 * Copyright 2011-2013 Jeroen Meetsma - IJsberg
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

package org.ijsberg.iglu.util;

import org.ijsberg.iglu.util.properties.IgluProperties;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 */
public class PropertiesSupportTest {

	@Test
	public void testGetSubsection() throws Exception {
	}

	@Test
	public void testGetSubsectionKeys() throws Exception {
		//Properties
	}

	@Test
	public void testGetSubsections() throws Exception {
		//Properties
	}

	@Test
	public void testGetSubsectionsForSectionKey() throws Exception {
		//Properties properties, String sectionkey
	}

	@Test
	public void testGetCommandLineProperties() {

		Properties properties = IgluProperties.getCommandLineProperties("-test", "true");
		assertEquals(1, properties.size());
		assertEquals("true", properties.getProperty("test"));

		properties = IgluProperties.getCommandLineProperties("-test", "true", "-key", "value");
		assertEquals(2, properties.size());
		assertEquals("true", properties.getProperty("test"));
		assertEquals("value", properties.getProperty("key"));

		properties = IgluProperties.getCommandLineProperties("-test", "true", "-key");
		assertEquals(2, properties.size());
		assertEquals("true", properties.getProperty("test"));
		assertEquals("", properties.getProperty("key"));

		properties = IgluProperties.getCommandLineProperties("-test", "-key", "value");
		assertEquals(2, properties.size());
		assertEquals("", properties.getProperty("test"));
		assertEquals("value", properties.getProperty("key"));

		properties = IgluProperties.getCommandLineProperties("-test", "true", "-key", "value", "dummy");
		assertEquals(2, properties.size());
		assertEquals("true", properties.getProperty("test"));
		assertEquals("value", properties.getProperty("key"));

		properties = IgluProperties.getCommandLineProperties("dummy1", "-test", "true", "-key", "value", "dummy2");
		assertEquals(2, properties.size());
		assertEquals("true", properties.getProperty("test"));
		assertEquals("value", properties.getProperty("key"));
	}

	@Test
	public void testLoadProperties() throws Exception {
		IgluProperties properties = IgluProperties.loadProperties("test/IJsberg.Iglu.properties");
		System.out.println(properties.stringPropertyNames());
		List<String> keys = new ArrayList<>(properties.stringPropertyNames());
		assertEquals("java.configuration.menu.contextMenu.extra.submenu.item1.css_class_name", keys.get(keys.size() - 1));
		assertEquals(23, keys.size());
	}

}
