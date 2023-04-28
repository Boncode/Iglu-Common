package org.ijsberg.iglu.assembly;

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.util.LinkedHashSet;
import java.util.Set;

public class AssemblyExplorer {

    private StringBuffer treeDescription = new StringBuffer();
    private String assemblyName;
    private Assembly rootAssembly;
    
    private Set<Assembly> internalAssemblies = new LinkedHashSet<>();
    private Set<Component> components = new LinkedHashSet<>();
     
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

}
