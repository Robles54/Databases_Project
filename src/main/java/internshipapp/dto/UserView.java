package internshipapp.dto;

import internshipapp.domain.User;

// What the API sends back about the user. No Password Hash

public record UserView(String username, String role) {
	public static UserView from (User user ) {
		return new UserView(user.username(), user.role());
	}
}
