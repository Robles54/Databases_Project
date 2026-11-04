package internshipapp.dto;

import java.time.LocalDate;

// What a user may need to send when creating or updating an Internship Posting
public record NewPosting(
		int Posting_ID,
		String Intern_Title,
		String Division,
		String Company,
		String City,
		String Country,
		String Technical_Skill_Required,
		String HR_Contact,
		int Salary,
		LocalDate Start_Date,
		LocalDate End_Date,
		boolean Accepting_Applications
) {
}