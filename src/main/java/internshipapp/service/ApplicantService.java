package internshipapp.service;

import java.util.List;
import java.util.Optional;

import internshipapp.dao.ApplicantDao;
import internshipapp.dao.ApplicantDao.DecisionStatus;
import internshipapp.dao.Transactions;
import internshipapp.domain.Applicant;
import internshipapp.domain.ApplicantApplication;
import internshipapp.domain.ApplicantDecision;
import internshipapp.domain.DivisionStats;
import internshipapp.domain.StudentHired;
import internshipapp.domain.User;
import internshipapp.dto.HireRequest;
import internshipapp.dto.NewApplicant;
import internshipapp.errors.ConflictException;
import internshipapp.errors.NotFoundException;
import internshipapp.errors.ValidationException;

public class ApplicantService {
	private final ApplicantDao dao;

	public ApplicantService(ApplicantDao dao) {
		this.dao = dao;
	}

	// Row-level protected: ADMIN can view any applicant; APPLICANT can only view their own.
	public Applicant getApplicantFromID(int id, User caller) {
		Optional<Applicant> applicant = caller.isAdmin()
				? dao.findById(id)
				: dao.findByIdAndUsername(id, caller.username());
		if (applicant.isEmpty()) {
			throw new NotFoundException("No Applicant with id " + id);
		}
		return applicant.get();
	}

	// Role-based check: ADMIN only
	public List<Applicant> getAllApplicants(User caller) {
		UserService.requireAdmin(caller);
		List<Applicant> applicants = dao.findAll();
		if (applicants.isEmpty()) {
			throw new NotFoundException("No Applicants");
		}
		return applicants;
	}

	// Role-based check: ADMIN only
	public List<Applicant> getAllApplicantsByClassYear(int classYear, User caller) {
		UserService.requireAdmin(caller);
		if (classYear < 2000 || classYear > 2040) {
			throw new ValidationException("Class_Year must be between 2000 and 2040");
		}
		List<Applicant> applicants = dao.findByClassYear(classYear);
		if (applicants.isEmpty()) {
			throw new NotFoundException("No Applicant with the Class Year of " + classYear);
		}
		return applicants;
	}

	// Any logged-in user can create an applicant profile; ownership is tied to caller.username().
	public Applicant createApplicant(NewApplicant req, User caller) {
		validateNewApplicant(req);
		int newId = dao.create(req, caller.username());
		return new Applicant(newId, req.First_Name().trim(), req.Last_Name().trim(), req.Class_Year());
	}

	// Row-level protected: ADMIN can update any applicant; APPLICANT can only update their own.
	public Applicant updateApplicant(int id, NewApplicant req, User caller) {
		validateNewApplicant(req);
		boolean updated = caller.isAdmin()
				? dao.update(id, req)
				: dao.updateByUsername(id, caller.username(), req);
		if (!updated) {
			throw new NotFoundException("No Applicant with id " + id);
		}
		return new Applicant(id, req.First_Name().trim(), req.Last_Name().trim(), req.Class_Year());
	}

	// Row-level protected: ADMIN can delete any applicant; APPLICANT can only delete their own.
	public void deleteApplicant(int id, User caller) {
		boolean deleted = caller.isAdmin()
				? dao.delete(id)
				: dao.deleteByUsername(id, caller.username());
		if (!deleted) {
			throw new NotFoundException("No Applicant with id " + id);
		}
	}

	// Role-based check (ADMIN only) + Locking Read (SELECT ... FOR UPDATE) inside Transactions.run
	public StudentHired hireApplicant(int id, HireRequest req, User caller) {
		UserService.requireAdmin(caller);
		validateHireRequest(req);

		return Transactions.run(conn -> {
			// 1. Locking read on APPLICANT row to prevent concurrent duplicate hires or race conditions
			Optional<DecisionStatus> status = dao.findDecisionStatusForUpdate(conn, id);
			if (status.isEmpty()) {
				throw new NotFoundException("No Applicant with id " + id);
			}
			if (status.get().accepted() || status.get().rejected()) {
				throw new ConflictException("Applicant " + id + " already has a finalized hiring decision");
			}

			// 2. Update APPLICANT and insert into STUDENT_HIRED atomically on the same connection
			dao.markApplicantHired(conn, id);
			int empId = dao.insertStudentHired(conn, id, req);

			return new StudentHired(
					empId,
					id,
					req.Internship_Company().trim(),
					req.Office_City().trim(),
					req.Office_State(),
					req.Office_Country().trim(),
					req.Education(),
					req.Finance(),
					req.Health(),
					req.Media_Communications(),
					req.Public_Service_Law_NonProfit_NGO(),
					req.Intern_Title().trim(),
					req.Returning_Intern()
			);
		});
	}

