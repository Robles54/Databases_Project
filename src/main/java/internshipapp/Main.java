package internshipapp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import internshipapp.dao.ApplicantDao;
import internshipapp.dao.PostingDao;
import internshipapp.dao.SessionDao;
import internshipapp.dao.UserDao;
import internshipapp.routes.ApplicantRoutes;
import internshipapp.routes.AuthRoutes;
import internshipapp.routes.CurrentUser;
import internshipapp.routes.ErrorHandler;
import internshipapp.routes.PostingRoutes;
import internshipapp.routes.UserRoutes;
import internshipapp.service.ApplicantService;
import internshipapp.service.PostingService;
import internshipapp.service.SessionService;
import internshipapp.service.UserService;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

/**
 * Starts the internship application API on port 7070.
 *
 * The only class that knows every layer: it builds them bottom-up and hands
 * each one the layer below.
 */
public class Main {

	public static void main(String[] args) {
		// Create DAOs
		UserDao userDao = new UserDao();
		SessionDao sessionDao = new SessionDao();
		ApplicantDao applicantDao = new ApplicantDao();
		PostingDao postingDao = new PostingDao();

		// Create services (SessionService first, since UserService and CurrentUser need it)
		SessionService sessionService = new SessionService(sessionDao);
		UserService userService = new UserService(userDao, sessionService);
		ApplicantService applicantService = new ApplicantService(applicantDao);
		PostingService postingService = new PostingService(postingDao);

		// Create auth filter and routes
		CurrentUser currentUser = new CurrentUser(sessionService);
		UserRoutes userRoutes = new UserRoutes(userService);
		AuthRoutes authRoutes = new AuthRoutes(userService, sessionService);
		ApplicantRoutes applicantRoutes = new ApplicantRoutes(applicantService);
		PostingRoutes postingRoutes = new PostingRoutes(postingService);

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
			
			// Every non-public route needs a valid session token
			currentUser.register(config);

			// Register routes
			userRoutes.register(config);
			authRoutes.register(config);
			applicantRoutes.register(config);
			postingRoutes.register(config);
			
		}).start();
	}
}