package internshipapp.dao;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;
import internshipapp.db.DB;
/**
* Runs several DAO calls as one transaction: all of them happen, or none do.
*
* Usage, from a service:
*
* <pre>
* Loan loan = Transactions.run(conn -> {
* ... DAO calls that take conn ...
* return result;
* });
* </pre>
*
* Every DAO call inside must use the Connection it is given, so they all share
* one transaction. If the work throws anything, the transaction is rolled back
* and the exception goes on to the caller. If a statement fails because of a
* deadlock or a lock wait timeout, the whole transaction is rolled back and run
* again from the start, a few times, before giving up.
*/

public final class Transactions {
private static final int MAX_ATTEMPTS = 3;
private static final Random RANDOM = new Random();

// The work of one transaction. It may throw SQLException: run() rolls back
// and translates it, so the DAO methods it calls don't translate their own.

@FunctionalInterface
public interface Work<T> {
	T run(Connection conn) throws SQLException;
}

private Transactions() {

}

public static <T> T run(Work<T> work) {
	int attempt = 1;
	while (true) {
		try (Connection conn = DB.getConnection()) {
			conn.setAutoCommit(false); // start a transaction: nothing is permanent
			// until commit
			try {
				T result = work.run(conn);
				conn.commit();
				return result;
			} catch (SQLException | RuntimeException e) {
				conn.rollback(); // undo everything this attempt did
				throw e;
			}

			// No need to turn auto-commit back on: the connection is closed here.
			// With a connection pool, it would go back to the pool, so you would.
		} catch (SQLException e) {
			if (SqlErrors.isRetryable(e) && attempt < MAX_ATTEMPTS) {
				// The whole transaction was rolled back (by MySQL for a deadlock;
				// by the rollback above for a lock wait timeout, where MySQL only
				// undoes the one statement). Nothing it did was kept, so running
				// it again from the start is safe.
				pause(attempt);
				attempt = attempt + 1;
			} else {
				throw SqlErrors.translate(e);
			}
		}
	}
}

// Wait a random time, longer after each failed attempt, so that
// transactions which collided don't all start again at the same moment.
private static void pause(int attempt) {
	try {
		Thread.sleep(50 * attempt + RANDOM.nextInt(50 * attempt));
	} catch (InterruptedException ie) {
		Thread.currentThread().interrupt();
	}
	}
}