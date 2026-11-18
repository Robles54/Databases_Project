package internshipapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import internshipapp.db.DB;
import internshipapp.domain.Applicant;
import internshipapp.domain.ApplicantApplication;
import internshipapp.domain.ApplicantDecision;
import internshipapp.domain.DivisionStats;
import internshipapp.dto.HireRequest;
import internshipapp.dto.NewApplicant;

/**
 * All of the SQL for the APPLICANT and STUDENT_HIRED tables.
 */
public class ApplicantDao {

	// Holds the current decision flags when locked with SELECT ... FOR UPDATE
	public record DecisionStatus(boolean accepted, boolean rejected) {
	}

	// Retrieves all applicants, sorted by Last_Name (Admin only).
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

	// Retrieves applicants filtered by class year.
	public List<Applicant> findByClassYear(int classYear) {
		String sql = "SELECT Applicant_ID, First_Name, Last_Name, Class_Year FROM APPLICANT WHERE Class_Year = ? ORDER BY Last_Name ASC";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, classYear);
			try (ResultSet rs = stmt.executeQuery()) {
				List<Applicant> applicants = new ArrayList<>();
				while (rs.next()) {
					applicants.add(toApplicant(rs));
				}
				return applicants;
			}
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}

	// Admin lookup: any applicant by ID.
	public Optional<Applicant> findById(int id) {
		String sql = "SELECT Applicant_ID, First_Name, Last_Name, Class_Year FROM APPLICANT WHERE Applicant_ID = ?";
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

	// Row-level check: only finds the applicant if it belongs to username.
	public Optional<Applicant> findByIdAndUsername(int id, String username) {
		String sql = "SELECT Applicant_ID, First_Name, Last_Name, Class_Year FROM APPLICANT WHERE Applicant_ID = ? AND username = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			stmt.setString(2, username);
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

	// Inserts a new applicant linked to the logged-in user's username.
	public int create(NewApplicant req, String username) {
		String sql = "INSERT INTO APPLICANT ("
				+ "First_Name, Last_Name, Middle_Name, College, Class_Year, "
				+ "Division_Applied, Intern_Title_Applied, Email, City, State, "
				+ "Zip_Code, House_Number, Street_Address, International_Applicant, "
				+ "Country_If_Non_USA, Interview_Avalibility_Date, Interview_Scheduled, "
				+ "Accepted, Pending, Rejected, username"
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
			stmt.setString(16, req.Interview_Avalibility_Date());
			stmt.setBoolean(17, req.Interview_Scheduled());
			stmt.setBoolean(18, req.Accepted());
			stmt.setBoolean(19, req.Pending());
			stmt.setBoolean(20, req.Rejected());
			stmt.setString(21, username);

			stmt.executeUpdate();
			try (ResultSet keys = stmt.getGeneratedKeys()) {
				keys.next();
				return keys.getInt(1);
			}
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}

	// Admin update: updates any applicant by ID.
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
			bindUpdateParams(stmt, req);
			stmt.setInt(21, id);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}

	// Row-level check update: only updates if Applicant_ID belongs to username.
	public boolean updateByUsername(int id, String username, NewApplicant req) {
		String sql = "UPDATE APPLICANT SET "
				+ "First_Name = ?, Last_Name = ?, Middle_Name = ?, College = ?, Class_Year = ?, "
				+ "Division_Applied = ?, Intern_Title_Applied = ?, Email = ?, City = ?, State = ?, "
				+ "Zip_Code = ?, House_Number = ?, Street_Address = ?, International_Applicant = ?, "
				+ "Country_If_Non_USA = ?, Interview_Avalibility_Date = ?, Interview_Scheduled = ?, "
				+ "Accepted = ?, Pending = ?, Rejected = ? "
				+ "WHERE Applicant_ID = ? AND username = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			bindUpdateParams(stmt, req);
			stmt.setInt(21, id);
			stmt.setString(22, username);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}

	private void bindUpdateParams(PreparedStatement stmt, NewApplicant req) throws SQLException {
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
	}

	// Admin delete: deletes any applicant by ID.
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

	// Row-level check delete: only deletes if Applicant_ID belongs to username.
	public boolean deleteByUsername(int id, String username) {
		String sql = "DELETE FROM APPLICANT WHERE Applicant_ID = ? AND username = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			stmt.setString(2, username);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}

	// Locking read inside a transaction: locks the applicant's row with FOR UPDATE.
	public Optional<DecisionStatus> findDecisionStatusForUpdate(Connection conn, int applicantId) throws SQLException {
		String sql = "SELECT Accepted, Rejected FROM APPLICANT WHERE Applicant_ID = ? FOR UPDATE";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, applicantId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(new DecisionStatus(
							rs.getBoolean("Accepted"),
							rs.getBoolean("Rejected")
					));
				}
				return Optional.empty();
			}
		}
	}

	// Updates APPLICANT status to Accepted inside a transaction.
	public void markApplicantHired(Connection conn, int applicantId) throws SQLException {
		String sql = "UPDATE APPLICANT SET Accepted = 1, Pending = 0, Rejected = 0 WHERE Applicant_ID = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, applicantId);
			stmt.executeUpdate();
		}
	}

	// Inserts a record into STUDENT_HIRED inside a transaction and returns generated Emp_ID.
	public int insertStudentHired(Connection conn, int applicantId, HireRequest req) throws SQLException {
		String sql = "INSERT INTO STUDENT_HIRED ("
				+ "Applicant_ID, Internship_Company, Office_City, Office_State, Office_Country, "
				+ "Education, Finance, Health, Media_Communications, "
				+ "Public_Service_Law_NonProfit_NGO, Intern_Title, Returning_Intern"
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, applicantId);
			stmt.setString(2, req.Internship_Company());
			stmt.setString(3, req.Office_City());
			stmt.setString(4, req.Office_State());
			stmt.setString(5, req.Office_Country());
			stmt.setBoolean(6, req.Education());
			stmt.setBoolean(7, req.Finance());
			stmt.setBoolean(8, req.Health());
			stmt.setBoolean(9, req.Media_Communications());
			stmt.setBoolean(10, req.Public_Service_Law_NonProfit_NGO());
			stmt.setString(11, req.Intern_Title());
			stmt.setBoolean(12, req.Returning_Intern());

			stmt.executeUpdate();
			try (ResultSet keys = stmt.getGeneratedKeys()) {
				keys.next();
				return keys.getInt(1);
			}
		}
	}

	// Admin lookup of applications for an applicant (3-Table Join).
	public List<ApplicantApplication> findApplicationsByApplicantId(int applicantId) {
		String sql = "SELECT a.Applicant_ID, a.First_Name, a.Last_Name, "
				+ "p.Posting_ID, p.Intern_Title, p.Division, p.Company, p.City, p.Salary, "
				+ "ia.Hiring_Manager, ia.Recuiter, ia.Returning_Applicant "
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

	// Row-level check lookup of applications for an applicant (enforces a.username = ? in WHERE).
	public List<ApplicantApplication> findApplicationsByApplicantIdAndUsername(int applicantId, String username) {
		String sql = "SELECT a.Applicant_ID, a.First_Name, a.Last_Name, "
				+ "p.Posting_ID, p.Intern_Title, p.Division, p.Company, p.City, p.Salary, "
				+ "ia.Hiring_Manager, ia.Recuiter, ia.Returning_Applicant "
				+ "FROM APPLICANT a "
				+ "JOIN INTERNSHIP_POSTING p "
				+ "  ON a.Intern_Title_Applied = p.Intern_Title AND a.Division_Applied = p.Division "
				+ "LEFT JOIN INTERNSHIP_APPLICATION ia "
				+ "  ON p.Posting_ID = ia.Posting_ID "
				+ "WHERE a.Applicant_ID = ? AND a.username = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, applicantId);
			stmt.setString(2, username);
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

	// Retrieves divisions whose applicant count is >= the average across all divisions.
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

	private Applicant toApplicant(ResultSet rs) throws SQLException {
		return new Applicant(
				rs.getInt("Applicant_ID"),
				rs.getString("First_Name"),
				rs.getString("Last_Name"),
				rs.getInt("Class_Year")
		);
	}

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
				rs.getString("Recuiter"),
				rs.getBoolean("Returning_Applicant")
		);
	}
}