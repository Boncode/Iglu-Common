package org.ijsberg.iglu.access;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.util.Properties;

public class ReflectionAgentFactory<T> extends BasicAgentFactory<T> {

    private Class<? extends T> implClass;

    public ReflectionAgentFactory(Cluster cluster, String agentId, Class<? extends T> implClass, Properties properties) {
        super(cluster, agentId, properties);
        this.implClass = implClass;
    }

    @Override
    public T createAgentImpl() {
        try {
            return ReflectionSupport.instantiateClass(implClass, properties);
        } catch (InstantiationException e) {
            throw new ConfigurationException("Agent " + agentId + " (" + implClass + ") cannot be instantiated", e);
        }
    }

}