	// Row-level protected: ADMIN can view any applicant's applications; APPLICANT can only view their own.
	public List<ApplicantApplication> getApplicationsByApplicantId(int id, User caller) {
		List<ApplicantApplication> applications = caller.isAdmin()
				? dao.findApplicationsByApplicantId(id)
				: dao.findApplicationsByApplicantIdAndUsername(id, caller.username());
		if (applications.isEmpty()) {
			throw new NotFoundException("No Applications found for Applicant with id " + id);
		}
		return applications;
	}

	// Role-based check: ADMIN only
	public List<DivisionStats> getHighDemandDivisions(User caller) {
		UserService.requireAdmin(caller);
		List<DivisionStats> stats = dao.findHighDemandDivisions();
		if (stats.isEmpty()) {
			throw new NotFoundException("No division analytics found");
		}
		return stats;
	}

	// Role-based check: ADMIN only
	public List<ApplicantDecision> getAllDecisions(User caller) {
		UserService.requireAdmin(caller);
		List<ApplicantDecision> decisions = dao.findAllDecisions();
		if (decisions.isEmpty()) {
			throw new NotFoundException("No finalized applicant decisions found");
		}
		return decisions;
	}

	// Service-layer input validation for creating/updating an Applicant (400 Bad Request)
	private void validateNewApplicant(NewApplicant req) {
		if (req == null) {
			throw new ValidationException("Request body is required");
		}
		requireNonBlankMax(req.First_Name(), "First_Name", 50);
		requireNonBlankMax(req.Last_Name(), "Last_Name", 50);
		requireNonBlankMax(req.College(), "College", 100);
		requireNonBlankMax(req.Division_Applied(), "Division_Applied", 50);
		requireNonBlankMax(req.Intern_Title_Applied(), "Intern_Title_Applied", 50);
		requireNonBlankMax(req.Email(), "email", 100);
		if (!req.Email().contains("@")) {
			throw new ValidationException("email must be a valid email address");
		}
		requireNonBlankMax(req.City(), "City", 50);
		requireNonBlankMax(req.Zip_Code(), "Zip_Code", 20);
		requireNonBlankMax(req.Street_Address(), "Street_Address", 100);
		if (req.Class_Year() < 2000 || req.Class_Year() > 2040) {
			throw new ValidationException("Class_Year must be between 2000 and 2040");
		}
		if (req.House_Number() <= 0) {
			throw new ValidationException("HouseNumber must be a positive integer");
		}
	}

	// Service-layer input validation for hiring an Applicant (400 Bad Request)
	private void validateHireRequest(HireRequest req) {
		if (req == null) {
			throw new ValidationException("Request body is required");
		}
		requireNonBlankMax(req.Internship_Company(), "Internship_Company", 100);
		requireNonBlankMax(req.Office_City(), "Office_City", 50);
		requireNonBlankMax(req.Office_Country(), "Office_Country", 60);
		requireNonBlankMax(req.Intern_Title(), "Intern_Title", 50);
		if (req.Office_State() != null && req.Office_State().length() > 50) {
			throw new ValidationException("Office_State can be at most 50 characters");
		}
	}

	private void requireNonBlankMax(String value, String fieldName, int maxLength) {
		if (value == null || value.isBlank()) {
			throw new ValidationException(fieldName + " is required and cannot be blank");
		}
		if (value.trim().length() > maxLength) {
			throw new ValidationException(fieldName + " can be at most " + maxLength + " characters");
		}
	}
}