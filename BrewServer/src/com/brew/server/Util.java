package com.brew.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.brew.lib.model.ApkPacket;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.ServerInfo;
import com.brew.lib.util.Base64;
import com.brew.server.socket.SocketConnection;

public class Util {

	public static ServerInfo getServerInfo() {

		ServerInfo info = new ServerInfo();

		long buildDate = 0;
		String appFile = "BrewDroid.apk";
		try {
			buildDate = new File(appFile).lastModified();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.log("ERROR", e.getMessage());
		}

		info.setAndroidBuildDate(buildDate);

		return info;

	}

	public static void sendApkFile(final SocketConnection socket,
			int bufferSize, List<Integer> missingPackets) {

		File file = new File("BrewDroid.apk");
		final long fileSize = file.length();

		FileInputStream fileInputStream = null;
		final byte[] fileBytes = new byte[(int) file.length()];
		try {
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(fileBytes);

			final String base64 = Base64.encodeToString(fileBytes,
					Base64.DEFAULT);

			Logger.log("FILE", "base64 length: " + base64.length());

			// account for json padding

			final int packetLen = bufferSize - 100;
			final int packetCount = base64.length() / packetLen
					+ ((base64.length() % packetLen > 0) ? 1 : 0);

			Logger.log("FILE", "sending apk file. size: " + ((float) fileSize)
					/ 1000f + "KB");

			// class def inside a method... bitches!!!!
			class Sender {

				void send(int i) {
					int start = packetLen * i;

					int end = start + packetLen;

					if (end > base64.length()) {
						end = base64.length();
					}

					String subStr = base64.substring(start, end);

					BrewMessage message = new BrewMessage();
					message.setMethod(SOCKET_METHOD.SEND_ANDROID_APK);

					ApkPacket packet = new ApkPacket();
					packet.setData(subStr);
					message.setApkPacket(packet);
					packet.setPacketNumber(i);
					packet.setTotalData(fileSize);
					packet.setTotalPackets(packetCount);
					socket.sendMessage(message);

				}

			}

			Sender sender = new Sender();

			if (missingPackets != null) {

				for (Integer i : missingPackets) {
					sender.send(i);
				}
			}

			for (int i = 0; i < packetCount; i++) {
				sender.send(i);
			}

			Logger.log("FILE", "done sending apk file");

		} catch (FileNotFoundException e) {
			Logger.log("ERROR", e.getMessage());
		} catch (IOException e1) {
			Logger.log("ERROR", e1.getMessage());
		}

		if (fileInputStream != null) {
			try {
				fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
