package photoalbum.errors;

/**
 * The application temporarily can't do it, e.g. the database can't be reached.
 */
public class UnavailableException extends AppException {

	private static final long serialVersionUID = 1L; // exceptions are Serializable; silences the compiler warning

	public UnavailableException(String message) {
		super(message);
	}

	public UnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
