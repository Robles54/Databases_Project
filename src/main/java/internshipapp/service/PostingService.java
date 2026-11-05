package internshipapp.service;

import java.util.List;

import internshipapp.dao.PostingDao;
import internshipapp.domain.Posting;
import internshipapp.dto.NewPosting;
import internshipapp.errors.NotFoundException;

public class PostingService {
	private final PostingDao dao;

	public PostingService(PostingDao dao) {
		this.dao = dao;
	}

	// Get all Internship Postings
	public List<Posting> getAllPostings() {
		List<Posting> postings = dao.findAll();
		if (postings.isEmpty()) {
			throw new NotFoundException("No Internship Postings found");
		}
		return postings;
	}
	
	// Get all Internship Postings that have no applications
	public List<Posting> getUnappliedPostings() {
		List<Posting> postings = dao.findUnapplied();
		if (postings.isEmpty()) {
			throw new NotFoundException("No unapplied Internship Postings found");
		}
		return postings;
	}
	
	// Create a new Internship Posting and return the created domain object
	public Posting createPosting(NewPosting req) {
		int newId = dao.create(req);
		return new Posting(
			newId,
			req.Intern_Title(),
			req.Division(),
			req.Company(),
			req.City(),
			req.Salary(),
			req.Start_Date(),
			req.End_Date(),
			req.Accepting_Applications(),
			0
		);
	}
	
	// Update an existing Internship Posting by ID. NotFoundException if there's no such posting
	public Posting updatePosting(int id, NewPosting req) {
		if (!dao.update(id, req)) {
			throw new NotFoundException("No Internship Posting with id " + id);
		}
		return new Posting(
				id,
				req.Intern_Title(),
				req.Division(),
				req.Company(),
				req.City(),
				req.Salary(),
				req.Start_Date(),
				req.End_Date(),
				req.Accepting_Applications(),
				0
			);
	}
	
	// Delete an internship posting by ID. NotFoundException if there's no such posting
	public void deletePosting(int id) {
		if (!dao.delete(id)) {
			throw new NotFoundException ("No internship posting with ID: " + id);
		}
	}
	
}