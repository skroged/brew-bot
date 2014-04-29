package com.brew.lib.model;

public enum SOCKET_METHOD {
	DATA_UPDATE("update"), SENSOR_SETTINGS_UPDATE("sensorSettingsUpdate"), CONFIRM_MESSAGE(
			"confirm"), SWITCH_UPDATE("switch"), PING("ping"), SUBSCRIBE(
			"subscribe"), SUBSCRIBE_RESULT("subscribeResult"), UNSUBSCRIBE(
			"unsubscribe"), REGISTER_USER("register"), LOGIN_USER("login"), LOGOUT_USER(
			"logout"), REGISTER_RESULT("registerResult"), LOGIN_RESULT(
			"loginResult"), LOG("log"), UPDATE_USERS("updateUsers"), REQUEST_SERVER_INFO(
			"requestServerInfo"), SEND_SERVER_INFO("sendServerInfo"), REQUEST_ANDROID_APK(
			"requestApk"), SEND_ANDROID_APK("sendApk");

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
