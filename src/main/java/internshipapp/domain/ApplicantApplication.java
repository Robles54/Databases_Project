package internshipapp.domain;

public record ApplicantApplication (
		int Applicant_ID,
		String First_Name,
		String Last_Name,
		int Posting_ID,
		String Intern_Title,
		String Division,
		String Company,
		String City,
		int Salary,
		String Hiring_Manager,
		String Recruiter,
		boolean Returning_Applicant
	) {
}
