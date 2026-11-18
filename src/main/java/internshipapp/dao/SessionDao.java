package internshipapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import internshipapp.db.DB;
import internshipapp.domain.User;

/**
 * All of the SQL for the SESSION table.
 */
public class SessionDao {

	// Stores a new session: a token for this user, valid until expiresAt.
	public void insert(String token, String username, LocalDateTime expiresAt) {
		String sql = "INSERT INTO SESSION (token, username, expires_at) VALUES (?, ?, ?)";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, token);
			stmt.setString(2, username);
			stmt.setObject(3, expiresAt);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}

	// The User a token belongs to, if the session exists and hasn't expired.
	public Optional<User> findUserByToken(String token) {
		String sql = "SELECT u.username, u.password, u.role "
				+ "FROM SESSION s "
				+ "JOIN USER u ON s.username = u.username "
				+ "WHERE s.token = ? AND s.expires_at > ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, token);
			stmt.setObject(2, LocalDateTime.now());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(new User(
							rs.getString("username"),
							rs.getString("password"),
							rs.getString("role")
					));
				}
				return Optional.empty();
			}
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}

	// Ends a session. Returns false if there was no such token.
	public boolean delete(String token) {
		String sql = "DELETE FROM SESSION WHERE token = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, token);
			return stmt.executeUpdate() == 1;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
}