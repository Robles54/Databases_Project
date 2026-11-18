package internshipapp.routes;

import java.util.Optional;

import internshipapp.domain.User;
import internshipapp.errors.UnauthorizedException;
import internshipapp.service.SessionService;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;

public class CurrentUser {
	private static final String KEY = "currentUser";
	private static final String BEARER = "Bearer ";

	private final SessionService sessionService;

	public CurrentUser(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	public void register(JavalinConfig config) {
		config.routes.beforeMatched(ctx -> {
			// If a token is provided (e.g., an Admin calling POST /users), resolve the user
			Optional<String> token = bearerToken(ctx);
			if (token.isPresent()) {
				Optional<User> user = sessionService.userForToken(token.get());
				if (user.isEmpty()) {
					throw new UnauthorizedException("Login required");
				}
				ctx.attribute(KEY, user.get());
				return;
			}

			// If no token is provided, only allow public routes
			if (isPublic(ctx)) {
				return;
			}
			throw new UnauthorizedException("Login required");
		});
	}

	// Gets the current logged-in User object (with username and role)
	public static User getUser(Context ctx) {
		return ctx.attribute(KEY);
	}

	// Gets the current logged-in username string
	public static String get(Context ctx) {
		User user = ctx.attribute(KEY);
		return user != null ? user.username() : null;
	}

	public static Optional<String> bearerToken(Context ctx) {
		String header = ctx.header("Authorization");
		if (header == null || !header.startsWith(BEARER)) {
			return Optional.empty();
		}
		return Optional.of(header.substring(BEARER.length()));
	}

	// Public endpoints that do not require logging in:
	// - POST /users (self-registration as APPLICANT)
	// - POST /login (logging in)
	// - GET /posting and GET /postings/open (browsing internships publicly)
	private static boolean isPublic(Context ctx) {
		String path = ctx.path();
		if (ctx.method().equals(HandlerType.POST)) {
			return path.equals("/users") || path.equals("/login");
		}
		if (ctx.method().equals(HandlerType.GET)) {
			return path.equals("/posting") || path.equals("/postings/open");
		}
		return false;
	}
}