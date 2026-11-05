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
import internshipapp.domain.ApplicantApplication;
import internshipapp.domain.ApplicantDecision;
import internshipapp.domain.DivisionStats;
import internshipapp.dto.HireRequest;
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
	
	// Updates APPLICANT status and inserts into STUDENT_HIRED in one transaction
	// Returns the generated Emp_ID, or empty if no such Applicant_ID exists
	public Optional<Integer> hireApplicant(int applicantId, HireRequest req) {
		String updateSql = "UPDATE APPLICANT SET Accepted = 1, Pending = 0, Rejected = 0 WHERE Applicant_ID = ?";
		String insertSql = "INSERT INTO STUDENT_HIRED ("
				+ "Applicant_ID, Internship_Company, Office_City, Office_State, Office_Country, "
				+ "Education, Finance, Health, Media_Communications, "
				+ "Public_Service_Law_NonProfit_NGO, Intern_Title, Returning_Intern"
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = DB.getConnection()) {
			conn.setAutoCommit(false);
			try (PreparedStatement updateStmt = conn.prepareStatement(updateSql);
					PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

				// 1. Update APPLICANT table
				updateStmt.setInt(1, applicantId);
				int rowsUpdated = updateStmt.executeUpdate();
				if (rowsUpdated == 0) {
					conn.rollback();
					return Optional.empty();
				}

				// 2. Insert into STUDENT_HIRED table
				insertStmt.setInt(1, applicantId);
				insertStmt.setString(2, req.Internship_Company());
				insertStmt.setString(3, req.Office_City());
				insertStmt.setString(4, req.Office_State());
				insertStmt.setString(5, req.Office_Country());
				insertStmt.setBoolean(6, req.Education());
				insertStmt.setBoolean(7, req.Finance());
				insertStmt.setBoolean(8, req.Health());
				insertStmt.setBoolean(9, req.Media_Communications());
				insertStmt.setBoolean(10, req.Public_Service_Law_NonProfit_NGO());
				insertStmt.setString(11, req.Intern_Title());
				insertStmt.setBoolean(12, req.Returning_Intern());

				insertStmt.executeUpdate();

				try (ResultSet keys = insertStmt.getGeneratedKeys()) {
					keys.next();
					int empId = keys.getInt(1);
					conn.commit();
					return Optional.of(empId);
				}
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
		
	// Retrieves all applications and posting details for a single applicant (3 Table - Join)
	public List<ApplicantApplication> findApplicationByApplicantID (int applicantId){
		String sql = "SELECT a.Applicant_ID, a.First_Name, a.Last_Name, "
				+ "p.Posting_ID, p.Intern_Title, p.Division, p.Company, p.City, p.Salary, "
				+ "ia.Hiring_Manager, ia.Recruiter, ia.Returning_Applicant "
				+ "FROM APPLICANT a "
				+ "JOIN INTERNSHIP_POSTING p "
				+ "  ON a.Intern_Title_Applied = p.Intern_Title AND a.Division_Applied = p.Division "
				+ "LEFT JOIN INTERNSHIP_APPLICATION ia "
				+ "  ON p.Posting_ID = ia.Posting_ID "
				+ "WHERE a.Applicant_ID = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, applicantId);
			try (ResultSet rs = stmt.executeQuery()) {
				List<ApplicantApplication> applications = new ArrayList<>();
				while (rs.next()) {
					applications.add(toApplicantApplication(rs));
				}
				return applications;
			}
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
	
	
	// Updates the applicant with this id. Returns true if a row was updated.
	public boolean update(int id, NewApplicant req) {
		String sql = "UPDATE APPLICANT SET "
				+ "First_Name = ?, Last_Name = ?, Middle_Name = ?, College = ?, Class_Year = ?, "
				+ "Division_Applied = ?, Intern_Title_Applied = ?, Email = ?, City = ?, State = ?, "
				+ "Zip_Code = ?, House_Number = ?, Street_Address = ?, International_Applicant = ?, "
				+ "Country_If_Non_USA = ?, Interview_Avalibility_Date = ?, Interview_Scheduled = ?, "
				+ "Accepted = ?, Pending = ?, Rejected = ? "
				+ "WHERE Applicant_ID = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
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
			stmt.setString(16, req.Interview_Avalibility_Date());
			stmt.setBoolean(17, req.Interview_Scheduled());
			stmt.setBoolean(18, req.Accepted());
			stmt.setBoolean(19, req.Pending());
			stmt.setBoolean(20, req.Rejected());
			stmt.setInt(21, id);

			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}

	// Retrieves divisions whose applicant count is >= the average across all divisions (Aggregation + Subquery).
	public List<DivisionStats> findHighDemandDivisions() {
		String sql = "SELECT Division_Applied, COUNT(*) AS Applicant_Count "
				+ "FROM APPLICANT "
				+ "GROUP BY Division_Applied "
				+ "HAVING COUNT(*) >= ("
				+ "    SELECT AVG(div_counts.cnt) "
				+ "    FROM (SELECT COUNT(*) AS cnt FROM APPLICANT GROUP BY Division_Applied) AS div_counts"
				+ ") "
				+ "ORDER BY Applicant_Count DESC";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			List<DivisionStats> stats = new ArrayList<>();
			while (rs.next()) {
				stats.add(new DivisionStats(
						rs.getString("Division_Applied"),
						rs.getInt("Applicant_Count")
				));
			}
			return stats;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
	
	
	// The current row of rs as an ApplicantApplication
	private ApplicantApplication toApplicantApplication(ResultSet rs) throws SQLException {
		return new ApplicantApplication(
				rs.getInt("Applicant_ID"),
				rs.getString("First_Name"),
				rs.getString("Last_Name"),
				rs.getInt("Posting_ID"),
				rs.getString("Intern_Title"),
				rs.getString("Division"),
				rs.getString("Company"),
				rs.getString("City"),
				rs.getInt("Salary"),
				rs.getString("Hiring_Manager"),
				rs.getString("Recruiter"),
				rs.getBoolean("Returning_Applicant")
				);
	}
	
	// Retrieves a unified list of hired and denied applicants (Set Operation UNION + Joins).
	public List<ApplicantDecision> findAllDecisions() {
		String sql = "SELECT a.Applicant_ID, a.First_Name, a.Last_Name, a.Division_Applied, "
				+ "'HIRED' AS Decision, "
				+ "CONCAT(sh.Intern_Title, ' at ', sh.Internship_Company) AS Details "
				+ "FROM APPLICANT a "
				+ "JOIN STUDENT_HIRED sh ON a.Applicant_ID = sh.Applicant_ID "
				+ "UNION "
				+ "SELECT a.Applicant_ID, a.First_Name, a.Last_Name, a.Division_Applied, "
				+ "'DENIED' AS Decision, "
				+ "da.Interviewer_Notes AS Details "
				+ "FROM APPLICANT a "
				+ "JOIN DENIED_APPLICANT da ON a.Applicant_ID = da.Applicant_ID "
				+ "ORDER BY Last_Name ASC, First_Name ASC";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			List<ApplicantDecision> decisions = new ArrayList<>();
			while (rs.next()) {
				decisions.add(new ApplicantDecision(
						rs.getInt("Applicant_ID"),
						rs.getString("First_Name"),
						rs.getString("Last_Name"),
						rs.getString("Division_Applied"),
						rs.getString("Decision"),
						rs.getString("Details")
				));
			}
			return decisions;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
	
}