package dev.prabhu.UserService.service;

import dev.prabhu.UserService.dto.UserDto;
import dev.prabhu.UserService.model.SessionStatus;
import dev.prabhu.UserService.model.User;
import dev.prabhu.UserService.model.Session;
import dev.prabhu.UserService.repository.SessionRepository;
import dev.prabhu.UserService.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


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
        Optional<User> userOptional = userRepository.findByEmail(email);

        if(userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if(!user.getPassword().equals(password)) {
            return null;
        }

        String token = RandomStringUtils.randomAlphanumeric(30);

        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(token);
        session.setUser(user);
        sessionRepository.save(session);

        return null;
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
}
