package it.unipi.dii.inginf.lsmdb.mongolibrary.controller;

import it.unipi.dii.inginf.lsmdb.mongolibrary.model.Customer;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.UserMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.UserMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.Optional;

//@RestController
@Controller
public class SignupController {

    @Autowired
    UserMongo userMongo;
    UserMongoRepository repo;

    @GetMapping("/signup")
    public String signup(){
        return "signup";
    }

    @PostMapping("/doSignup")
    public String doSignup(@RequestParam("username") String username,
                           @RequestParam("fullname") String fullname,
                           @RequestParam("email") String email,
                           @RequestParam("birth") Date birth,
                           @RequestParam("nationality") String nationality,
                           @RequestParam("password") String password,
                           @RequestParam("pass-confirm") String passConfirm,
                           Model model){

        model.addAttribute("username", username);
        model.addAttribute("fullname", fullname);
        model.addAttribute("email", email);
        model.addAttribute("birth", birth);
        model.addAttribute("nationality", nationality);
        model.addAttribute("password", password);

        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setStatus(true);
        customer.setEmail(email);
        customer.setFullName(fullname);
        customer.setNationality(nationality);
        customer.setDateOfBirth((int)birth.getTime());
        customer.setPassword(password);

        Optional<Customer> c = userMongo.findByUsername(username);
        if(c.isEmpty()) {
            userMongo.addUser(customer);
            return "provalogin";
        }
        else {
            return "Alreadyexist";
        }

    }


}
