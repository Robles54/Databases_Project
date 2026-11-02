package internshipapp.service;
import java.util.Optional;

import internshipapp.dao.PhotoDao;
import internshipapp.domain.Photo;
import internshipapp.errors.NotFoundException;
/**
* The rules for working with photos.
*
* Knows nothing about HTTP: it takes plain values and returns domain objects,
* and reports failures by throwing the application's exceptions.
*/
public class PhotoService {
	private final PhotoDao dao;
	public PhotoService(PhotoDao dao) {
		this.dao = dao;
	}
	// One of username's photos. NotFoundException if there's no such
	// photo or it belongs to another user.
	public Photo getPhoto(int id, String username) {
		Optional<Photo> photo = dao.findById(id, username);
		if (photo.isEmpty()) {
			throw new NotFoundException("No photo with id " + id);
		}
		return photo.get();
	}
}