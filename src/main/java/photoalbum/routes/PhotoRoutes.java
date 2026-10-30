package photoalbum.routes;
import static photoalbum.routes.Params.pathId;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import photoalbum.domain.Photo;
import photoalbum.service.PhotoService;
/**
* The HTTP side of the photo endpoints.
*
* Each handler reads the request, calls the service, and writes the response.
* No SQL and no rules here; failures are exceptions that ErrorHandler turns
* into status codes.
*/
public class PhotoRoutes {
	private final PhotoService service;
	public PhotoRoutes(PhotoService service) {
		this.service = service;
	}
	// Called from inside Javalin.create(config -> { ... })
	public void register(JavalinConfig config) {
		config.routes.get("/photos/{id}", ctx -> getPhoto(ctx));
	}
	// GET /photos/{id} -> 200 and the photo
	private void getPhoto(Context ctx) {
		int id = pathId(ctx);
		String username = CurrentUser.get(ctx);
		Photo photo = service.getPhoto(id, username);
		ctx.json(photo);
	}
}