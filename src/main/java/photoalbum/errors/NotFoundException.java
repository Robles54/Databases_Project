package photoalbum.errors;

/**
 * No such thing (or not yours to see).
 */
public class NotFoundException extends AppException {

	private static final long serialVersionUID = 1L; // exceptions are Serializable; silences the compiler warning

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
