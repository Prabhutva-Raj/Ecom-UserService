package dev.prabhu.UserService.service;

import dev.prabhu.UserService.dto.UserDto;
import dev.prabhu.UserService.exception.InvalidCredentialException;
import dev.prabhu.UserService.exception.UserNotFoundException;
import dev.prabhu.UserService.model.SessionStatus;
import dev.prabhu.UserService.model.User;
import dev.prabhu.UserService.model.Session;
import dev.prabhu.UserService.repository.SessionRepository;
import dev.prabhu.UserService.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;


import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public ResponseEntity<UserDto> login(String email, String password) {
        //getting the user from DB
        Optional<User> userOptional = userRepository.findByEmail(email);

        if(userOptional.isEmpty()) {
            throw new UserNotFoundException("User with this email does not exist");
        }

        User user = userOptional.get();

        //verify the password
        if(!user.getPassword().equals(password)) {
            throw new InvalidCredentialException(("Invalid Credentials"));
        }

        //token generation
        String token = RandomStringUtils.randomAlphanumeric(30);

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
        return null;
    }

    public UserDto signup(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        User savedUser = userRepository.save(user);

        return UserDto.from(savedUser);
    }

    public SessionStatus validate(String token, Long userId) {
        return null;
    }

    //not to be in production
    public ResponseEntity<List<Session>> getAllSession() {
        List<Session> sessions = sessionRepository.findAll();
        return ResponseEntity.ok(sessions);
    }
}
