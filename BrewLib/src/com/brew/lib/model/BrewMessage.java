package com.brew.lib.model;

public class BrewMessage {

	private SOCKET_METHOD method;

	private ClientIdentifier clientIdentifier;

	private BrewData data;

	private String guaranteeId;

	private String confirmId;

	private SOCKET_CHANNEL channel;

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

}
