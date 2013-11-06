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
import com.brew.lib.model.SWITCH_NAME;
import com.brew.lib.model.Sensor;
import com.brew.lib.model.SensorCalibration;
import com.brew.lib.model.Switch;
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

	public static List<User> getUsers() {

		List<User> returnList = new ArrayList<User>();

		String sql = "SELECT idusers, username, name FROM users";

		try {

			Statement statement = connection.createStatement();

			boolean hasResults = statement.execute(sql);

			if (hasResults) {

				ResultSet results = statement.getResultSet();

				while (results.next()) {

					User user = new User();

					Logger.log("AUTH", "success");

					int id = results.getInt("idusers");
					String username = results.getString("username");
					String name = results.getString("name");
					List<UserChannelPermission> permissions = getPermissionsForUser(id);

					user.setId(id);
					user.setUsername(username);
					user.setPermissions(permissions);
					user.setName(name);

					returnList.add(user);
				}

				statement.close();
				results.close();
			}

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

		return returnList;
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

	public static void updateUser(User user) {

		String sql = "UPDATE users SET name = '' WHERE idusers = "
				+ user.getId();

		Logger.log("DATA", "updating user: " + user.getUsername());

		try {

			Statement statement = connection.createStatement();

			statement.execute(sql);

			Logger.log("DATA", "success");

			statement.close();

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

		List<UserChannelPermission> perms = user.getPermissions();
		for (UserChannelPermission perm : perms) {
			setPermissionForUser(user.getId(), perm.getChannel(),
					perm.getPermission());
		}
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

		String sql = "SELECT idpermissions, channel, permission FROM permissions WHERE userId = "
				+ userId + ";";

		List<UserChannelPermission> permissions = new ArrayList<UserChannelPermission>();

		try {
			Statement statement = connection.createStatement();

			boolean hasResults = statement.execute(sql);

			if (hasResults) {

				ResultSet results = statement.getResultSet();

				while (results.next()) {

					int id = results.getInt("idpermissions");
					String channelStr = results.getString("channel");
					String permissionStr = results.getString("permission");

					SOCKET_CHANNEL channel = SOCKET_CHANNEL.valueOf(channelStr);
					CHANNEL_PERMISSION permission = CHANNEL_PERMISSION
							.valueOf(permissionStr);

					UserChannelPermission userChannelPermission = new UserChannelPermission();
					userChannelPermission.setChannel(channel);
					userChannelPermission.setPermission(permission);
					userChannelPermission.setId(id);

					permissions.add(userChannelPermission);

				}
			}

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

		return permissions;

	}

	public static void setPermissionForUser(int userId, SOCKET_CHANNEL channel,
			CHANNEL_PERMISSION permission) {

		List<UserChannelPermission> permissions = getPermissionsForUser(userId);

		String sql;

		boolean exists = false;
		int existingId = 0;
		for (UserChannelPermission ucp : permissions) {
			if (ucp.getChannel() == channel) {
				exists = true;
				existingId = ucp.getId();
				break;
			}
		}

		if (exists) {
			sql = "UPDATE permissions SET permission = '" + permission
					+ "' WHERE idpermissions = '" + existingId + "';";
		} else {
			sql = "INSERT INTO permissions (userId, channel, permission) VALUES ("
					+ userId + ", '" + channel + "', '" + permission + "');";
		}

		try {
			Statement statement = connection.createStatement();

			statement.execute(sql);

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

	}

	public static Switch getSwitch(SWITCH_NAME switchName) {

		try {
			String sql = "SELECT idswitches, address FROM switches WHERE name = '"
					+ switchName + "';";

			Statement statement = connection.createStatement();

			boolean hasResults = statement.execute(sql);

			if (hasResults) {

				ResultSet results = statement.getResultSet();

				if (results.next()) {

					int address = results.getInt("address");
					int id = results.getInt("idswitches");

					Switch switchh = new Switch();
					switchh.setName(switchName);
					switchh.setAddress(address);
					switchh.setId(id);

					return switchh;
				}
			}

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

		return null;
	}

	public static Sensor getSensor(SENSOR_NAME sensorName) {

		try {
			String sql = "SELECT idsensors, address FROM sensors WHERE name = '"
					+ sensorName + "';";

			Statement statement = connection.createStatement();

			boolean hasResults = statement.execute(sql);

			if (hasResults) {

				ResultSet results = statement.getResultSet();

				if (results.next()) {

					String address = results.getString("address");
					int id = results.getInt("idsensors");
					SensorCalibration calibration = getCalibrationForSensor(id);

					Sensor sensor = new Sensor();
					sensor.setSensorName(sensorName);
					sensor.setAddress(address);
					sensor.setCalibration(calibration);
					sensor.setSensorId(id);

					return sensor;
				}
			}

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

		return null;
	}

	public static void saveSensor(Sensor sensor) {

		Sensor savedSensor = getSensor(sensor.getSensorName());

		try {

			String sql = "UPDATE sensors SET address = '" + sensor.getAddress()
					+ "' WHERE idsensors = " + savedSensor.getSensorId() + ";";

			Statement statement = connection.createStatement();

			statement.execute(sql);

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

		saveSensorCalibration(savedSensor.getSensorId(),
				sensor.getCalibration());
	}

	private static void saveSensorCalibration(int sensorId,
			SensorCalibration calibration) {

		try {

			String sql = "UPDATE sensorCalibration SET inputLow = "
					+ calibration.getInputLow() + ", outputLow = "
					+ calibration.getOutputLow() + ", inputHigh = "
					+ calibration.getInputHigh() + ", outputHigh = "
					+ calibration.getOutputHigh() + " WHERE sensorId = "
					+ sensorId + ";";

			Statement statement = connection.createStatement();

			statement.execute(sql);

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

	}

	public static SensorCalibration getCalibrationForSensor(int sensorId) {

		try {
			String sql = "SELECT inputLow, inputHigh, outputLow, outputHigh FROM sensorCalibration WHERE sensorId = "
					+ sensorId + ";";

			Statement statement = connection.createStatement();

			boolean hasResults = statement.execute(sql);

			if (hasResults) {

				ResultSet results = statement.getResultSet();

				if (results.next()) {

					float inputLow = results.getFloat("inputLow");
					float inputHigh = results.getFloat("inputHigh");
					float outputLow = results.getFloat("outputLow");
					float outputHigh = results.getFloat("outputHigh");

					SensorCalibration calibration = new SensorCalibration();
					calibration.setInputLow(inputLow);
					calibration.setInputHigh(inputHigh);
					calibration.setOutputLow(outputLow);
					calibration.setOutputHigh(outputHigh);

					return calibration;
				}
			}

		} catch (SQLException e) {
			Logger.log("ERROR", e.getMessage());
		}

		return null;

	}

}
