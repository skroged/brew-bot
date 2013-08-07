package com.brew.lib.model;

public class UserChannelPermission {

	private int userId;
	private SOCKET_CHANNEL channel;
	private CHANNEL_PERMISSION permission;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public SOCKET_CHANNEL getChannel() {
		return channel;
	}

	public void setChannel(SOCKET_CHANNEL channel) {
		this.channel = channel;
	}

	public CHANNEL_PERMISSION getPermission() {
		return permission;
	}

	public void setPermission(CHANNEL_PERMISSION permission) {
		this.permission = permission;
	}

}
