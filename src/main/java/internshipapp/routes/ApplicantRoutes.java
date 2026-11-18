package internshipapp.routes;

import static internshipapp.routes.Params.pathId;

import java.util.List;

import internshipapp.domain.Applicant;
import internshipapp.domain.ApplicantApplication;
import internshipapp.domain.ApplicantDecision;
import internshipapp.domain.DivisionStats;
import internshipapp.domain.StudentHired;
import internshipapp.domain.User;
import internshipapp.dto.HireRequest;
import internshipapp.dto.NewApplicant;
import internshipapp.service.ApplicantService;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;

public class ApplicantRoutes {
	private final ApplicantService service;

	public ApplicantRoutes(ApplicantService service) {
		this.service = service;
	}

	public void register(JavalinConfig config) {
		// STATIC STUFF HAS TO BE ADDED FIRST BEFORE DYNAMIC
		config.routes.get("/applicants/all", ctx -> getAllApplicants(ctx));
		config.routes.get("/applicants/decisions", ctx -> getAllDecisions(ctx));
		config.routes.get("/analytics/divisions", ctx -> getDivisionAnalytics(ctx));
		config.routes.get("/applicants-classYear={id}", ctx -> getAllApplicantsByClassYear(ctx));

		// DYNAMIC ROUTES BELOW
		config.routes.get("/applicants/{id}", ctx -> getApplicantByID(ctx));
		config.routes.get("/applicants/{id}/applications", ctx -> getApplicationsByApplicantId(ctx));
		config.routes.post("/applicants", ctx -> createApplicant(ctx));
		config.routes.post("/applicants/{id}/hire", ctx -> hireApplicant(ctx));
		config.routes.put("/applicants/{id}", ctx -> updateApplicant(ctx));
		config.routes.delete("/applicants/{id}", ctx -> deleteApplicant(ctx));
	}

	private void getApplicantByID(Context ctx) {
		int id = pathId(ctx);
		User caller = CurrentUser.getUser(ctx);
		Applicant applicant = service.getApplicantFromID(id, caller);
		ctx.json(applicant);
	}

	private void getAllApplicants(Context ctx) {
		User caller = CurrentUser.getUser(ctx);
		List<Applicant> allApplicants = service.getAllApplicants(caller);
		ctx.json(allApplicants);
	}

	private void getAllApplicantsByClassYear(Context ctx) {
		int id = pathId(ctx);
		User caller = CurrentUser.getUser(ctx);
		List<Applicant> allApplicantsSortClassYear = service.getAllApplicantsByClassYear(id, caller);
		ctx.json(allApplicantsSortClassYear);
	}

	private void createApplicant(Context ctx) {
		User caller = CurrentUser.getUser(ctx);
		NewApplicant req = ctx.bodyAsClass(NewApplicant.class);
		Applicant created = service.createApplicant(req, caller);
		ctx.status(201).json(created);
	}

	private void updateApplicant(Context ctx) {
		int id = pathId(ctx);
		User caller = CurrentUser.getUser(ctx);
		NewApplicant req = ctx.bodyAsClass(NewApplicant.class);
		Applicant updated = service.updateApplicant(id, req, caller);
		ctx.json(updated);
	}

	private void deleteApplicant(Context ctx) {
		int id = pathId(ctx);
		User caller = CurrentUser.getUser(ctx);
		service.deleteApplicant(id, caller);
		ctx.status(204);
	}

	private void hireApplicant(Context ctx) {
		int id = pathId(ctx);
		User caller = CurrentUser.getUser(ctx);
		HireRequest req = ctx.bodyAsClass(HireRequest.class);
		StudentHired hired = service.hireApplicant(id, req, caller);
		ctx.status(201).json(hired);
	}

	private void getApplicationsByApplicantId(Context ctx) {
		int id = pathId(ctx);
		User caller = CurrentUser.getUser(ctx);
		List<ApplicantApplication> applications = service.getApplicationsByApplicantId(id, caller);
		ctx.json(applications);
	}

	private void getDivisionAnalytics(Context ctx) {
		User caller = CurrentUser.getUser(ctx);
		List<DivisionStats> stats = service.getHighDemandDivisions(caller);
		ctx.json(stats);
	}

	private void getAllDecisions(Context ctx) {
		User caller = CurrentUser.getUser(ctx);
		List<ApplicantDecision> decisions = service.getAllDecisions(caller);
		ctx.json(decisions);
	}
}