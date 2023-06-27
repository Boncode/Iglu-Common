package org.ijsberg.iglu.assembly;

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;

public class AssemblyExplorer {

    private StringBuffer treeDescription = new StringBuffer();
    private String assemblyName;
    private Assembly rootAssembly;
    
    private Set<Assembly> internalAssemblies = new LinkedHashSet<>();
    private Set<Component> components = new LinkedHashSet<>();

    private TreeMap<String, Component> componentsByName = new TreeMap();
     
    public AssemblyExplorer(String assemblyName, Assembly assembly) {
        this.assemblyName = assemblyName;
        this.rootAssembly = assembly;
        printComponentTree(0, this.rootAssembly, this.assemblyName);
    }

    private void printComponentTree(int depth, Assembly assembly, String assemblyName) {
 /*       if(depth > 7) {
            return;
        }
 */       if(assembly == rootAssembly || !internalAssemblies.contains(assembly)) {
            internalAssemblies.add(assembly);
            treeDescription.append(
                    new String(StringSupport.createCharArray(depth * 2, ' ')) + "Assembly: " + assemblyName + "\n");
            for (String clusterName : assembly.getClusters().keySet()) {
                Cluster cluster = assembly.getClusters().get(clusterName);
                printComponentTree(depth + 1, cluster, clusterName);
            }
        }

    }

    private void printComponentTree(int depth, Cluster cluster, String clusterName) {
/*        if(depth > 7) {
            return;
        }
*/        treeDescription.append(
                new String(StringSupport.createCharArray(depth * 2, ' ')) + "Cluster: " + clusterName + "\n");
        for(String componentName : cluster.getInternalComponents().keySet()) {
            Component component = cluster.getInternalComponents().get(componentName);
            if(!components.contains(component)) {
                components.add(component);
                componentsByName.put(componentName, component);
                if (component.implementsInterface(Assembly.class)) {
                    printComponentTree(depth + 1, component.getProxy(Assembly.class), componentName);
                } else if (component.implementsInterface(Cluster.class)) {
                    printComponentTree(depth + 1, component.getProxy(Cluster.class), componentName);
                } else {
                    printComponent(depth + 1, component, componentName);
                }
            }
        }
    }

    private void printComponent(int depth, Component component, String componentName) {
        treeDescription.append(
                new String(StringSupport.createCharArray(depth * 2, ' ')) + "component: " + componentName +
                        (!component.implementsInterface(Startable.class) ? "" : (component.getProxy(Startable.class).isStarted() ?
                                ": started" : ": NOT started")) + "\n");
    }

    public String getTreeDescription() {
        return treeDescription.toString();
    }

    /**
     * Invokes method on a component without parameters.
     * @param componentName
     * @param methodName
     * @return
     */
    public Object invokeComponent(String componentName, String methodName) {
        Component component = componentsByName.get(componentName);
        if(component == null) {
            return "component not found";
        }
        Object result = null;
        try {
            result = component.invoke(methodName, new Object[0]);
        } catch (InvocationTargetException e) {
            System.out.println(new LogEntry(Level.CRITICAL, "could not invoke method " + methodName + " on component " + componentName));
            return e + ": " + e.getMessage();
        } catch (NoSuchMethodException e) {
            System.out.println(new LogEntry(Level.CRITICAL, "could not invoke method " + methodName + " on component " + componentName));
            return e + ": " + e.getMessage();
        }
        return result;
    }
}
