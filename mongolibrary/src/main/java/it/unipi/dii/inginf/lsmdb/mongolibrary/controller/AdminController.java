package it.unipi.dii.inginf.lsmdb.mongolibrary.controller;

import com.mongodb.MongoException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.MongolibraryApplication;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.BookException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.ReviewException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.BookMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.ReviewMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.service.AdminManager;
import it.unipi.dii.inginf.lsmdb.mongolibrary.service.CustomerManager;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.Constants;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.CustomBean;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.DateConverter;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


@Controller
public class AdminController {

    private AdminManager adminManager = AdminManager.getInstance();
    static CustomBean customBean = MongolibraryApplication.getCustomBean();
/*
    @Autowired
    private BookService service;
*/
    @GetMapping("/admin")
    public String admin(Model model){
        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        return "admin"; }

    @RequestMapping(value = {"/addBook"}, method = RequestMethod.GET)
    public String addBook(Model model){

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        return "addBook";
    }

    @RequestMapping(value = {"/addBook"}, method = RequestMethod.POST)
    public String doAddBook(@RequestParam("bookTitle") String bookTitle,
                            @RequestParam("authors") String authors,
                            @RequestParam("categories") String categories,
                            @RequestParam("publisher") String publisher,
                            @RequestParam("publishDate") String publishDate,
                            @RequestParam("description") String description,
                            @RequestParam("copies") String copies,
                            Model model){
        try
        {
            //Creates document with all the data of the user
            Document book = new Document();
            book.append("Title" , bookTitle);
            book.append("description", description);
            book.append("authors", authors);
            book.append("publisher" , publisher);
            book.append("publishedDate", DateConverter.toLong(publishDate));
            book.append("categories", categories);
            book.append("score", (double) 0);
            book.append("copies", Integer.parseInt(copies));
            book.append("reviews", new ArrayList<>());

            System.out.println(book); //DEBUG

            adminManager.addBook(book); //Adds book to DB

            model.addAttribute("info", "addBook");
            model.addAttribute("infoMessage", "book added successfuly");
            return "/addBook";

        }
        catch (BookException be)
        {
            System.out.println(be.getMessage());
            model.addAttribute("info", "error");
            model.addAttribute("infoMessage", be.getMessage());
            return "/addBook";
        }

    }

    @GetMapping("/viewReportedReviews")
    public String viewReportedReviews(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                                      @RequestParam(value = "size", required = false, defaultValue = "30") int size,
                                      Model model){

        model.addAttribute("reportedReviews", adminManager.displayReportedReviews(pageNumber, size));

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("pageNumber", pageNumber);

        return "viewReportedReviews";
    }

    @GetMapping("/viewReportedReviews/approve{username}/{title}")
    public String approveReview(@PathVariable(name = "username") String username,
                                @PathVariable(name = "title") String bookTitle,
                                RedirectAttributes ra){

        System.out.println(username + "\n" +bookTitle);
        try{
            adminManager.deleteReportedReview(username, bookTitle);
            System.out.println("review approved");
            ra.addAttribute("info", "approve");
            ra.addAttribute("infoMessage", "review approved");
        }catch (MongoException me){
            System.out.println(me.getMessage());
            ra.addAttribute("info", "error");
            ra.addAttribute("infoMessage", me.getMessage());
        }

        return "redirect:/viewReportedReviews";
    }

    @GetMapping("/viewReportedReviews/delete{username}/{title}")
    public String deleteReview(@PathVariable(name = "username") String username,
                                @PathVariable(name = "title") String bookTitle,
                                RedirectAttributes ra){

        System.out.println(username + "\n" +bookTitle);
        try{
            adminManager.deleteReview(username, bookTitle);
            System.out.println("review deleted");
            ra.addAttribute("info", "delete");
            ra.addAttribute("infoMessage", "review deleted");
        }catch (MongoException me){
            System.out.println(me.getMessage());
            ra.addAttribute("info", "error");
            ra.addAttribute("infoMessage", me.getMessage());
        } catch (ReviewException e) {
            System.out.println(e.getMessage());
            ra.addAttribute("info", "error");
            ra.addAttribute("infoMessage", e.getMessage());
        }

        return "redirect:/viewReportedReviews";
    }

    @RequestMapping(value = {"/analytics"}, method = RequestMethod.GET)
    public String viewAnalytics(Model model){

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        return "analytics";
    }

    @RequestMapping(value = {"/viewAnalytic"}, method = RequestMethod.POST)
    public String mostBorrowedBooks(@RequestParam(value = "startDate", required = false, defaultValue = "") String start,
                                    @RequestParam(value = "endDate", required = false, defaultValue = "") String end,
                                    @RequestParam(value = "startYear", required = false, defaultValue = "1950") int startYear,
                                    @RequestParam(value = "endYear", required = false, defaultValue = "2020") int endYear,
                                    @RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                                    @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                    @RequestParam(value = "score", required = false, defaultValue = "5") int score,
                                    @RequestParam(value = "numberAnalytic", required = false, defaultValue = "1") int numAnalytic,
                                    Model model){

        Integer startDate = DateConverter.toLong(start);
        Integer endDate = DateConverter.toLong(end);

        System.out.println(startDate);
        System.out.println(start);
        model.addAttribute("startDate", start);
        System.out.println(end);
        System.out.println(endDate);
        model.addAttribute("endDate", end);
        System.out.println(startYear);
        model.addAttribute("startYear", startYear);
        System.out.println(endYear);
        model.addAttribute("endYear", endYear);
        System.out.println(pageNumber);
        model.addAttribute("pageNumber", pageNumber);
        System.out.println(score);
        model.addAttribute("score", score);
        System.out.println(numAnalytic);
        model.addAttribute("numAnalytic", numAnalytic);


        switch (numAnalytic){
            case 1:
                model.addAttribute("results", adminManager.displayMostBorrowedBooksBasedOnTimeAndBirthYear(startYear, startDate, endYear , endDate, pageNumber, size));
                break;
            case 2:
                model.addAttribute("results", adminManager.displayAverageNumberOfBorrowedBooksPerUserNationalityInAPeriod(startDate, endDate, pageNumber, size));
                break;
            case 3:
                model.addAttribute("results", adminManager.displayUsersThatWroteTheMostLikedReviewsInATimePeriodWithAScore(startDate, endDate, score, pageNumber, size));
                break;
            case 4:
                model.addAttribute("results", adminManager.displayAverageNumberOfLikesPerReviewOfBooksInAPeriod(startDate, endDate, pageNumber, size));
                break;
            default:
                System.out.println("not an analytic");
                break;
        }

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("analytics", "ok");


        return "analytics";
    }

    @GetMapping("/changeStatus{username}")
    public String changeStatus(@PathVariable(name = "username") String username,
                               Model model){

        adminManager.modifyUserStatus(username);

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));

        return "redirect:/viewUsers";
    }







}
