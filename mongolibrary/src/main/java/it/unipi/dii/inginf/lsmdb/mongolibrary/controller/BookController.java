package it.unipi.dii.inginf.lsmdb.mongolibrary.controller;

import it.unipi.dii.inginf.lsmdb.mongolibrary.MongolibraryApplication;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.BookMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.service.AdminManager;
import it.unipi.dii.inginf.lsmdb.mongolibrary.service.CustomerManager;
import it.unipi.dii.inginf.lsmdb.mongolibrary.service.UserManager;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.Constants;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.CustomBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@Controller
public class BookController {

    private UserManager userManager;

    static CustomBean customBean = MongolibraryApplication.getCustomBean();

    @RequestMapping(value = {"/searchBook", "searchBook/retrieve"}, method = RequestMethod.GET)
    public String searchBook(Model model){

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));

        System.out.println(customBean.getBean(Constants.SESSION_USERNAME));
        return "searchBook";
    }

    @RequestMapping(value = {"/retreiveBook"}, method = RequestMethod.POST)
    public String doSearchBook(@RequestParam("howmany") String howMany,
                               @RequestParam("parameter") String parameter,
                               @RequestParam("sort") Integer order,
                               Model model){

        if(customBean.getBean(Constants.SESSION_USER_CLASS) == "admin"){
            userManager = AdminManager.getInstance();
        }else{
            userManager = CustomerManager.getInstance();
        }
        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));

       // model.addAttribute("retreivedBooks", userManager.displayBooksSorted(parameter, Integer.parseInt(howMany), order));
        model.addAttribute("retreivedBooks", userManager.displayBooksSorted(parameter, "", 1, Integer.parseInt(howMany), order));
        model.addAttribute("retreive", "true");

        System.out.println(customBean.getBean(Constants.SESSION_USER_CLASS));
        return "searchBook";
    }

    @RequestMapping(value = {"/searchBook"}, method = RequestMethod.POST)
    public String doSearchBook(@RequestParam("searchType") String searchType,
                               @RequestParam("value") String value,
                               Model model){

        if(customBean.getBean(Constants.SESSION_USER_CLASS) == "admin"){
            userManager = AdminManager.getInstance();
        }else{
            userManager = CustomerManager.getInstance();
        }
        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));

        ArrayList<BookMongo> list = userManager.displayBooksSorted(searchType, value, 1, 50, 1);
        model.addAttribute("search", "true");

        if(list.size() > 0){
            model.addAttribute("searchedBooks", list);
            model.addAttribute("list", "notEmpty");
        }
        else{
            model.addAttribute("searchError", "true");
        }

        return "searchBook";
    }


    @RequestMapping(value = {"/viewBooks"}, method = RequestMethod.GET)
    public String viewBooks(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                            @RequestParam(value = "size", required = false, defaultValue = "52") int size, Model model) {


        if(customBean.getBean(Constants.SESSION_USER_CLASS) == "admin"){
            userManager = AdminManager.getInstance();
        }else{
            userManager = CustomerManager.getInstance();
        }
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));

        model.addAttribute("pageNumber", pageNumber);

        model.addAttribute("books", userManager.displayBooksSorted("Title", "", pageNumber, size, 1));
        System.out.println("pageNumber: " + pageNumber + "\n" + "size: " + size);

        return "viewBooks";
    }


    @GetMapping(value = "/bookDetails")
    public String bookDetails(@RequestParam(value = "title", required = false, defaultValue = "") String title,
                              @RequestParam(value = "info", required = false, defaultValue = "") String info,
                              @RequestParam(value = "infoMessage", required = false, defaultValue = "") String infoMessage,
                              Model model){
        System.out.println("book details of: " + title);

        if(customBean.getBean(Constants.SESSION_USER_CLASS) == "admin"){
            userManager = AdminManager.getInstance();
        }else{
            userManager = CustomerManager.getInstance();
        }
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));

        if(title.equals(""))
            //return from addReadingList command
            //get the same book
            title = (String) customBean.getBean(Constants.SELECTED_BOOK);
        else{
            //retrieve old book selected
            String oldTitle = (String) customBean.getBean(Constants.SELECTED_BOOK);
            if(oldTitle != null){
                if(!oldTitle.equals(title))
                    //update the selected book
                    customBean.replaceBean(Constants.SELECTED_BOOK, title);
            }
            else
                customBean.putBean(Constants.SELECTED_BOOK, title);
        }

        model.addAttribute("info", info);
        model.addAttribute("infoMessage", infoMessage);

        BookMongo book = userManager.displayBook(title);

        model.addAttribute("book", book);
        System.out.println(book.getReviews());
        model.addAttribute("reviews", book.getReviews());

        return "bookDetails";
    }











}
