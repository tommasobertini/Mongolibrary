package it.unipi.dii.inginf.lsmdb.mongolibrary.controller;

import it.unipi.dii.inginf.lsmdb.mongolibrary.MongolibraryApplication;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.BookException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.ReviewException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.model.Book;
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
                                      @RequestParam(value = "size", required = false, defaultValue = "50") int size,
                                      Model model){

        model.addAttribute("reportedReviews", adminManager.displayReportedReviews(pageNumber, size));

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));

        return "viewReportedReviews";
    }

    @RequestMapping(value = {"/analytics"}, method = RequestMethod.GET)
    public String viewAnalytics(Model model){

        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        return "analytics";
    }

    @RequestMapping(value = {"/viewAnalytic"}, method = RequestMethod.POST)
    public String mostBorrowedBooks(@RequestParam(value = "start", required = false, defaultValue = "") String start,
                                    @RequestParam(value = "end", required = false, defaultValue = "") String end,
                                    @RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                                    @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                    @RequestParam(value = "score", required = false, defaultValue = "5") int score,
                                    @RequestParam(value = "numberAnalytic", required = false, defaultValue = "1") int numAnalytic,
                                    Model model){

        //handler pagination
        String s = (String)customBean.getBean(Constants.START_DATE);
        String e = (String)customBean.getBean(Constants.END_DATE);
        if (s==null && e==null){
            customBean.putBean(Constants.START_DATE, start);
            customBean.putBean(Constants.END_DATE, end);
            customBean.putBean(Constants.NUM_ANALYTIC, numAnalytic);
            System.out.println("parametri inseriti");
        }

        if(start.equals("")){
            start = (String) customBean.getBean(Constants.START_DATE);
            end = (String) customBean.getBean(Constants.END_DATE);
            numAnalytic = (int) customBean.getBean(Constants.NUM_ANALYTIC);
            System.out.println("update");
        }else{
            customBean.replaceBean(Constants.START_DATE, start);
            customBean.replaceBean(Constants.END_DATE, end);
            customBean.replaceBean(Constants.NUM_ANALYTIC, numAnalytic);
        }

        System.out.println("start: " + start);

        Integer startYear = LocalDate.parse(start).getYear();
        Integer startDate = (int) LocalDate.parse(start).toEpochSecond(LocalTime.NOON, ZoneOffset.UTC);
        Integer endYear = LocalDate.parse(end).getYear();
        Integer endDate = (int) LocalDate.parse(end).toEpochSecond(LocalTime.NOON, ZoneOffset.UTC);

        switch (numAnalytic){
            case 1:
                model.addAttribute("results", adminManager.displayMostBorrowedBooksBasedOnTimeAndBirthYear(startYear, startDate, endYear, endDate, pageNumber, size));
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


        System.out.println(start);
        System.out.println(end);
        System.out.println("numAnalytic: " + numAnalytic);
        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("analytics", "ok");
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("score", score);
        model.addAttribute("numAnalytic", numAnalytic);

        return "analytics";
    }

    /*
    @RequestMapping(value = {"/mostBorrowed"}, method = RequestMethod.POST)
    public String mostBorrowedBooks(@RequestParam(value = "start", required = false, defaultValue = "") String start,
                                    @RequestParam(value = "end", required = false, defaultValue = "") String end,
                                    @RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                                    @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                    //@PathVariable(value)
                                    Model model){

        //handler pagination
        String s = (String)customBean.getBean(Constants.START_DATE);
        String e = (String)customBean.getBean(Constants.END_DATE);
        if (s==null && e==null){
            customBean.putBean(Constants.START_DATE, start);
            customBean.putBean(Constants.END_DATE, end);
            System.out.println("parametri inseriti");
        }

        if(start.equals("")){
            start = (String) customBean.getBean(Constants.START_DATE);
            end = (String) customBean.getBean(Constants.END_DATE);
            System.out.println("update");
        }else{
            customBean.replaceBean(Constants.START_DATE, start);
            customBean.replaceBean(Constants.END_DATE, end);
        }

        System.out.println("start: " + start);

        Integer startYear = LocalDate.parse(start).getYear();
        Integer startDate = (int) LocalDate.parse(start).toEpochSecond(LocalTime.NOON, ZoneOffset.UTC);
        Integer endYear = LocalDate.parse(end).getYear();
        Integer endDate = (int) LocalDate.parse(end).toEpochSecond(LocalTime.NOON, ZoneOffset.UTC);


        model.addAttribute("results", adminManager.displayMostBorrowedBooksBasedOnTimeAndBirthYear(startYear, startDate, endYear, endDate, pageNumber, size));
        System.out.println(start);
        System.out.println(end);
        model.addAttribute("userClass", customBean.getBean(Constants.SESSION_USER_CLASS));
        model.addAttribute("sessionUsername", customBean.getBean(Constants.SESSION_USERNAME));
        model.addAttribute("analytics", "ok");
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("start", start);
        model.addAttribute("end", end);

        return "analytics";
    }
     */






}
