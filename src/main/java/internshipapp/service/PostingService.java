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
}