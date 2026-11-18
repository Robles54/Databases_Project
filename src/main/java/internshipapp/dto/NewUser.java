package internshipapp.dto;

// The body of POST /users { "username": "...", "password": "...", "role": "APPLICANT" }

public record NewUser(String username, String password, String role) {

}
