package org.ijsberg.iglu.util.properties;

import org.ijsberg.iglu.util.collection.CollectionSupport;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class IgluPropertiesTest {

    @Test
    public void testSetProperty_check_order() {
        IgluProperties igluProperties = new IgluProperties();
        igluProperties.setProperty("A", "1");
        igluProperties.setProperty("B", "2");
        igluProperties.setProperty("C", "3");

        assertEquals("A,B,C", CollectionSupport.format(igluProperties.getRootKeys(), ","));

        igluProperties.setProperty("B", "5");

        assertEquals("A,B,C", CollectionSupport.format(igluProperties.getRootKeys(), ","));
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
    public void testLoadProperties() {
        IgluProperties properties = IgluProperties.loadProperties("test/IJsberg.Iglu.properties");
        System.out.println(properties.stringPropertyNames());
        List<String> keys = new ArrayList<>(properties.stringPropertyNames());
        assertEquals("java.configuration.menu.contextMenu.extra.submenu.item1.css_class_name", keys.get(keys.size() - 1));
        assertEquals(25, keys.size());
    }

    @Test
    public void testEquals() {
        IgluProperties propertiesA = IgluProperties.loadProperties("test/IJsberg.Iglu.properties");
        IgluProperties propertiesB = IgluProperties.loadProperties("test/IJsberg.Iglu.properties");
        IgluProperties propertiesC = IgluProperties.loadProperties("test/IJsberg.Iglu.properties");

        propertiesC.setProperty("x", "y");

//        System.out.println(propertiesA.getProperty("test"));

        assertTrue(propertiesA.equals(propertiesB));
        assertFalse(propertiesA.equals(propertiesC));

        IgluProperties propertiesD = new IgluProperties(propertiesA);
        assertTrue(propertiesA.equals(propertiesD));
    }

/*


java.redundancyAnalyzers.literalDuplicateAnalyzer.ingoredTokens=[nl.ijsberg.codeanalysis.code.construct.ImportStatement,nl.ijsberg.codeanalysis.code.token.Directive]
java.redundancyAnalyzers.patternDuplicateAnalyzer.ignoredConstructs=nl.ijsberg.codeanalysis.code.construct.ImportStatement,nl.ijsberg.codeanalysis.code.token.Directive

 */


    @Test
    public void getPropertyAsArray() {
        IgluProperties properties = IgluProperties.loadProperties("test/IJsberg.Iglu.properties");
        String[] array = properties.getPropertyAsArray("projectName");
        assertEquals(1, array.length);
        assertEquals("Iglu" , array[0]);

        array = properties.getPropertyAsArray("java.redundancyAnalyzers.literalDuplicateAnalyzer.ingoredTokens");
        assertEquals(2, array.length);
        assertEquals("nl.ijsberg.codeanalysis.code.construct.ImportStatement", array[0]);
        assertEquals("nl.ijsberg.codeanalysis.code.token.Directive", array[1]);

        array = properties.getPropertyAsArray("java.redundancyAnalyzers.patternDuplicateAnalyzer.ignoredConstructs");
        assertEquals(2, array.length);
        assertEquals("nl.ijsberg.codeanalysis.code.construct.ImportStatement", array[0]);
        assertEquals("nl.ijsberg.codeanalysis.code.token.Directive", array[1]);
    }

    @Test
    public void testRemoveSingleProperty() {
        IgluProperties toEdit = new IgluProperties();
        toEdit.setProperty("test.prop.a", "1");
        toEdit.setProperty("test.prop.twenty.seven", "2");
        toEdit.setProperty("test.a", "3");

        IgluProperties toDelete = new IgluProperties();
        toDelete.setProperty("test.prop.twenty.seven", "2");

        for(String prop: toDelete.stringPropertyNames()) {
            toEdit.remove(prop);
        }

        System.out.println(toEdit);
    }
}