package it.unipi.dii.inginf.lsmdb.mongolibrary.controller;

import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.RegisterException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.model.Customer;
import it.unipi.dii.inginf.lsmdb.mongolibrary.service.RegisterManager;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.Password;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Optional;
import org.bson.Document;

@Controller
public class RegisterController {

    private RegisterManager registerManager = new RegisterManager();

    @GetMapping("/signup")
    public String signup(){ return "signup"; }

    @PostMapping("/signup")
    public String doSignup(@RequestParam("username") String username,
                           @RequestParam("email") String email,
                           @RequestParam("birth") String birth,
                           @RequestParam("nationality") String nationality,
                           @RequestParam("password") String password,
                           @RequestParam("confirm-password") String passConfirm,
                           Model model){

        if(!passConfirm.equals(passConfirm)){
            model.addAttribute("info", "error");
            model.addAttribute("infoMessage", "User not registered correctly. Retry");
            return "signup";
        }else {
            try{
                //Checks unique values
                registerManager.doRegistrationCheck(username, email);

                //Creates document with all the data of the user
                Document user = new Document();
                user.append("username" , username);
                user.append("email", email);
                Password userPassword = new Password(password);
                user.append("password", userPassword.getHashPassword());
                user.append("salt", userPassword.getSalt());
                user.append("birthYear", Integer.valueOf(birth));
                user.append("nationality" , nationality);
                user.append("readingList", new ArrayList<>()); //EMPTY ARRAY
                user.append("borrowingList", new ArrayList<>()); //EMPTY ARRAY
                user.append("status", true);

                System.out.println(user.toString()); //DEBUG

                registerManager.doRegistration(user); //Adds user to db
                model.addAttribute("info", "signup");
                model.addAttribute("infoMessage", "User added successfuly");


                return "login";
            }
            catch (RegisterException re)
            {
                System.out.println(re.getMessage()); //DEBUG
                return "signup";

            }

        }
    }
}
