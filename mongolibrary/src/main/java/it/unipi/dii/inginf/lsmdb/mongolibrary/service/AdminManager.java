package it.unipi.dii.inginf.lsmdb.mongolibrary.service;


import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.BookException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.ReviewException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.AdminMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.AnalyticObjectMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.ReportedReviewMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.ReviewMongo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.ArrayList;

public class AdminManager extends UserManager
{
    private static AdminManager instance = null; //SINGLETON PATTERN
    private AdminMongo myAdminData; //MANAGER HAS ONLY ONE ADMIN DATA

    public static AdminManager getInstance()
    {
        if(instance == null)
        {
            instance = new AdminManager();
        }
        return instance;
    }

    public AdminMongo getMyAdminData() {
        return myAdminData;
    }

    public void setMyAdminData(Document myAdminData)
    {
        this.myAdminData = new AdminMongo(myAdminData);
    }
    /*----------------------------------------Control functions-------------------------------------------------------*/

    /*----------------------------------------Display functions-------------------------------------------------------*/

    /**
     * Searches the reported reviews in the database and returns them as ReviewMongo.
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return ArrayList of ReviewMongo
     */
    public ArrayList<ReportedReviewMongo> displayReportedReviews(Integer pageNumber, Integer pageLength) throws MongoException
    {
        ArrayList<ReportedReviewMongo> fullReviews = new ArrayList<>();
        for(Document review : mongoConnectionManager.findAllReportedReviewsSorted(pageNumber, pageLength))
        {
            fullReviews.add(new ReportedReviewMongo(review));
        }
        return fullReviews;
    }

    /*----------------------------------------Add functions-----------------------------------------------------------*/

    /**
     * Adds the book the database.
     * @param book Document that represents the book
     */
    public void addBook(Document book) throws BookException, MongoException
    {

        if (mongoConnectionManager.findDocumentByKeyValue("books", "Title", book.getString("Title")).hasNext()) {
            throw new BookException("The book already exists"); //ERROR
        }
        mongoConnectionManager.addElement("books", book);
        boolean result = neo4JConnectionManager.addBook(book.getString("Title"), book.getString("Category"), book.getString("Author"), book.getInteger("publishDate"));
        if (!result) {
            //Rollback in MongoDB to remove the book
            mongoConnectionManager.removeElement("books", "Title", book.getString("Title"));
        }

    }

    /*----------------------------------------Generic functions-------------------------------------------------------*/

    /**
     * Changes the status of a specific book in the borrowing list of a user.
     * @param username name of the user that borrowed the book
     * @param bookTitle name of the book that has been borrowed
     * @param newStatus either "RETURNED" or "OVERDUE"
     */
    public void modifyBorrowingListBookStatus(String username, String bookTitle, String newStatus) throws MongoException
    {
        mongoConnectionManager.atomicModifyBorrowingListBookStatus(username, bookTitle, newStatus);
    }

    /**
     * Changes the status of a user from blocked to unblocked or viceversa.
     * @param username name of the user that we want to block or unblock
     * @param currentStatus status of the user -> TRUE or FALSE
     * @param newStatus new status -> TRUE or FALSE
     * @return if the operation is succesfull we return TRUE
     */
    public boolean modifyUserStatus(String username, Boolean currentStatus, Boolean newStatus) throws MongoException
    {
        Document user = mongoConnectionManager.findDocumentByKeyValue("customers", "username", username).next();
        if(user.getBoolean("status") == currentStatus)
        {
            mongoConnectionManager.updateOneDocumentByKeyValue("customers", "username", username, "status", newStatus);
            return true;
        }
        return false; //The user was already blocked/unblocked
    }

    /**
     * Deletes the review wrote by a user.
     * @param username name of the user that wrote the review
     * @param bookTitle name of the book that has the review
     */
    public void deleteReview(String username, String bookTitle) throws ReviewException, MongoException
    {
        String collection = "reviews";
        if(mongoConnectionManager.findDocumentByKeyValue(collection, "username", username, "bookTitle", bookTitle).hasNext())
        {
            mongoConnectionManager.atomicDeleteReview(collection, username, bookTitle);
        } else {
            throw new ReviewException("Review doesn't exist");
        }
    }

    /**
     * Deletes the review from reported list.
     * @param username name of the user that wrote the review
     * @param bookTitle name of the book that has the review
     */
    public void deleteReportedReview(String username, String bookTitle) throws MongoException
    {
        mongoConnectionManager.removeReportedReview(username, bookTitle);
    }

