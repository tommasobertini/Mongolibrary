package it.unipi.dii.inginf.lsmdb.mongolibrary.controller;

import it.unipi.dii.inginf.lsmdb.mongolibrary.MongolibraryApplication;
import it.unipi.dii.inginf.lsmdb.mongolibrary.service.AdminManager;
import it.unipi.dii.inginf.lsmdb.mongolibrary.service.CustomerManager;
import it.unipi.dii.inginf.lsmdb.mongolibrary.service.UserManager;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.Constants;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.CustomBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ListController {

    private UserManager userManager;

    static CustomBean customBean = MongolibraryApplication.getCustomBean();

    @GetMapping("/viewUsers")
    public String viewUsers(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                            Model model){

        if(customBean.getBean(Constants.SESSION_USER_CLASS) == "admin"){
            userManager = AdminManager.getInstance();
        }else{
            userManager = CustomerManager.getInstance();
        }

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));

        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("customers", userManager.displayUsersSorted("username", pageNumber, size, 1));
        model.addAttribute("mode", "viewAll");

        System.out.println("pageNumber: " + pageNumber);
        System.out.println("size: " + size);

        return "viewUsers";
    }

    @PostMapping("/viewUsers/search{username}")
    public String searchUser(@RequestParam(value = "pageNumber", required = false, defaultValue = "-1") int pageNumber,
                            @RequestParam(value = "size", required = false, defaultValue = "50") int size,
                            @RequestParam(name = "username") String username,
                            Model model){

        if(customBean.getBean(Constants.SESSION_USER_CLASS) == "admin"){
            userManager = AdminManager.getInstance();
        }else{
            userManager = CustomerManager.getInstance();
        }

        System.out.println("username: " + username);

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));

        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("customers", userManager.displayUsersSorted("username", username, pageNumber, size, 1));
        model.addAttribute("mode", "search");

        System.out.println("pageNumber: " + pageNumber);
        System.out.println("size: " + size);

        return "viewUsers";
    }


    @GetMapping("/borrowingList{username}")
    public String borrowingList(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                                @RequestParam(value = "size", required = false, defaultValue = "50") int size,
                                @PathVariable String username,
                                Model model) {

        if(customBean.getBean(Constants.SESSION_USER_CLASS) == "admin"){
            userManager = AdminManager.getInstance();
        }else{
            userManager = CustomerManager.getInstance();
        }

        System.out.println("sessionUsername: " + customBean.getBean(Constants.SESSION_USERNAME));
        System.out.println("username: " + username);
        System.out.println("pageNumber: " + pageNumber);
        System.out.println("size: " + size);

        if (username.equals(customBean.getBean(Constants.SESSION_USERNAME))){
            model.addAttribute("books", userManager.getDetailedBorrowingList(username));
        }else{
            model.addAttribute("books", userManager.getBorrowingListTitles(username));
        }

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("username", username);

        return "/borrowingList";
    }

    @GetMapping("/readingList{username}")
    public String readingList(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                              @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                              @PathVariable String username,
                              Model model) {


        if(customBean.getBean(Constants.SESSION_USER_CLASS) == "admin"){
            userManager = AdminManager.getInstance();
        }else{
            userManager = CustomerManager.getInstance();
        }

        System.out.println("sessionUsername: " + customBean.getBean(Constants.SESSION_USERNAME));
        System.out.println("username: " + username);
        System.out.println("pageNumber: " + pageNumber);
        System.out.println("size: " + size);


        if(username.equals("")){
            String u = (String) customBean.getBean(Constants.SESSION_USERNAME);
            System.out.println("u: " + u);
            model.addAttribute("books", userManager.getReadingListTitles(u));
            model.addAttribute("username", u);
        }else{
            model.addAttribute("books", userManager.getReadingListTitles(username));
            model.addAttribute("username", username);

        }

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("pageNumber", pageNumber);

        return "readingList";
    }
}
