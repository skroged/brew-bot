package com.brew.server.db;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.brew.lib.model.User;
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

			System.out.println("connecting MySQL DB at URL: " + url);

			connection = (Connection) DriverManager.getConnection(url, "root",
					"ssydneyy");

			System.out.println("MySQL Connection: " + connection);

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static boolean registerUser(User user) {

		String name = user.getName() != null ? "'" + user.getName() + "'"
				: "NULL";

		String sql = "INSERT INTO users (username, password, name) VALUES ('"
				+ user.getUsername() + "', '" + user.getPassword() + "', "
				+ name + ");";
		
		System.out.println("registering user: " + user.getUsername());

		try {

			Statement statement = connection.createStatement();

			statement.execute(sql);

			System.out.println("success");
			
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;

	}

}