    /*----------------------------------------Analytics functions-----------------------------------------------------*/

    /**
     * Retrieves from the database the most borrowed books in a time period by people born in a certain interval of time
     * @param startYear smallest birthYear included
     * @param finalYear biggest birthYear included
     * @param startDate smallest borrowdate included
     * @param endDate biggest borrowdate included
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return Arraylist of AnalitycObjectMongo(key: String, value: String)
     */
    public ArrayList<AnalyticObjectMongo> displayMostBorrowedBooksBasedOnTimeAndBirthYear(Integer startYear, Integer finalYear, Integer startDate, Integer endDate, Integer pageNumber, Integer pageLength) throws MongoException
    {
        //Creates the arraylist that has to be returned
        ArrayList<AnalyticObjectMongo> convertedAnalytics = new ArrayList<>();

        //Conversion to generic analytic object
        for(Document analyticObject : mongoConnectionManager.mostBorrowedBooksBasedOnTimeAndBirthYear(startYear, finalYear, startDate, endDate, pageNumber, pageLength))
            convertedAnalytics.add(new AnalyticObjectMongo(analyticObject, "timesBorrowed"));

        //It will never be null
        return convertedAnalytics;
    }

    /**
     * Retrieves from the database the average number of borrowed books by each group of people based on nationality and a period
     * @param startDate smallest borrowdate included
     * @param endDate biggest borrowdate included
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return Arraylist of AnalitycObjectMongo(key: String, value: String)
     */
    public ArrayList<AnalyticObjectMongo> displayAverageNumberOfBorrowedBooksPerUserNationalityInAPeriod(Integer startDate, Integer endDate, Integer pageNumber, Integer pageLength) throws MongoException
    {
        //Creates the arraylist that has to be returned
        ArrayList<AnalyticObjectMongo> convertedAnalytics = new ArrayList<>();

        //Conversion to generic analytic object
        for(Document analyticObject : mongoConnectionManager.averageNumberOfBorrowedBooksPerUserNationalityInAPeriod(startDate, endDate,pageNumber, pageLength))
            convertedAnalytics.add(new AnalyticObjectMongo(analyticObject, "averageNumberOfBooksTaken"));

        //It will never be null
        return convertedAnalytics;
    }

    /**
     * Retrieves from the database the users who received the most likes on the reviews that they have written within a time period and a selected score
     * @param startDate smallest reviewTime included
     * @param endDate biggest reviewTime included
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return Arraylist of AnalitycObjectMongo(key: String, value: String)
     */
    public ArrayList<AnalyticObjectMongo> displayUsersThatWroteTheMostLikedReviewsInATimePeriodWithAScore(Integer startDate, Integer endDate, Integer score, Integer pageNumber, Integer pageLength) throws MongoException
    {
        //Creates the arraylist that has to be returned
        ArrayList<AnalyticObjectMongo> convertedAnalytics = new ArrayList<>();

        //Conversion to generic analytic object
        for(Document analyticObject : mongoConnectionManager.usersThatWroteTheMostLikedReviewsInATimePeriodWithAScore(startDate, endDate, score, pageNumber, pageLength))
            convertedAnalytics.add(new AnalyticObjectMongo(analyticObject, "numberOfLikesReceived"));

        //It will never be null
        return convertedAnalytics;
    }

    /**
     * Retrieves from the database the books with the highest average number of likes per review in a period
     * @param startDate smallest reviewTime included
     * @param endDate biggest reviewTime included
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return Arraylist of AnalitycObjectMongo(key: String, value: String)
     */
    public ArrayList<AnalyticObjectMongo> displayAverageNumberOfLikesPerReviewOfBooksInAPeriod(Integer startDate, Integer endDate, Integer pageNumber, Integer pageLength) throws MongoException
    {
        //Creates the arraylist that has to be returned
        ArrayList<AnalyticObjectMongo> convertedAnalytics = new ArrayList<>();

        //Conversion to generic analytic object
        for(Document analyticObject : mongoConnectionManager.averageNumberOfLikesPerReviewOfBooksInAPeriod(startDate, endDate, pageNumber, pageLength))
            convertedAnalytics.add(new AnalyticObjectMongo(analyticObject, "averageNumberOfLikes"));

        //It will never be null
        return convertedAnalytics;
    }

}
