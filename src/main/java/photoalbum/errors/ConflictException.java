package photoalbum.errors;

/**
 * Clashes with the current state: a duplicate value, a row still referenced,
 * etc.
 */
public class ConflictException extends AppException {

	private static final long serialVersionUID = 1L; // exceptions are Serializable; silences the compiler warning

	public ConflictException(String message) {
		super(message);
	}

	public ConflictException(String message, Throwable cause) {
		super(message, cause);
	}
}
