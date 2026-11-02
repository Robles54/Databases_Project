package internshipapp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import internshipapp.dao.PhotoDao;
import internshipapp.routes.CurrentUser;
import internshipapp.routes.ErrorHandler;
import internshipapp.routes.PhotoRoutes;
import internshipapp.service.PhotoService;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

/**
 * Starts the photo album API on port 7070.
 *
 * The only class that knows every layer: it builds them bottom-up and hands
 * each one the layer below.
 */
public class Main {

	public static void main(String[] args) {
		// TODO: create DAOs
		PhotoDao photoDao = new PhotoDao();

		// TODO: create services
		PhotoService photoService = new PhotoService(photoDao);

		// TODO: create routes
		PhotoRoutes photoRoutes = new PhotoRoutes (photoService);

		// Javalin 7: all configuration and routes go inside the config block
		Javalin.create(config -> {
			config.jetty.port = 7070;

			// JSON: dates as "1979-10-12" rather than [1979,10,12]
			// and ignore JSON fields a DTO doesn't have
			config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
				mapper.registerModule(new JavaTimeModule());
				mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
				mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			}));

			// One place turns every exception into a status code and error JSON
			ErrorHandler.register(config);
			
			// Every route needs to know whose album the request is for
			CurrentUser.register(config);

			// TODO: register routes
			photoRoutes.register(config);
			
		}).start();
	}
}
