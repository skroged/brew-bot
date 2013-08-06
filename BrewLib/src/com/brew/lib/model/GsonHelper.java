package com.brew.lib.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonHelper {

	public static Gson gson;

	public static Gson getGson() {

		if (gson == null) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(SOCKET_METHOD.class,
					new SocketMethodTypeDeserializer());
			gsonBuilder.registerTypeAdapter(SOCKET_METHOD.class,
					new SocketMethodTypeSerializer());

			gson = gsonBuilder.create();
		}

		return gson;
	}

}
