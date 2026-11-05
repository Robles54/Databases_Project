package internshipapp.service;
import java.util.List;
import java.util.Optional;

import internshipapp.dao.ApplicantDao;
import internshipapp.domain.Applicant;
import internshipapp.domain.StudentHired;
import internshipapp.dto.HireRequest;
import internshipapp.dto.NewApplicant;
import internshipapp.errors.NotFoundException;
/**
* The rules for working with photos.
*
* Knows nothing about HTTP: it takes plain values and returns domain objects,
* and reports failures by throwing the application's exceptions.
*/
public class ApplicantService {
	private final ApplicantDao dao;
	public ApplicantService(ApplicantDao dao) {
		this.dao = dao;
	}
	// One of username's photos. NotFoundException if there's no such
	// photo or it belongs to another user.
	
	// This one is permission based, not needed yet for Milestone 3
//	public Applicant getApplicant(int id, String username) {
//		Optional<Applicant> photo = dao.findById(id);
//		if (photo.isEmpty()) {
//			throw new NotFoundException("No photo with id " + id);
//		}
//		return photo.get();
//	}
	
	// Get Applicant from Applicant ID
	public Applicant getApplicantFromID (int id) {
		Optional<Applicant> applicant = dao.findById(id);
		if (applicant.isEmpty()) {
			throw new NotFoundException("No Applicant with id " + id);
		}
		return applicant.get();
	}
	
	// Get all Applicants Sorted by Last Name
	public List<Applicant> getAllApplicants() {
		List<Applicant> applicants = new ApplicantDao().findAll();
		if (applicants.isEmpty()) {
			throw new NotFoundException ("No Applicants");
		}
		return applicants;
	}
	
	// Get All Applicants from Class Year
	public List<Applicant> getAllApplicantsByClassYear(int classYear){
		List<Applicant> applicants = new ApplicantDao().findByClassYear(classYear);
		if (applicants.isEmpty()) {
			throw new NotFoundException ("No Applicant with the Class Year of " + classYear);
		}
		return applicants;
	}
	
	// Create a new Applicant and return the Applicant ID
	public Applicant createApplicant (NewApplicant req) {
		int newId = dao.create(req);
		return new Applicant (newId, req.First_Name(), req.Last_Name(), req.Class_Year());
	}
	
	// Delete an applicant by Applicant ID
	public void deleteApplicant (int id) {
		if (!dao.delete(id)) {
			throw new NotFoundException("No Applicant with ID: " + id);
		}
	}
	
	// Hires an Applicant (updates APPLICANT and inserts into STUDENT_HIRED)
	public StudentHired hireApplicant (int id, HireRequest req) {
		Optional<Integer> empId = dao.hireApplicant(id, req);
		if (empId.isEmpty()) {
			throw new NotFoundException ("No Applicant with ID: "+ id);
		}
		return new StudentHired(
				empId.get(),
				id,
				req.Internship_Company(),
				req.Office_City(),
				req.Office_State(),
				req.Office_Country(),
				req.Education(),
				req.Finance(),
				req.Health(),
				req.Media_Communications(),
				req.Public_Service_Law_NonProfit_NGO(),
				req.Intern_Title(),
				req.Returning_Intern()
			);
	}
	
}