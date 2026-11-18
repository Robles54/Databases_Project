package internshipapp.routes;

import static internshipapp.routes.Params.pathId;

import java.util.List;

import internshipapp.domain.Posting;
import internshipapp.domain.PublicPosting;
import internshipapp.domain.User;
import internshipapp.dto.NewPosting;
import internshipapp.service.PostingService;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;

public class PostingRoutes {
	private final PostingService service;

	public PostingRoutes(PostingService service) {
		this.service = service;
	}

	public void register(JavalinConfig config) {
		// STATIC STUFF HAS TO BE ADDED FIRST BEFORE DYNAMIC
		// Getting open internship postings from OPEN_POSTINGS_VIEW (Public View)
		config.routes.get("/postings/open", ctx -> getOpenPostings(ctx));
		// Getting unapplied internship postings (Subquery - Admin only)
		config.routes.get("/postings/unapplied", ctx -> getUnappliedPostings(ctx));
		// Getting all internship postings (Public)
		config.routes.get("/posting", ctx -> getAllPostings(ctx));

		// Creating a new internship posting (Admin only)
		config.routes.post("/postings", ctx -> createPosting(ctx));
		// Updating an internship posting by Posting ID (Admin only)
		config.routes.put("/postings/{id}", ctx -> updatePosting(ctx));
		// Deleting an internship posting by Posting ID (Admin only)
		config.routes.delete("/postings/{id}", ctx -> deletePosting(ctx));
	}

	private void getAllPostings(Context ctx) {
		List<Posting> allPostings = service.getAllPostings();
		ctx.json(allPostings);
	}

	private void getOpenPostings(Context ctx) {
		List<PublicPosting> openPostings = service.getOpenPostingsFromView();
		ctx.json(openPostings);
	}

	private void getUnappliedPostings(Context ctx) {
		User caller = CurrentUser.getUser(ctx);
		List<Posting> unappliedPostings = service.getUnappliedPostings(caller);
		ctx.json(unappliedPostings);
	}

	private void createPosting(Context ctx) {
		User caller = CurrentUser.getUser(ctx);
		NewPosting req = ctx.bodyAsClass(NewPosting.class);
		Posting created = service.createPosting(req, caller);
		ctx.status(201).json(created);
	}

	private void updatePosting(Context ctx) {
		int id = pathId(ctx);
		User caller = CurrentUser.getUser(ctx);
		NewPosting req = ctx.bodyAsClass(NewPosting.class);
		Posting updated = service.updatePosting(id, req, caller);
		ctx.json(updated);
	}

	private void deletePosting(Context ctx) {
		int id = pathId(ctx);
		User caller = CurrentUser.getUser(ctx);
		service.deletePosting(id, caller);
		ctx.status(204);
	}
}