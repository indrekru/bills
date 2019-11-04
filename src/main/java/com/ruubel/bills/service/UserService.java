package com.ruubel.bills.service;

import com.ruubel.bills.model.User;
import com.ruubel.bills.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    public static String ADMIN_USER_EMAIL = "indrekr@tutanota.com";
    private static String ADMIN_USER_PASSWORD = "pass";
    public final static String ROLE_USER = "ROLE_USER";

    private UserRepository repository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        createMainUser();
    }

    private UserDetails createMainUser() {
        Optional<UserDetails> user = repository.findByEmail(ADMIN_USER_EMAIL);
        if (!user.isPresent()) {
            User newUser = new User(ADMIN_USER_EMAIL, passwordEncoder.encode(ADMIN_USER_PASSWORD), true);
            return repository.save(newUser);
        }
        return user.get();
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<UserDetails> userDetails = repository.findByEmail(s);
        if (userDetails.isPresent()) {
            return userDetails.get();
        }
        throw new UsernameNotFoundException("No user found");
    }

    public Optional<UserDetails> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<User> findAllByActive(boolean active) {
        return repository.findAllByActive(active);
    }
}
