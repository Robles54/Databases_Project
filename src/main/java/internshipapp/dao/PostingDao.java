package internshipapp.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import internshipapp.db.DB;
import internshipapp.domain.Posting;
import internshipapp.dto.NewPosting;
import internshipapp.errors.NotFoundException;

// For INTERNSHIP_POSTING TABLE
public class PostingDao {
	
	// Retrieves all internship posting along with their application counts
	// (Join + Aggregation)
	
	public List<Posting> findAll(){
		String sql = "SELECT p.Posting_ID, p.Intern_Title, p.Division, p.Company, p.City, "
				+ "p.Salary, p.Start_Date, p.End_Date, p.Accepting_Applications, "
				+ "COUNT(a.Posting_ID) AS Application_Count "
				+ "FROM INTERNSHIP_POSTING p "
				+ "LEFT JOIN INTERNSHIP_APPLICATION a ON p.Posting_ID = a.Posting_ID "
				+ "GROUP BY p.Posting_ID, p.Intern_Title, p.Division, p.Company, p.City, "
				+ "p.Salary, p.Start_Date, p.End_Date, p.Accepting_Applications "
				+ "ORDER BY p.Posting_ID ASC";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()){
			List<Posting> postings = new ArrayList<>();
			while (rs.next()) {
				postings.add(toPosting(rs));
			}
			return postings;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
	
	// Retrieves internship postings that have no rows in INTERNSHIP_APPLICATION (Subquery).
	public List<Posting> findUnapplied() {
		String sql = "SELECT Posting_ID, Intern_Title, Division, Company, City, "
				+ "Salary, Start_Date, End_Date, Accepting_Applications, "
				+ "0 AS Application_Count "
				+ "FROM INTERNSHIP_POSTING "
				+ "WHERE Posting_ID NOT IN (SELECT Posting_ID FROM INTERNSHIP_APPLICATION) "
				+ "ORDER BY Posting_ID ASC";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			List<Posting> postings = new ArrayList<>();
			while (rs.next()) {
				postings.add(toPosting(rs));
			}
			return postings;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
	
	private Posting toPosting(ResultSet rs) throws SQLException {
		return new Posting(
			rs.getInt("Posting_ID"),
			rs.getString("Intern_Title"),
			rs.getString("Division"),
			rs.getString("Company"),
			rs.getString("City"),
			rs.getInt("Salary"),
			rs.getDate("Start_Date").toLocalDate(),
			rs.getDate("End_Date").toLocalDate(),
			rs.getBoolean("Accepting_Applications"),
			rs.getInt("Application_Count")
		);
	}
	
	// Inserts a new internship posting and returns the generated Posting_ID.
	public int create(NewPosting req) {
		String sql = "INSERT INTO INTERNSHIP_POSTING ("
				+ "Intern_Title, Division, Company, City, Country, "
				+ "Technical_Skill_Required, HR_Contact, Salary, "
				+ "Start_Date, End_Date, Accepting_Applications"
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, req.Intern_Title());
			stmt.setString(2, req.Division());
			stmt.setString(3, req.Company());
			stmt.setString(4, req.City());
			stmt.setString(5, req.Country() != null ? req.Country() : "United States");
			stmt.setString(6, req.Technical_Skill_Required());
			stmt.setString(7, req.HR_Contact());
			stmt.setInt(8, req.Salary());
			stmt.setDate(9, Date.valueOf(req.Start_Date()));
			stmt.setDate(10, Date.valueOf(req.End_Date()));
			stmt.setBoolean(11, req.Accepting_Applications());

			stmt.executeUpdate();
				try (ResultSet keys = stmt.getGeneratedKeys()) {
					keys.next();
					return keys.getInt(1);
				}
			} catch (SQLException e) {
				throw SqlErrors.translate(e);
			}
		}
	
	// Updates the internship posting with this id. Returns true if a row was updated.
	public boolean update(int id, NewPosting req) {
		String sql = "UPDATE INTERNSHIP_POSTING SET "
				+ "Intern_Title = ?, Division = ?, Company = ?, City = ?, Country = ?, "
				+ "Technical_Skill_Required = ?, HR_Contact = ?, Salary = ?, "
				+ "Start_Date = ?, End_Date = ?, Accepting_Applications = ? "
				+ "WHERE Posting_ID = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, req.Intern_Title());
			stmt.setString(2, req.Division());
			stmt.setString(3, req.Company());
			stmt.setString(4, req.City());
			stmt.setString(5, req.Country() != null ? req.Country() : "United States");
			stmt.setString(6, req.Technical_Skill_Required());
			stmt.setString(7, req.HR_Contact());
			stmt.setInt(8, req.Salary());
			stmt.setDate(9, Date.valueOf(req.Start_Date()));
			stmt.setDate(10, Date.valueOf(req.End_Date()));
			stmt.setBoolean(11, req.Accepting_Applications());
			stmt.setInt(12, id);

			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
	
	// Deletes the internship posting with this id. Returns true if a row is deleted
	public boolean delete(int id) {
		String sql = "DELETE FROM INTERNSHIP_POSTING WHERE Posting_ID = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
	
}
