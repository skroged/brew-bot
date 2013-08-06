package com.brew.lib.model;

public enum SOCKET_METHOD {

	IDENTIFY_CLIENT("identify"), DATA_UPDATE("update"), REQUEST_DUMP("dump"), CONFIRM_MESSAGE(
			"confirm"), SWITCH_UPDATE("switch"), PING("ping");

	String SERVER_STR;

	SOCKET_METHOD(String serverStr) {

		SERVER_STR = serverStr; 
	}

	public static SOCKET_METHOD findServerStr(String serverStr) {
		for (SOCKET_METHOD e : values()) {
			if (e.SERVER_STR.equals(serverStr)) {
				return e;
			}
		}
		return null;
	}

}
