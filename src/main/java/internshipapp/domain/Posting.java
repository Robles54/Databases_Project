package internshipapp.domain;

import java.time.LocalDate;

public record Posting (
		int Posting_ID,
		String Intern_Title,
		String Division,
		String Company,
		String City,
		int Salary,
		LocalDate Start_Date,
		LocalDate End_Date,
		boolean Accepting_Applications,
		int applicationCound
		){

}
