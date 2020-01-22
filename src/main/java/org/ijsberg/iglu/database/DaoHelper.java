package org.ijsberg.iglu.database;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.database.component.StandardConnectionPool;
import org.ijsberg.iglu.util.properties.IgluProperties;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import javax.sql.DataSource;

public abstract class DaoHelper {

    private DaoHelper() {}

    /**
     * Creates component and adds it to cluster exposing interface
     *
     * @param cluster
     * @param connPoolConfigFileName
     * @param connPoolComponentName
     * @param daoComponentName
     * @param daoClass
     * @param expInterface
     * @param xorKey
     * @throws InstantiationException
     */
    public static <T extends JdbcProcessor> T createDaoComponent(
            Cluster cluster,
            String connPoolConfigFileName,
            String connPoolComponentName,
            String daoComponentName,
            Class<T> daoClass,
            Class expInterface,
            String xorKey)
            throws InstantiationException {
        DataSource configConnectionPool = new StandardConnectionPool(xorKey);
        Component configConnectionPoolComponent = new StandardComponent(configConnectionPool);
        configConnectionPoolComponent.setProperties(IgluProperties.loadProperties(connPoolConfigFileName));
        cluster.connect(connPoolComponentName, configConnectionPoolComponent, Startable.class);
        T impl = ReflectionSupport.instantiateClass(daoClass, configConnectionPool);
        Component configDbComponent = new StandardComponent(impl);
        cluster.connect(daoComponentName, configDbComponent, expInterface);
        return impl;
    }

}
