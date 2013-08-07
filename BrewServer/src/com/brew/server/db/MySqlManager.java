package com.brew.server.db;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.SENSOR_NAME;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.Sensor;
import com.brew.lib.model.User;
import com.brew.lib.model.UserChannelPermission;
import com.brew.server.Logger;
import com.mysql.jdbc.Connection;

public class MySqlManager {

	private static Connection connection;

	public static void init() {

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		String url = "jdbc:mysql://localhost:3306/BrewData";

		try {

			Logger.log("DB", "connecting MySQL DB at URL: " + url);

			connection = (Connection) DriverManager.getConnection(url, "root",
					"ssydneyy");

			Logger.log("DB", "MySQL Connection: " + connection);

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

	}

	public static User registerUser(User user) {

		String name = user.getName() != null ? "'" + user.getName() + "'"
				: "NULL";

		String sql = "INSERT INTO users (username, password, name) VALUES ('"
				+ user.getUsername() + "', '" + user.getPassword() + "', "
				+ name + ");";

		Logger.log("AUTH", "registering user: " + user.getUsername());

		try {

			Statement statement = connection.createStatement();

			statement.execute(sql);

			Logger.log("AUTH", "success");

			statement.close();

			return loginUser(user);

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

		Logger.log("AUTH", "fail");

		return null;

	}

	public static User loginUser(User user) {

		String sql = "SELECT idusers, username, password FROM users WHERE username = '"
				+ user.getUsername()
				+ "' AND password = '"
				+ user.getPassword() + "';";

		Logger.log("AUTH", "authenticate user: " + user.getUsername());

		try {

			Statement statement = connection.createStatement();

			boolean hasResults = statement.execute(sql);

			if (hasResults) {

				ResultSet results = statement.getResultSet();

				if (results.next()) {

					Logger.log("AUTH", "success");

					int id = results.getInt("idusers");
					List<UserChannelPermission> permissions = getPermissionsForUser(id);

					user.setId(id);
					user.setPermissions(permissions);
					user.setPassword(null);

					statement.close();

					return user;
				}
			}

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

		Logger.log("AUTH", "fail");

		return null;

	}

	public static List<UserChannelPermission> getPermissionsForUser(int userId) {

		String sql = "SELECT channel, permission FROM permissions WHERE userId = "
				+ userId + ";";

		List<UserChannelPermission> permissions = new ArrayList<UserChannelPermission>();

		try {
			Statement statement = connection.createStatement();

			boolean hasResults = statement.execute(sql);

			if (hasResults) {

				ResultSet results = statement.getResultSet();

				while (results.next()) {

					String channelStr = results.getString("channel");
					String permissionStr = results.getString("permission");

					SOCKET_CHANNEL channel = SOCKET_CHANNEL.valueOf(channelStr);
					CHANNEL_PERMISSION permission = CHANNEL_PERMISSION
							.valueOf(permissionStr);

					UserChannelPermission userChannelPermission = new UserChannelPermission();
					userChannelPermission.setChannel(channel);
					userChannelPermission.setPermission(permission);

					permissions.add(userChannelPermission);

				}
			}

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

		return permissions;

	}

	public static Sensor getSensor(SENSOR_NAME sensorName) {

		try {
			String sql = "SELECT address FROM sensors WHERE name = '"
					+ sensorName + "';";

			Statement statement = connection.createStatement();

			boolean hasResults = statement.execute(sql);

			if (hasResults) {

				ResultSet results = statement.getResultSet();

				if (results.next()) {

					String address = results.getString("address");
					
					Sensor sensor = new Sensor();
					sensor.setSensorName(sensorName);
					sensor.setAddress(address);
					
					return sensor;
				}
			}

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

		return null;
	}

}
