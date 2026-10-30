package photoalbum.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import photoalbum.db.DB;
import photoalbum.domain.Photo;
/**
* All of the SQL for the ALBUM table.
*
* Every lookup includes the owner, so a photo that belongs to someone else is
* indistinguishable from one that doesn't exist.
*/
public class PhotoDao {
// The photo with this id in this user's album, or empty if there
// is none.
	public Optional<Photo> findById(int id, String username) {
		String sql = "SELECT id, url, caption FROM ALBUM WHERE id = ? " +
				"AND username = ?";
		try (Connection conn = DB.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			stmt.setString(2, username);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(toPhoto(rs));
				}
			return Optional.empty();
			}
		} catch (SQLException e) {
			throw SqlErrors.translate(e);
		}
	}
	// The current row of rs as a Photo. getString returns null for a
	// NULL caption.
	private Photo toPhoto(ResultSet rs) throws SQLException {
		return new Photo(rs.getInt("id"), rs.getString("url"), rs.getString("caption"));
	}
}