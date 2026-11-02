package internshipapp.dto;

/**
 * The body of POST /login.
 */
public record LoginRequest(String username, String password) {
}
