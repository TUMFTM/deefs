package de.tum.mw.ftm.deefs.log.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;


/**
 * Use this class to open a connection to a sqlite Database
 *
 * @author Michael Wittmann
 */
public class DBConnection {


	/**
	 * Opens a new connection to a sqlite database using JDBC
	 *
	 * @param filepath Path to sqlite database
	 * @return Connection to the sqlite database
	 * @see Connection
	 */
	public static Connection getConnection(String filepath) {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + filepath);
			conn.setAutoCommit(false);
			return conn;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}


}
