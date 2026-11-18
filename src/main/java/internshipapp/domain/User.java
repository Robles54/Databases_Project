package internshipapp.domain;

// One row of the user table
// Contains the password hash, so never send a User to the client — send a UserView instead

public record User(String username, String passwordHash, String role) {
	public boolean isAdmin() {
		return "ADMIN".equalsIgnoreCase(role);
	}
}
