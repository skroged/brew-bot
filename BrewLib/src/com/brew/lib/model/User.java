package com.brew.lib.model;

import java.util.List;

public class User {

	private int id;
	private String username;
	private String password;
	private String name;
	private List<UserChannelPermission> permissions;

	public CHANNEL_PERMISSION getPermissionForChannel(SOCKET_CHANNEL channel) {

		if (permissions != null) {

			for (UserChannelPermission permission : permissions) {

				if (permission.getChannel() == channel) {
					return permission.getPermission();
				}
			}
		}

		return CHANNEL_PERMISSION.NONE;
	}

	public void populateNewSettings(User user) {
		username = user.username;
		name = user.name;
		permissions = user.permissions;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<UserChannelPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<UserChannelPermission> permissions) {
		this.permissions = permissions;
	}

}
