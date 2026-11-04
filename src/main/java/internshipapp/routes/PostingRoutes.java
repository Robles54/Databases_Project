package internshipapp.routes;

import java.util.List;

import internshipapp.domain.Posting;
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
		// Getting unapplied internship postings (Subquery)
		config.routes.get("/postings/unapplied", ctx -> getUnappliedPostings(ctx));
		// Getting all internship postings
		config.routes.get("/posting", ctx -> getAllPostings(ctx));
		
		// Creating a new internship posting
		config.routes.post("/postings", ctx -> createPosting(ctx));
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
	
}