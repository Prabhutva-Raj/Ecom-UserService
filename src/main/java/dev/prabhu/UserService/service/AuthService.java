package dev.prabhu.UserService.service;

import dev.prabhu.UserService.dto.UserDto;
import dev.prabhu.UserService.exception.InvalidCredentialException;
import dev.prabhu.UserService.exception.InvalidTokenException;
import dev.prabhu.UserService.exception.UserNotFoundException;
import dev.prabhu.UserService.model.SessionStatus;
import dev.prabhu.UserService.model.User;
import dev.prabhu.UserService.model.Session;
import dev.prabhu.UserService.repository.SessionRepository;
import dev.prabhu.UserService.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.*;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResponseEntity<UserDto> login(String email, String password) {
        //getting the user from DB
        Optional<User> userOptional = userRepository.findByEmail(email);

        if(userOptional.isEmpty()) {
            throw new UserNotFoundException("User with this email does not exist");
        }

        User user = userOptional.get();

        //verify the password
        //if(!user.getPassword().equals(password)) {
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialException(("Invalid Credentials"));
        }

        //Token generation
        //String token = RandomStringUtils.randomAlphanumeric(30);
        MacAlgorithm alg = Jwts.SIG.HS256;
        SecretKey key = alg.key().build();

        Map<String, Object> jsonForJWT = new HashMap<>();       //adding claims
        jsonForJWT.put("email", user.getEmail());
        jsonForJWT.put("roles", user.getRoles());
        jsonForJWT.put("createdAt", new Date());
        jsonForJWT.put("expiringAt", new Date(LocalDate.now().plusDays(3).toEpochDay()));

        String token = Jwts.builder().claims(jsonForJWT).signWith(key, alg).compact();      //generating token

        //Session Creation
        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(token);
        session.setUser(user);
        sessionRepository.save(session);

        //generating the response

        //setting up the headers
        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token:" + token);

        //returning response
        return new ResponseEntity<>(UserDto.from(user), headers, HttpStatus.OK);
    }

    public ResponseEntity<Void> logout(String token, Long userId) {
        //validations -> token exists, token is not expired, user exists else throw exception
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);
        if (sessionOptional.isEmpty()) {
            throw new InvalidTokenException("Token or User is invalid");
        }

        if(sessionOptional.get().getSessionStatus().equals(SessionStatus.ENDED)) {
            throw new InvalidTokenException("Session is already ended");
        }

        Session session = sessionOptional.get();
        session.setSessionStatus(SessionStatus.ENDED);
        sessionRepository.save(session);

        return ResponseEntity.ok().build();
    }

    public UserDto signup(String email, String password) {
        User user = new User();
        user.setEmail(email);
        //user.setPassword(password);
        user.setPassword(bCryptPasswordEncoder.encode(password));

        User savedUser = userRepository.save(user);

        return UserDto.from(savedUser);
    }

    public SessionStatus validate(String token, Long userId) {
        // check expiry: jwts parser = read the expiry date

        //verifying from DB if session exists
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);
        if (sessionOptional.isEmpty() || sessionOptional.get().getSessionStatus().equals(SessionStatus.ENDED)) {
            throw new InvalidTokenException("token is invalid");
        }

        return SessionStatus.ACTIVE;
    }

    //not to be in production
    public ResponseEntity<List<Session>> getAllSession() {
        List<Session> sessions = sessionRepository.findAll();
        return ResponseEntity.ok(sessions);
    }
}
