package photoalbum.dao;

import java.sql.SQLException;

import photoalbum.errors.AppException;
import photoalbum.errors.ConflictException;
import photoalbum.errors.NotFoundException;
import photoalbum.errors.UnavailableException;
import photoalbum.errors.ValidationException;

/**
 * Turns an SQLException into one of our exceptions.
 *
 * The DAO is the only layer that knows SQL, so it's the one that translates.
 * Decisions use the SQLState and MySQL's vendor code, never the message text,
 * which changes between MySQL versions and languages.
 */
public final class SqlErrors {

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
		return new AppException("Database error", e); // anything else: 500
	}
}
