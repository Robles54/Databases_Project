package internshipapp.dto;

/**
 * The body of a successful POST /login: { "token": "..." }.
 */
public record TokenResponse(String token) {
}
