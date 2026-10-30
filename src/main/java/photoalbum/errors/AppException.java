package photoalbum.errors;

/**
 * Base class for the application's own failures.
 *
 * Unchecked, so it passes up through DAOs, services, and routes without throws
 * clauses; ErrorHandler is the one place that catches it. Each subclass names a
 * kind of failure, not an HTTP status code.
 *
 * Thrown as a plain AppException, it means an unexpected failure (500).
 */
public class AppException extends RuntimeException {

	private static final long serialVersionUID = 1L; // exceptions are Serializable; silences the compiler warning

	public AppException(String message) {
		super(message);
	}

	public AppException(String message, Throwable cause) {
		super(message, cause);
	}
}
