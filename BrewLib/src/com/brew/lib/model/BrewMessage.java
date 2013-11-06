package com.brew.lib.model;

import java.util.List;

public class BrewMessage {

	private SOCKET_METHOD method;
	private ClientIdentifier clientIdentifier;
	private BrewData data;
	private String guaranteeId;
	private String confirmId;
	private SOCKET_CHANNEL channel;
	private Boolean success;
	private CHANNEL_PERMISSION channelPermission;
	private LogMessage logMessage;
	private ServerInfo serverInfo;
	private ApkPacket apkPacket;
	private List<Integer> missingPackets;

	public SOCKET_METHOD getMethod() {
		return method;
	}

	public void setMethod(SOCKET_METHOD method) {
		this.method = method;
	}

	public ClientIdentifier getClientIdentifier() {
		return clientIdentifier;
	}

	public void setClientIdentifier(ClientIdentifier clientIdentifier) {
		this.clientIdentifier = clientIdentifier;
	}

	public BrewData getData() {
		return data;
	}

	public void setData(BrewData data) {
		this.data = data;
	}

	public String getGuaranteeId() {
		return guaranteeId;
	}

	public void setGuaranteeId(String guaranteeId) {
		this.guaranteeId = guaranteeId;
	}

	public String getConfirmId() {
		return confirmId;
	}

	public void setConfirmId(String confirmId) {
		this.confirmId = confirmId;
	}

	public SOCKET_CHANNEL getChannel() {
		return channel;
	}

	public void setChannel(SOCKET_CHANNEL channel) {
		this.channel = channel;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public CHANNEL_PERMISSION getChannelPermission() {
		return channelPermission;
	}

	public void setChannelPermission(CHANNEL_PERMISSION channelPermission) {
		this.channelPermission = channelPermission;
	}

	public LogMessage getLogMessage() {
		return logMessage;
	}

	public void setLogMessage(LogMessage logMessage) {
		this.logMessage = logMessage;
	}

	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}

	public ApkPacket getApkPacket() {
		return apkPacket;
	}

	public void setApkPacket(ApkPacket apkPacket) {
		this.apkPacket = apkPacket;
	}

	public void setMissingPackets(List<Integer> missingPackets) {
		this.missingPackets = missingPackets;
	}

	public List<Integer> getMissingPackets() {
		return missingPackets;
	}

}
