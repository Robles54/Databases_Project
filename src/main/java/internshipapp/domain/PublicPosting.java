package internshipapp.domain;

import java.time.LocalDate;

/**
 * Maps to OPEN_POSTINGS_VIEW (hides HR_Contact and Accepting_Applications,
 * and only includes rows where Accepting_Applications = 1).
 */
public record PublicPosting(
		int Posting_ID,
		String Intern_Title,
		String Division,
		String Company,
		String City,
		String Country,
		String Technical_Skill_Required,
		int Salary,
		LocalDate Start_Date,
		LocalDate End_Date
) {
}