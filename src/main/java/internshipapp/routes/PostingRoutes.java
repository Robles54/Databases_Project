package internshipapp.routes;

import java.util.List;

import internshipapp.domain.Posting;
import internshipapp.dto.NewPosting;
import internshipapp.service.PostingService;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import static internshipapp.routes.Params.pathId;

public class PostingRoutes {
	private final PostingService service;

	public PostingRoutes(PostingService service) {
		this.service = service;
	}

	public void register(JavalinConfig config) {
		// STATIC STUFF HAS TO BE ADDED FIRST BEFORE DYNAMIC
		// Getting unapplied internship postings (Subquery)
		config.routes.get("/postings/unapplied", ctx -> getUnappliedPostings(ctx));
		// Getting all internship postings
		config.routes.get("/posting", ctx -> getAllPostings(ctx));
		// Creating a new internship posting
		config.routes.post("/postings", ctx -> createPosting(ctx));
		// Updating an internship posting by Posting ID
		config.routes.put("/postings/{id}", ctx -> updatePosting(ctx));
		
		// Deleting an internship postings by Posting ID
		config.routes.delete("/postings/{id}", ctx -> deletePosting(ctx));
	}

	// GET /posting -> 200 and the list of postings
	private void getAllPostings(Context ctx) {
		List<Posting> allPostings = service.getAllPostings();
		ctx.json(allPostings);
	}
	
	// GET /postings/unapplied -> 200 and the list of unapplied postings
	private void getUnappliedPostings(Context ctx) {
		List<Posting> unappliedPostings = service.getUnappliedPostings();
		ctx.json(unappliedPostings);
	}
	
	// POST /postings -> 201 and the created posting
	private void createPosting(Context ctx) {
		NewPosting req = ctx.bodyAsClass(NewPosting.class);
		Posting created = service.createPosting(req);
		ctx.status(201).json(created);
	}
	
	// PUT /postings/{id} -> 200 and the updated posting
	private void updatePosting(Context ctx) {
		int id = pathId(ctx);
		NewPosting req = ctx.bodyAsClass(NewPosting.class);
		Posting updated = service.updatePosting(id, req);
		ctx.json(updated);
	}
	
	// DELETE /postings/{id} -> 204 no context
	private void deletePosting(Context ctx) {
		int id = pathId(ctx);
		service.deletePosting(id);
		ctx.status(204);
	}
	
}