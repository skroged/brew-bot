package com.brew.lib.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class SocketMethodTypeDeserializer implements
		JsonDeserializer<SOCKET_METHOD> {

	public SOCKET_METHOD deserialize(JsonElement json, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		String serverStr = json.getAsString();
		return SOCKET_METHOD.findServerStr(serverStr);
	}

}