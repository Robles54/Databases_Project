package photoalbum.errors;

/**
 * Logged in, but not allowed to do this.
 */
public class ForbiddenException extends AppException {

	private static final long serialVersionUID = 1L; // exceptions are Serializable; silences the compiler warning

	public ForbiddenException(String message) {
		super(message);
	}

	public ForbiddenException(String message, Throwable cause) {
		super(message, cause);
	}
}
