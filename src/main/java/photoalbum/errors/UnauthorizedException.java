package photoalbum.errors;

/**
 * Not logged in: no token, or an unknown or expired one.
 */
public class UnauthorizedException extends AppException {

	private static final long serialVersionUID = 1L; // exceptions are Serializable; silences the compiler warning

	public UnauthorizedException(String message) {
		super(message);
	}

	public UnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}
}
