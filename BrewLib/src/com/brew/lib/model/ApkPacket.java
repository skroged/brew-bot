package com.brew.lib.model;

public class ApkPacket {

	private String data;
	private int packetNumber;
	private int totalPackets;
	private long totalData;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getPacketNumber() {
		return packetNumber;
	}

	public void setPacketNumber(int packetNumber) {
		this.packetNumber = packetNumber;
	}

	public int getTotalPackets() {
		return totalPackets;
	}

	public void setTotalPackets(int totalPackets) {
		this.totalPackets = totalPackets;
	}

	public long getTotalData() {
		return totalData;
	}

	public void setTotalData(long totalData) {
		this.totalData = totalData;
	}

}
