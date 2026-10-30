package photoalbum.dto;

/**
 * The body of every error response: { "status": 409, "message": "A book with
 * ISBN … already exists" }
 *
 * The message is written for the client. Never put stack traces, SQL, or table
 * and column names in it; those go in the server log.
 */
public record ErrorResponse(int status, String message) {
}
