package photoalbum.routes;

import io.javalin.http.Context;
import photoalbum.errors.ValidationException;

/**
 * Request parsing shared by the route classes.
 *
 * Route classes use it with a static import, so the call reads pathId(ctx).
 */
public final class Params {

	private Params() {
	}

	// The {id} path parameter as a number. GET /books/abc → 400, not a 500.
	public static int pathId(Context ctx) {
		try {
			return Integer.parseInt(ctx.pathParam("id"));
		} catch (NumberFormatException e) {
			throw new ValidationException("id must be a whole number");
		}
	}
}
