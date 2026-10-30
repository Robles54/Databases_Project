package photoalbum.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The one place that knows how to connect to MySQL .
 *
 * Credentials come from environment variables, never from source code: DB_URL,
 * DB_USER, DB_PASSWORD
 */
public final class DB {

	private static final String URL = System.getenv("DB_URL");
	private static final String USER = System.getenv("DB_USER");
	private static final String PASSWORD = System.getenv("DB_PASSWORD");

	private DB() {
	}

	public static Connection getConnection() throws SQLException {
		if (URL == null || USER == null || PASSWORD == null) {
			throw new SQLException("Set DB_URL, DB_USER, and DB_PASSWORD in the environment");
		}
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}
}
