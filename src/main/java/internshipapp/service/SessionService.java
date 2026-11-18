package internshipapp.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import internshipapp.dao.SessionDao;
import internshipapp.domain.User;

public class SessionService {
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final Duration SESSION_LENGTH = Duration.ofHours(8);

	private final SessionDao dao;

	public SessionService(SessionDao dao) {
		this.dao = dao;
	}

	// Starts a session for this user and returns its token.
	public String start(User user) {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		dao.insert(token, user.username(), LocalDateTime.now().plus(SESSION_LENGTH));
		return token;
	}

	// The User a token belongs to, if the token is valid.
	public Optional<User> userForToken(String token) {
		return dao.findUserByToken(token);
	}

	// Ends the session with this token (logging out).
	public void end(String token) {
		dao.delete(token);
	}
}