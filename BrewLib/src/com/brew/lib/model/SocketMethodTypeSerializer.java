package com.brew.lib.model;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SocketMethodTypeSerializer implements
		JsonSerializer<SOCKET_METHOD> {

	@Override
	public JsonElement serialize(SOCKET_METHOD arg0, Type arg1,
			JsonSerializationContext arg2) {

		return new JsonPrimitive(arg0.SERVER_STR);

	}

}