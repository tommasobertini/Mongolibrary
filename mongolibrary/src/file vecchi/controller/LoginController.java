package it.unipi.dii.inginf.lsmdb.mongolibrary.controller;


import it.unipi.dii.inginf.lsmdb.mongolibrary.LoginForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String getLoginForm(){
        return "provaloign";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute(name="loginForm") LoginForm loginForm, Model model){
        String username = loginForm.getUsername();
        String password = loginForm.getPassword();

        if("aaa".equals(username) && "123".equals(password)){
            return "home";
        }

        model.addAttribute("credenzialiInvialide", true);
        return "loginn";
    }
}
