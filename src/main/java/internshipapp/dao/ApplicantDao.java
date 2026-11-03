package internshipapp.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import internshipapp.db.DB;
import internshipapp.domain.Applicant;
/**
* All of the SQL for the APPLICANT table.
*
* Every lookup includes the owner, so a photo that belongs to someone else is
* indistinguishable from one that doesn't exist.
*/
public class ApplicantDao {
// The photo with this id in this user's album, or empty if there
// is none.
	public static void main (String[] args) {
		List<Applicant> applicants = new ApplicantDao().findAll();
		System.out.println("TEST: Finding All" + applicants);
		System.out.println("TEST: Specific ID" + new ApplicantDao().findById(1));
	}
	
	// Getting all applicants sorted by LastName Ascending (for GET /applicants/all )
	public List<Applicant> findAll() {
		String sql = "SELECT Applicant_ID, First_Name, Last_Name, Class_Year FROM APPLICANT ORDER BY Last_Name ASC";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			List<Applicant> applicants = new ArrayList<>();
			while (rs.next()) {
				applicants.add(toApplicant(rs));
			}
			return applicants;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
	
	// Getting applicants with a specific Applicant_ID or none at all (for GET /applicants/{id} )
	public Optional<Applicant> findById(int id) {
		String sql = "SELECT Applicant_ID, First_Name, Last_Name, Class_Year FROM APPLICANT WHERE APPLICANT_ID = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(toApplicant(rs));
				}
			return Optional.empty();
			}
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
	
	// Getting Applicants by Class Year (for GET /applicants?classYear=?)
	public List<Applicant> findByClassYear(int classYear){
		String sql = "SELECT Applicant_ID, First_Name, Last_Name, Class_Year FROM APPLICANT WHERE Class_Year = ? ORDER BY FIRST_NAME";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, classYear);
			try (ResultSet rs = stmt.executeQuery()){
				List<Applicant> applicants = new ArrayList<>();
				while (rs.next()) {
					applicants.add(toApplicant(rs));
				}
				return applicants;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw SqlErrors.translate(e);
		}
	}
	
	// The current row of rs as a Applicant. 
	private Applicant toApplicant(ResultSet rs) throws SQLException {
		return new Applicant(
				rs.getInt("Applicant_ID"), 
				rs.getString("First_Name"), 
				rs.getString("Last_Name"), 
				rs.getInt("Class_Year")
			);
	}
}