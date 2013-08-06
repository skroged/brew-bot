package com.brew.lib.model;

public class BrewEnvelope {

	private BrewMessage message;

	public void send() {

		String json = GsonHelper.getGson().toJson(message);
		
		

	}

}
