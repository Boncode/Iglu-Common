package org.ijsberg.iglu.access;

public class UserGroup {

	private long id;
	private String name;
	private String description;

	public UserGroup() {
	}

	public UserGroup(String name, String description) {
		this.name = name;
		this.description = description;
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
}
