package internshipapp.dto;

// What a user sends in the JSON body when hiring an applicant
public record HireRequest(
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