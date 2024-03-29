package it.unipi.dii.inginf.lsmdb.mongolibrary.controller;


import com.mongodb.MongoException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.MongolibraryApplication;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.BookException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.ReviewException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.ReviewMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.service.CustomerManager;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.Constants;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.CustomBean;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.DateConverter;
import org.bson.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.awt.event.WindowFocusListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CustomerController {

    private CustomerManager customerManager = CustomerManager.getInstance();
    static CustomBean customBean = MongolibraryApplication.getCustomBean();

    @GetMapping("/userHome")
    public String home(Model model){
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        return "userHome";
    }


    @RequestMapping(value = {"/addReview"}, method = RequestMethod.POST)
    public String addReview(@RequestParam("bookTitle") String bookTitle,
                            @RequestParam("description") String description,
                            @RequestParam("rate") Integer score,
                            RedirectAttributes ra,
                            Model model){

        Document newReview = new Document()
                .append("bookTitle", bookTitle)
                .append("username", customerManager.getMyCustomerData().getUsername())
                .append("score", score)
                .append("reviewTime", DateConverter.toLong(LocalDate.now().toString()))
                .append("description", description)
                .append("likes", new ArrayList<>());

        try {
            customerManager.addReview(bookTitle, newReview);
            System.out.println("review added");
            ra.addAttribute("info", "addReview");
            ra.addAttribute("infoMessage", "Review added");

        } catch (ReviewException e) {
            ra.addAttribute("info", "error");
            ra.addAttribute("infoMessage", e.getMessage());
        }


        return "redirect:/bookDetails" + bookTitle +"/1";
    }

    @GetMapping(value = "/bookDetails{titlee}/addReadingList")
    public String addReadingList(@PathVariable("titlee") String title,
                                 RedirectAttributes ra,
                                 Model model){
        try
        {
            customerManager.addBookToReadingList(title);
            ra.addAttribute("info", "addReadingList");
            ra.addAttribute("infoMessage","The book has been added to your reading list");
            System.out.println("book added");

        } catch (BookException be)
        {
            System.out.println(be.getMessage());
            ra.addAttribute("info", "error");
            ra.addAttribute("infoMessage", be.getMessage());
        }

        return "redirect:/bookDetails" + title +"/1";
    }

    @GetMapping("/readingList{username}/removeFromReadingList{title}")
    public String remove(@PathVariable(value = "username") String username,
                         @PathVariable(value = "title") String title,
                         RedirectAttributes ra,
                         Model model) {

        try {
            customerManager.removeFromReadingList(title, Boolean.TRUE);
            System.out.println(title + " removed from reading list");

        } catch (BookException be) {
            ra.addAttribute("info", "error");
            ra.addAttribute("infoMessage", be.getMessage());
        }

        ra.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        ra.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        ra.addAttribute("username", username);


        return "redirect:/readingList";
    }

    @GetMapping(value = "/bookDetails{titlee}/addBorrowingList")
    public String addBorrowingList(@PathVariable("titlee") String title,
                                 RedirectAttributes ra,
                                 Model model){
        try
        {
            customerManager.addBookToBorrowingList(title);
            ra.addAttribute("info", "addBorrowingList");
            ra.addAttribute("infoMessage","The book has been added to your borrowing list");
            System.out.println("book added");

        } catch (BookException be)
        {
            System.out.println(be.getMessage());
            ra.addAttribute("info", "error");
            ra.addAttribute("infoMessage", be.getMessage());
        }

        return "redirect:/bookDetails" + title +"/1";
    }

    @GetMapping("/report{username}/{title}/{description}")
    public String report(@PathVariable(name = "username") String username,
                         @PathVariable(name = "title") String title,
                         @PathVariable(name = "description") String description,
                         RedirectAttributes ra,
                         Model model) {

        try {
            customerManager.reportReview(username, title, description);
            ra.addAttribute("info", "reportReview");
            ra.addAttribute("infoMessage","The review has been reported");
            System.out.println("review deleted:\nusername: " + username + "\nbookTitle: " + title + "\ndesription: " + description);

        } catch (MongoException be) {
            ra.addAttribute("info", "error");
            ra.addAttribute("infoMessage", be.getMessage());
        }


        return "redirect:/bookDetails" + title +"/1";
    }

    @GetMapping("/like{username}/{title}")
    public String like(@PathVariable(name = "username") String username,
                         @PathVariable(name = "title") String title,
                         RedirectAttributes ra,
                         Model model) {

        try {
            customerManager.addOrRemoveLikeToReview(username, title);
            ra.addAttribute("info", "like");
            ra.addAttribute("infoMessage","The review has been liked");
            System.out.println("liked review:\nusername: " + username + "\nbookTitle: " + title);

        } catch (MongoException be) {
            ra.addAttribute("info", "error");
            ra.addAttribute("infoMessage", be.getMessage());
        }


        return "redirect:/bookDetails" + title +"/1";
    }

    @GetMapping("/profile{username}")
    public String viewProfile(@PathVariable(name = "username") String username,
                              Model model){

        model.addAttribute("infoUser", customerManager.displayUser(username));
        model.addAttribute("followed", customerManager.displayFollows(username));
        model.addAttribute("followers", customerManager.displayFollowers(username));

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("username", username);

        return "profile";
    }

    @GetMapping("/follow{username}")
    public String follow(@PathVariable(name = "username") String username,
                              Model model){

        //model.addAttribute("infoUser", customerManager.displayUser(username));
        String myUsername = (String) customBean.getBean(Constants.SESSION_USERNAME);
        if(customerManager.addToFollowed(myUsername, username))
                System.out.println("followed");
        else
            System.out.println("error follow");

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("username", username);

        return "redirect:/viewUsers";
    }

    @GetMapping("/removeFollow{username}")
    public String removeFollow(@PathVariable(name = "username") String username,
                         Model model){

        //model.addAttribute("infoUser", customerManager.displayUser(username));
        String myUsername = (String) customBean.getBean(Constants.SESSION_USERNAME);
        if(customerManager.removeFromFollowed(myUsername, username))
            System.out.println("unfollowed");
        else
            System.out.println("error follow");

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("username", username);

        return "redirect:/viewUsers";
    }

    @GetMapping("/suggestions")
    public String suggestions(Model model){
        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        return "suggestions";
    }

    @GetMapping("/show{num}")
    public String showUsers1(@PathVariable(name = "num") int numSuggestion,
                            Model model){

        String username = (String)customBean.getBean(Constants.SESSION_USERNAME);
        //Integer date = DateConverter.toLong(LocalDate.now().toString());
        System.out.println(numSuggestion);

        switch (numSuggestion){
            case 1:
                model.addAttribute("results", customerManager.showBooksSuggestion(username));
                model.addAttribute("type", "books");
                model.addAttribute("info", "Show the books suggested to the user based on the borrowing list of the followed user" );
                break;
            case 2:
                model.addAttribute("results", customerManager.showBooksSuggestionReading(username));
                model.addAttribute("type", "books");
                model.addAttribute("info", "Show the books suggested to the user based on the reading list of the followed users" );
                break;
            case 3:
                model.addAttribute("results", customerManager.showBooksSuggestionTime(username, DateConverter.toLong(LocalDate.now().toString())));
                model.addAttribute("type", "books");
                model.addAttribute("info", "Show the books suggested to the user based on the borrowing list of the followed users in the last month" );
                break;
            case 4:
                model.addAttribute("results", customerManager.showUsersSuggestionReading(username));
                model.addAttribute("type", "users");
                model.addAttribute("info", "Show the customers suggested to the user based on the book in the reading list" );
                break;
            case 5:
                model.addAttribute("results", customerManager.showUsersSuggestionTime(username, DateConverter.toLong(LocalDate.now().toString())));
                model.addAttribute("type", "users");
                model.addAttribute("info", "Show the customers suggested to the user based on the book read in the last 30 days" );
                break;
            default:
                System.out.println("not a suggestion");
                model.addAttribute("info", "Not A Suggestion" );
                break;
        }

        model.addAttribute("suggestion", "ok");
        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));

        return "suggestions";
    }

    @GetMapping("/borrowingList{username}/returnBook{title}")
    public String returnBook(@PathVariable(name = "username") String username,
                             @PathVariable(name = "title") String title,
                             RedirectAttributes ra,
                             Model model){

        try{
            customerManager.modifyBorrowingListBookStatus(customerManager.getMyCustomerData().getUsername(), title, "RETURNED");
            System.out.println(username + " has returned the book " + title);
        }catch (MongoException me){
            System.out.println(me.getMessage());
        }


        ra.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        ra.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        ra.addAttribute("username", username);

        return "redirect:/borrowingList" +  username;
    }

}
