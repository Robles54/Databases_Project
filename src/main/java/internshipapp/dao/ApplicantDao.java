package internshipapp.dao;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.Statement;

import internshipapp.db.DB;
import internshipapp.domain.Applicant;
import internshipapp.dto.NewApplicant;
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
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ResultSet rs = stmt.executeQuery()) {
			List<Applicant> applicants = new ArrayList<>();
			while (rs.next()) {
				applicants.add(toApplicant(rs));
			}
			return applicants;
		} catch (SQLException e) {
			e.printStackTrace();
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
	
	// Inserts a new Applicant and returns the generated Applicant ID (for POST /applicants+)
	public int create (NewApplicant req) {
		String sql = "INSERT INTO APPLICANT (First_Name, Last_Name, Middle_Name, "
				+ "College, Class_Year, Division_Applied, Intern_Title_Applied, Email, "
				+ "City, State, Zip_Code, House_Number, Street_Address, International_Applicant, "
				+ "Country_If_Non_USA, Interview_Avalibility_Date, Interview_Scheduled, "
				+ "Accepted, Pending, Rejected) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, req.First_Name());
			stmt.setString(2, req.Last_Name());
			stmt.setString(3, req.Middle_Name());
			stmt.setString(4, req.College());
			stmt.setInt(5, req.Class_Year());
			stmt.setString(6, req.Division_Applied());
			stmt.setString(7, req.Intern_Title_Applied());
			stmt.setString(8, req.Email());
			stmt.setString(9, req.City());
			stmt.setString(10, req.State());
			stmt.setString(11, req.Zip_Code());
			stmt.setInt(12, req.House_Number());
			stmt.setString(13, req.Street_Address());
			stmt.setBoolean(14, req.International_Applicant());
			stmt.setString(15, req.Country_If_Not_USA());
			stmt.setDate(16, req.Interview_Avalibility_Date() != null ? java.sql.Date.valueOf(req.Interview_Avalibility_Date()) : null);			stmt.setBoolean(17, req.Interview_Scheduled());
			stmt.setBoolean(18, req.Accepted());
			stmt.setBoolean(19, req.Pending());
			stmt.setBoolean(20, req.Rejected());
			
			stmt.executeUpdate();
			
			try (ResultSet keys = stmt.getGeneratedKeys()){
				keys.next();
				return keys.getInt(1);
			}
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
	
	// Deletes the applicant with a specific applicant ID
	public boolean delete(int id) {
		String sql = "DELETE FROM APPLICANT WHERE Applicant_ID = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
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