package internshipapp.service;

import java.util.List;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import internshipapp.dao.UserDao;
import internshipapp.domain.User;
import internshipapp.dto.NewUser;
import internshipapp.errors.AppException;
import internshipapp.errors.ConflictException;
import internshipapp.errors.ForbiddenException;
import internshipapp.errors.UnauthorizedException;
import internshipapp.errors.ValidationException;

public class UserService {
	private static final int MAX_USERNAME_LENGTH = 50;
	private static final int MIN_PASSWORD_LENGTH = 8;
	private static final String INVALID_LOGIN = "Invalid username or password";

	private final UserDao dao;
	private final SessionService sessionService;

	public UserService(UserDao dao, SessionService sessionService) {
		this.dao = dao;
		this.sessionService = sessionService;
	}

	// Creates an account. Anyone can self-register as APPLICANT; only an ADMIN can create an ADMIN.
	public User register(NewUser newUser, User caller) {
		if (newUser == null) {
			throw new ValidationException("Request body is required");
		}
		String username = newUser.username();
		if (username == null || username.isBlank()) {
			throw new ValidationException("username is required");
		}
		username = username.trim();
		if (username.length() > MAX_USERNAME_LENGTH) {
			throw new ValidationException("username must be at most " + MAX_USERNAME_LENGTH + " characters");
		}

		String password = newUser.password();
		if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
			throw new ValidationException("password must be at least " + MIN_PASSWORD_LENGTH + " characters");
		}

		String role = (newUser.role() == null || newUser.role().isBlank())
				? "APPLICANT"
				: newUser.role().trim().toUpperCase();

		if (!role.equals("APPLICANT") && !role.equals("ADMIN")) {
			throw new ValidationException("role must be APPLICANT or ADMIN");
		}

		// Only an logged-in ADMIN can create an ADMIN account
		if (role.equals("ADMIN")) {
			if (caller == null) {
				throw new UnauthorizedException("Login required to create an ADMIN account");
			}
			if (!caller.isAdmin()) {
				throw new ForbiddenException("Administrators only");
			}
		}

		String hash = BCrypt.hashpw(password, BCrypt.gensalt());
		try {
			dao.insert(username, hash, role);
		} catch (ConflictException e) {
			throw new ConflictException("Username " + username + " is already taken", e);
		}

		Optional<User> created = dao.findByUsername(username);
		if (created.isEmpty()) {
			throw new AppException("User " + username + " was inserted but can't be found");
		}
		return created.get();
	}

	// Logs in and returns a new session token.
	public String login(String username, String password) {
		if (username == null || password == null) {
			throw new UnauthorizedException(INVALID_LOGIN);
		}
		Optional<User> user = dao.findByUsername(username.trim());
		if (user.isEmpty() || !BCrypt.checkpw(password, user.get().passwordHash())) {
			throw new UnauthorizedException(INVALID_LOGIN);
		}
		return sessionService.start(user.get());
	}

	// Lists all users (Administrators only).
	public List<User> listUsers(User caller) {
		requireAdmin(caller);
		return dao.findAll();
	}

	public static void requireAdmin(User user) {
		if (user == null || !user.isAdmin()) {
			throw new ForbiddenException("Administrators only");
		}
	}
}