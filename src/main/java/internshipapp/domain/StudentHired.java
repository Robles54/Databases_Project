package internshipapp.domain;

public record StudentHired(
		int Emp_ID,
		int Applicant_ID,
		String Internship_Company,
		String Office_City,
		String Office_State,
		String Office_Country,
		boolean Education,
		boolean Finance,
		boolean Health,
		boolean Media_Communications,
		boolean Public_Service_Law_NonProfit_NGO,
		String Intern_Title,
		boolean Returning_Intern
) {
}