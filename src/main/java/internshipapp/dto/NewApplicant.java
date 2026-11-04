package internshipapp.dto;

// What a user may need to send when creating or updating an Applicant
public record NewApplicant (int Applicant_ID, String First_Name, String Last_Name, String Middle_Name, 
		String College, int Class_Year, String Division_Applied, String Intern_Title_Applied, String Email,
		String City, String State, String Zip_Code, int House_Number, String Street_Address, boolean International_Applicant,
		String Country_If_Not_USA, String Interview_Avalibility_Date, boolean Interview_Scheduled, boolean Accepted,
		boolean Pending, boolean Rejected){

}
