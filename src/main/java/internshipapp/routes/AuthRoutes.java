package internshipapp.routes;

import java.util.Optional;

import internshipapp.dto.LoginRequest;
import internshipapp.dto.TokenResponse;
import internshipapp.service.SessionService;
import internshipapp.service.UserService;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;

public class AuthRoutes {
	private final UserService userService;
	private final SessionService sessionService;

	public AuthRoutes(UserService userService, SessionService sessionService) {
		this.userService = userService;
		this.sessionService = sessionService;
	}

	public void register(JavalinConfig config) {
		config.routes.post("/login", ctx -> login(ctx));   // public
		config.routes.post("/logout", ctx -> logout(ctx)); // needs a token
	}

	private void login(Context ctx) {
		LoginRequest body = ctx.bodyAsClass(LoginRequest.class);
		String token = userService.login(body.username(), body.password());
		ctx.json(new TokenResponse(token));
	}

	private void logout(Context ctx) {
		Optional<String> token = CurrentUser.bearerToken(ctx);
		if (token.isPresent()) {
			sessionService.end(token.get());
		}
		ctx.status(204);
	}
}