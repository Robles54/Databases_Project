package internshipapp.dao;

import java.sql.SQLException;

import internshipapp.errors.AppException;
import internshipapp.errors.ConflictException;
import internshipapp.errors.NotFoundException;
import internshipapp.errors.UnavailableException;
import internshipapp.errors.ValidationException;

/**
 * Turns an SQLException into one of our exceptions.
 *
 * The DAO is the only layer that knows SQL, so it's the one that translates.
 * Decisions use the SQLState and MySQL's vendor code, never the message text,
 * which changes between MySQL versions and languages.
 */
public final class SqlErrors {
	
	private static final int DEADLOCK = 1213;
	private static final int LOCK_WAIT_TIMEOUT = 1205;

	private SqlErrors() {
	}

	// Returns the exception rather than throwing it, so callers write
	// "throw SqlErrors.translate(e);" and the compiler knows the method ends there.
	public static RuntimeException translate(SQLException e) {
		String state = e.getSQLState();
		if (state != null && state.startsWith("08")) {
			return new UnavailableException("Database unavailable", e); // connection problems
		}
		int code = e.getErrorCode();
		if (code == 1062) {
			return new ConflictException("Duplicate value", e); // UNIQUE or primary key
		}
		if (code == 1451) {
			return new ConflictException("Still referenced by other records", e); // ON DELETE RESTRICT
		}
		if (code == 1452) {
			return new NotFoundException("Referenced record does not exist", e); // foreign key on insert/update
		}
		if (code == 1048 || code == 3819) {
			return new ValidationException("Invalid value", e); // NOT NULL, CHECK
		}
		if (isRetryable(e)) {
			return new UnavailableException("The internship server is busy; please try again", e);
		}
		return new AppException("Database error", e); // anything else: 500
	}
	
	// True if MySQL abandoned the transaction because of other transactions
	// running a deadlock again could work
	public static boolean isRetryable(SQLException e) {
		int code = e.getErrorCode();
		return code == DEADLOCK || code == LOCK_WAIT_TIMEOUT;
	}
}
