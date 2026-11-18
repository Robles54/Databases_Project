package internshipapp.service;

import java.util.List;

import internshipapp.dao.PostingDao;
import internshipapp.domain.Posting;
import internshipapp.domain.PublicPosting;
import internshipapp.domain.User;
import internshipapp.dto.NewPosting;
import internshipapp.errors.NotFoundException;
import internshipapp.errors.ValidationException;

public class PostingService {
	private final PostingDao dao;

	public PostingService(PostingDao dao) {
		this.dao = dao;
	}

	// Public / any user: Get all Internship Postings with application counts
	public List<Posting> getAllPostings() {
		List<Posting> postings = dao.findAll();
		if (postings.isEmpty()) {
			throw new NotFoundException("No Internship Postings found");
		}
		return postings;
	}

	// Public / any user: Get open postings from OPEN_POSTINGS_VIEW (hides HR_Contact and closed postings)
	public List<PublicPosting> getOpenPostingsFromView() {
		List<PublicPosting> postings = dao.findAllOpenFromView();
		if (postings.isEmpty()) {
			throw new NotFoundException("No open Internship Postings found");
		}
		return postings;
	}

	// ADMIN only: Get all Internship Postings that have no applications
	public List<Posting> getUnappliedPostings(User caller) {
		UserService.requireAdmin(caller);
		List<Posting> postings = dao.findUnapplied();
		if (postings.isEmpty()) {
			throw new NotFoundException("No unapplied Internship Postings found");
		}
		return postings;
	}

	// ADMIN only: Create a new Internship Posting
	public Posting createPosting(NewPosting req, User caller) {
		UserService.requireAdmin(caller);
		validateNewPosting(req);
		int newId = dao.create(req);
		return new Posting(
				newId,
				req.Intern_Title().trim(),
				req.Division().trim(),
				req.Company().trim(),
				req.City().trim(),
				req.Salary(),
				req.Start_Date(),
				req.End_Date(),
				req.Accepting_Applications(),
				0
		);
	}

	// ADMIN only: Update an existing Internship Posting by ID
	public Posting updatePosting(int id, NewPosting req, User caller) {
		UserService.requireAdmin(caller);
		validateNewPosting(req);
		if (!dao.update(id, req)) {
			throw new NotFoundException("No Internship Posting with id " + id);
		}
		return new Posting(
				id,
				req.Intern_Title().trim(),
				req.Division().trim(),
				req.Company().trim(),
				req.City().trim(),
				req.Salary(),
				req.Start_Date(),
				req.End_Date(),
				req.Accepting_Applications(),
				0
		);
	}

	// ADMIN only: Delete an Internship Posting by ID
	public void deletePosting(int id, User caller) {
		UserService.requireAdmin(caller);
		if (!dao.delete(id)) {
			throw new NotFoundException("No Internship Posting with id " + id);
		}
	}

	// Service-layer input validation matching INTERNSHIP_POSTING column constraints (400 Bad Request)
	private void validateNewPosting(NewPosting req) {
		if (req == null) {
			throw new ValidationException("Request body is required");
		}
		requireNonBlankMax(req.Intern_Title(), "Intern_Title", 50);
		requireNonBlankMax(req.Division(), "Division", 50);
		requireNonBlankMax(req.Company(), "Company", 50);
		requireNonBlankMax(req.City(), "City", 30);
		requireNonBlankMax(req.Technical_Skill_Required(), "Technical_Skill_Required", 100);
		if (req.Country() != null && req.Country().trim().length() > 60) {
			throw new ValidationException("Country can be at most 60 characters");
		}
		if (req.HR_Contact() != null && req.HR_Contact().trim().length() > 30) {
			throw new ValidationException("HR_Contact can be at most 30 characters");
		}
		if (req.Salary() < 0) {
			throw new ValidationException("Salary cannot be negative");
		}
		if (req.Start_Date() == null || req.End_Date() == null) {
			throw new ValidationException("Start_Date and End_Date are required");
		}
		if (req.End_Date().isBefore(req.Start_Date())) {
			throw new ValidationException("End_Date cannot be before Start_Date");
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