package internshipapp.domain;

public record ApplicantDecision(
	int Applicant_ID,
	String First_Name,
	String Last_Name,
	String Division_Applied,
	String Decision,
	String Details
) {
}