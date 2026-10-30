package photoalbum.routes;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import photoalbum.dto.ErrorResponse;
import photoalbum.errors.ConflictException;
import photoalbum.errors.ForbiddenException;
import photoalbum.errors.NotFoundException;
import photoalbum.errors.UnauthorizedException;
import photoalbum.errors.UnavailableException;
import photoalbum.errors.ValidationException;

/**
 * The only place that maps failures to status codes.
 *
 * Javalin picks the handler registered for the exception's own class, or its
 * nearest superclass, so the catch-all Exception handler only sees what nothing
 * more specific claimed: real bugs.
 *
 * 4xx: the exception's message is written for the client, so send it. 5xx: log
 * the details for us, send a generic message.
 */
public final class ErrorHandler {

	private static final Logger LOG = Logger.getLogger(ErrorHandler.class.getName());

	private ErrorHandler() {
	}

	// Called from inside Javalin.create(config -> { ... })
	public static void register(JavalinConfig config) {
		config.routes.exception(ValidationException.class, (e, ctx) -> send(ctx, 400, e.getMessage()));
		config.routes.exception(UnauthorizedException.class, (e, ctx) -> send(ctx, 401, e.getMessage())); // once login
																											// is added
		config.routes.exception(ForbiddenException.class, (e, ctx) -> send(ctx, 403, e.getMessage())); // once login is
																										// added
		config.routes.exception(NotFoundException.class, (e, ctx) -> send(ctx, 404, e.getMessage()));
		config.routes.exception(ConflictException.class, (e, ctx) -> send(ctx, 409, e.getMessage()));
		config.routes.exception(UnavailableException.class, (e, ctx) -> {
			LOG.log(Level.SEVERE, "Database unavailable", e);
			send(ctx, 503, "Service temporarily unavailable");
		});

		// A body that isn't valid JSON for the DTO (malformed, wrong types, empty).
		// Jackson's own message names our classes, so send a fixed one instead.
		config.routes.exception(JsonProcessingException.class,
				(e, ctx) -> send(ctx, 400, "Request body is not valid JSON for this request"));

		// Javalin's own errors (an unknown path → 404, a wrong Content-Type → 400):
		// keep their status, but use our error shape so every error looks the same.
		config.routes.exception(HttpResponseException.class, (e, ctx) -> send(ctx, e.getStatus(), e.getMessage()));

		// Anything else is a bug: log everything, tell the client nothing internal.
		config.routes.exception(Exception.class, (e, ctx) -> {
			LOG.log(Level.SEVERE, "Unhandled error on " + ctx.method() + " " + ctx.path(), e);
			send(ctx, 500, "Internal server error");
		});
	}

	private static void send(Context ctx, int status, String message) {
		ctx.status(status).json(new ErrorResponse(status, message));
	}
}
