package internshipapp.routes;

import java.util.List;

import internshipapp.domain.User;
import internshipapp.dto.NewUser;
import internshipapp.dto.UserView;
import internshipapp.service.UserService;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;

public class UserRoutes {
	private final UserService service;

	public UserRoutes(UserService service) {
		this.service = service;
	}

	public void register(JavalinConfig config) {
		config.routes.post("/users", ctx -> createUser(ctx)); // public for APPLICANT, admin-only for ADMIN
		config.routes.get("/users", ctx -> listUsers(ctx));   // admin-only
	}

	private void createUser(Context ctx) {
		NewUser body = ctx.bodyAsClass(NewUser.class);
		User caller = CurrentUser.getUser(ctx); // null if unauthenticated self-registration
		User created = service.register(body, caller);
		ctx.status(201).json(UserView.from(created));
	}

	private void listUsers(Context ctx) {
		User caller = CurrentUser.getUser(ctx);
		List<UserView> views = service.listUsers(caller).stream()
				.map(UserView::from)
				.toList();
		ctx.json(views);
	}
}