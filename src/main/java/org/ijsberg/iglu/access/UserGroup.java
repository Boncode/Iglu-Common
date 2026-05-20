package org.ijsberg.iglu.access;

import org.ijsberg.iglu.event.model.EventType;

import java.util.ArrayList;
import java.util.List;

public class UserGroup {

	private long id;
	private String name;
	private String description;
	//can be used for alerting
	private List<String> eventTypeIds;

	public UserGroup() {
	}

	public UserGroup(String name, String description) {
		this.name = name;
		this.description = description;
		this.eventTypeIds = new ArrayList<>();
	}

	public UserGroup(String name, String description, List<String> eventTypeIds) {
		this.name = name;
		this.description = description;
		this.eventTypeIds = eventTypeIds;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String toString() {
		return "UserGroup{name=" + name +
				", description=" + description +
				"}";
	}

	public List<String> getEventTypeIds() {
		return eventTypeIds;
	}
}
