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

package org.ijsberg.iglu.access;

import org.ijsberg.iglu.configuration.Cluster;

import java.util.Properties;

/**
 */
public abstract class BasicAgentFactory<T> implements AgentFactory<T> {

	protected String agentId;
	protected Properties properties;
	protected Cluster cluster;

	public BasicAgentFactory(Cluster cluster, String agentId) {
		this(cluster, agentId, new Properties());
	}

	public BasicAgentFactory(Cluster cluster, String agentId, Properties properties) {
		this.cluster = cluster;
		this.agentId = agentId;
		this.properties = properties;
	}

	@Override
	public String getAgentId() {
		return agentId;
	}

	@Override
	public abstract T createAgentImpl();

	@Override
	public Properties getAgentProperties() {
		return properties;
	}

	public Cluster getCluster() {
		return cluster;
	}

}
