package internshipapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import internshipapp.db.DB;
import internshipapp.domain.User;

public class UserDao {

	// The user with this username, or empty if there is none.
	public Optional<User> findByUsername(String username) {
		String sql = "SELECT username, password, role FROM USER WHERE username = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, username);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(toUser(rs));
				}
				return Optional.empty();
			}
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}

	// Retrieves all users alphabetically (for Admin GET /users).
	public List<User> findAll() {
		String sql = "SELECT username, password, role FROM USER ORDER BY username ASC";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			List<User> users = new ArrayList<>();
			while (rs.next()) {
				users.add(toUser(rs));
			}
			return users;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}

	// Adds a user with their hashed password and role.
	public void insert(String username, String passwordHash, String role) {
		String sql = "INSERT INTO USER (username, password, role) VALUES (?, ?, ?)";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, username);
			stmt.setString(2, passwordHash);
			stmt.setString(3, role);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}

	private User toUser(ResultSet rs) throws SQLException {
		return new User(
				rs.getString("username"),
				rs.getString("password"),
				rs.getString("role")
		);
	}
}