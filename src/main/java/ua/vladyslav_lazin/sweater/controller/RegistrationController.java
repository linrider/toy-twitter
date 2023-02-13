package ua.vladyslav_lazin.sweater.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import ua.vladyslav_lazin.sweater.entity.User;
import ua.vladyslav_lazin.sweater.service.UserService;

import java.util.Map;

import javax.validation.Valid;

@Controller
public class RegistrationController {
    private final static String CAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify";
    private final UserService userService;

    @Value("${recaptcha.secret}")
    private String secret;

    private RestTemplate restTemplate;

    public RegistrationController(UserService userService, RestTemplate restTemplate) {
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/registration")
    public String registrate() {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(
            @RequestParam("password2") String passwordConfirm,
            @RequestParam("g-recaptcha-responce") String captchaResponce, 
            @Valid User user, 
            BindingResult bindingResult, 
            Model model
    ) {
        boolean isConfirmEmpty = !StringUtils.hasLength(passwordConfirm);
        if (!StringUtils.hasLength(passwordConfirm)) {
            model.addAttribute("password2Error", "Password confirmation cannot be blank");
            
        }
        if (user.getPassword() != null && !user.getPassword().equals(passwordConfirm)) {
            model.addAttribute("passwordError", "Passwords are different");
            return "registration";
        }
        
        if (isConfirmEmpty || bindingResult.hasErrors()) {
            Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errors);
            return "registration";
        }
        
        if (!userService.addUser(user)) {
            model.addAttribute("usernameError", "This user already exists!");
            return "registration";
        }
        
        return "redirect:/login";
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code) {
        boolean isActivated = userService.activateUser(code);
        if (isActivated) {
            model.addAttribute("messageType", "success");
            model.addAttribute("message", "User successfully activated");
        } else {
            model.addAttribute("messageType", "danger");
            model.addAttribute("message", "Activation code is not found");
        }
        return "login";
    }

}
