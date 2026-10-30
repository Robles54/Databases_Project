package photoalbum.routes;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import photoalbum.errors.UnauthorizedException;
/**
* Works out whose album a request is for.
*
* For now the client simply names the user in an X-User header. Once login is
* added, only register() changes (to read a token instead); routes keep calling
* CurrentUser.get(ctx).
*/
public final class CurrentUser {
	private static final String KEY = "currentUser";
	private CurrentUser() {
}
// Set up a before handler to extract the current user from the
// request.
// Called from inside Javalin.create(config -> { ... })
public static void register(JavalinConfig config) {
	// beforeMatched runs before every request that matches a route
	// (an unknown path still gets a 404, not a 401)
	config.routes.beforeMatched(ctx -> {
		String username = ctx.header("X-User");
		if (username == null || username.isBlank()) {
			throw new UnauthorizedException("X-User header is required");
		}
		ctx.attribute(KEY, username.trim());
	});
}
// The current user's username, as stored by the before handler.
public static String get(Context ctx) {
	return ctx.attribute(KEY);
	}
}