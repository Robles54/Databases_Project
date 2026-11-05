package internshipapp.routes;
import static internshipapp.routes.Params.pathId;

import java.util.List;

import internshipapp.domain.Applicant;
import internshipapp.domain.StudentHired;
import internshipapp.dto.HireRequest;
import internshipapp.dto.NewApplicant;
import internshipapp.service.ApplicantService;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
/**
* The HTTP side of the photo endpoints.
*
* Each handler reads the request, calls the service, and writes the response.
* No SQL and no rules here; failures are exceptions that ErrorHandler turns
* into status codes.
*/
public class ApplicantRoutes {
	private final ApplicantService service;
	public ApplicantRoutes(ApplicantService service) {
		this.service = service;
	}
	// Called from inside Javalin.create(config -> { ... })
	public void register(JavalinConfig config) {
		// STATIC STUFF HAS TO BE ADDED FIRST BEFORE DYNAMIC
		
		// Getting all applicants by Last Name
		config.routes.get("/applicants/all", ctx -> getAllApplicants(ctx));
		
		// Getting applicants by Class year (/applicant?classYear={id}
		config.routes.get("/applicant", ctx -> getAllApplicantsByClassYear(ctx));
		
		// Getting applicant by Applicant ID
		config.routes.get("/applicants/{id}", ctx -> getApplicantByID(ctx));
		
		// Creating a new Applicant
		config.routes.post("/applicants", ctx -> createApplicant(ctx));

		// Deleting an applicant by Applicant ID
		config.routes.delete("/applicants/{id}", ctx -> deleteApplicant(ctx));
		
		// Hiring an Applicant (will modify both APPLICANT and STUDENT_HIRED)
		config.routes.post("/applicant/{id}/hire", ctx -> hireApplicant(ctx));
		
	}
	// GET /applicants/{id} -> 200 and the applicant
	
	// gets applicant by ID (/applicants/{id})
	private void getApplicantByID(Context ctx) {
		int id = pathId(ctx);
		//String username = CurrentUser.get(ctx);
		Applicant applicant = service.getApplicantFromID(id);
		ctx.json(applicant);
	}
	
	// gets all applicants by Last Name (/applicants/all)
	private void getAllApplicants (Context ctx) {
		List<Applicant> allApplicants = service.getAllApplicants();
		ctx.json(allApplicants);
	}
	
	// gets all applicants by Class Year (GET /applicant?classYear={id}
	private void getAllApplicantsByClassYear(Context ctx) {
		int id = ctx.queryParamAsClass("classYear", Integer.class).get();
		List<Applicant> allApplicantsSortClassYear = service.getAllApplicantsByClassYear(id);
		ctx.json(allApplicantsSortClassYear);
	}
	
	// Creates a new Applicant POST /applicants -> code 201 and the created Applicant ID
	private void createApplicant(Context ctx) {
		NewApplicant req = ctx.bodyAsClass(NewApplicant.class);
		Applicant created = service.createApplicant(req);
		ctx.status(201).json(created);
	}
	
	// Delete /applicants/{id} -> 204
	private void deleteApplicant(Context ctx) {
		int id = pathId(ctx);
		service.deleteApplicant(id);
		ctx.status(204);
	}
	
	// POST /applicant/{id}/hire -> 201 and created the StudentHired record
	private void hireApplicant(Context ctx) {
		int id = pathId(ctx);
		HireRequest req = ctx.bodyAsClass(HireRequest.class);
		StudentHired hired = service.hireApplicant(id, req);
		ctx.status(201).json(hired);
	}
}