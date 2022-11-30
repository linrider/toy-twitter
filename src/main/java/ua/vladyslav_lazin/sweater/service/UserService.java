package ua.vladyslav_lazin.sweater.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ua.vladyslav_lazin.sweater.entity.Role;
import ua.vladyslav_lazin.sweater.entity.User;
import ua.vladyslav_lazin.sweater.repository.UserRepository;

import java.util.Collections;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final MailSender mailSender;

    public UserService(UserRepository userRepository, MailSender mailSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
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
            userRepository.save(user);
            if (StringUtils.hasLength(user.getEmail())) {
                String message = String.format(
                        "Hello, %s! \n" + "Welcome to Swearter. Please, visit the next link: http://localhost:8081/activate/%s",
                        user.getUsername(),
                        user.getActivationCode()
                );
                mailSender.sendMail(user.getEmail(), "Activation code", message);
            }
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
}
