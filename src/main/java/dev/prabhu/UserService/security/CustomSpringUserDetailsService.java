package dev.prabhu.UserService.security;

import dev.prabhu.UserService.model.User;
import dev.prabhu.UserService.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CustomSpringUserDetailsService implements UserDetailsService {
    private UserRepository userRepo;

    public CustomSpringUserDetailsService (UserRepository userRepository) {
        this.userRepo = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByEmail(username);
        return null;
    }
}
