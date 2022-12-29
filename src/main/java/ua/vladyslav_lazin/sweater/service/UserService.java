package ua.vladyslav_lazin.sweater.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ua.vladyslav_lazin.sweater.entity.Role;
import ua.vladyslav_lazin.sweater.entity.User;
import ua.vladyslav_lazin.sweater.repository.UserRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final MailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, MailSender mailSender, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    public boolean addUser(User user) {
        User userFromDb = userRepository.findByUsername(user.getUsername());
        boolean userFlag;
        if (userFromDb != null) {
            userFlag = false;
        } else {
            user.setActive(true);
            user.setRoles(Collections.singleton(Role.USER));
            user.setActivationCode(UUID.randomUUID().toString());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            sendMessage(user);
            userFlag = true;
        }
        return userFlag;
    }

    public boolean activateUser(String code) {
        User user = userRepository.findByActivationCode(code);
        boolean userFlag = true;
        if (user == null) {
            userFlag = false;
        } else {
            user.setActivationCode(null);
            userRepository.save(user);
        }
        return userFlag;
    }

    private void sendMessage(User user) {
        if (StringUtils.hasLength(user.getEmail())) {
            String message = String.format(
                    "Hello, %s! \n"
                            + "Welcome to Swearter. Please, visit the next link: http://localhost:8081/activate/%s",
                    user.getUsername(),
                    user.getActivationCode());
            mailSender.sendMail(user.getEmail(), "Activation code", message);
        }
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void saveUser(User user, String username, Map<String, String> form) {
        user.setUsername(username);
        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());
        user.getRoles().clear();
        for (String key : form.keySet()) {
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }
        userRepository.save(user);
    }

    public void updateProfile(User user, String password, String email) {
        String userEmail = user.getEmail();

        boolean isEmailChanged = (email != null && !email.equals(userEmail)) ||
                (userEmail != null && !userEmail.equals(email));
        if (isEmailChanged) {
            user.setEmail(email);

            if (StringUtils.hasLength(email)) {
                user.setActivationCode(UUID.randomUUID().toString());
            }
        }

        if (StringUtils.hasLength(password)) {
            user.setPassword(password);
        }
        userRepository.save(user);

        if (isEmailChanged) {
            sendMessage(user);
        }
    }
}
