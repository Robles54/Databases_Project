package photoalbum.errors;

/**
 * Bad input: missing or invalid values in the request.
 */
public class ValidationException extends AppException {

	private static final long serialVersionUID = 1L; // exceptions are Serializable; silences the compiler warning

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
