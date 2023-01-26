package it.unipi.dii.inginf.lsmdb.mongolibrary.service;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;

import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.BookException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.ReviewException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.BookMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.BorrowingListBookMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.CustomerMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.ReviewMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.DateConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.neo4j.driver.Record;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerManager extends UserManager
{
    private static CustomerManager instance = null; //SINGLETON PATTERN
    private CustomerMongo myCustomerData;

    public static CustomerManager getInstance()
    {
        if(instance == null)
        {
            //LOGGER.info("getInstance() | Creating a new CustomerManager instance"); //LOG
            instance = new CustomerManager();
        }
        //LOGGER.info("getInstance() | Returning the CustomerManager instance"); //LOG
        return instance;
    }

    public CustomerMongo getMyCustomerData() {
        return myCustomerData;
    }

    public void setMyCustomerData(Document myCustomerData)
    {
        //Saves all data in the structure
        this.myCustomerData = new CustomerMongo(myCustomerData);
        //To avoid any waste of memory we empty the Lists, since we retrieve them from the database when necessary
        this.myCustomerData.setReadingList(new ArrayList<>());
        this.myCustomerData.setBorrowingList(new ArrayList<>());
        //Sets books to OVERDUE if necessary
        updateBorrowingList();
    }

    /*----------------------------------------Control functions-------------------------------------------------------*/

    /**
     * Checks if the user has the specified book in the reading list.
     * @param bookTitle name to check in the database
     * @return TRUE if user already has it, FALSE otherwise
     */
    private boolean hasBookInReadingList(String bookTitle) throws MongoException
    {
        return getReadingListTitles(myCustomerData.getUsername()).contains(bookTitle);
    }

    /**
     * Checks if the user has the specified book in the borrowing list.
     * @param bookTitle name to check in the database
     * @return TRUE if user already has it, FALSE otherwise
     */
    private boolean hasBookInBorrowingList(String bookTitle) throws MongoException
    {
        return getBorrowingListTitles(myCustomerData.getUsername()).contains(bookTitle);
    }

    /**
     * Checks and updates all the books that have a return date smaller than the current date and havev't been returned.
     */
    private void updateBorrowingList() throws MongoException
    {
        ArrayList<String> overdueBooks = new ArrayList<>();
        for(BorrowingListBookMongo borrowedBook : getDetailedBorrowingList(myCustomerData.getUsername())){
            if((borrowedBook.getReturnDate().isBefore(LocalDate.now())) && (borrowedBook.getStatus().equals("BORROWED")))
                overdueBooks.add(borrowedBook.getBookTitle());
        }
        for(String bookTitle : overdueBooks)
        {
            mongoConnectionManager.updateElementOfList("customers","username", myCustomerData.getUsername(), "borrowingList.booktitle", bookTitle,"borrowingList.$.status", "OVERDUE");
        }
    }

    /**
     * Checks if the user has any OVERDUE book in the borrowing list.
     * @return TRUE if user has it, FALSE otherwise
     */
    private boolean hasOverdueBookInBorrowingList() throws MongoException
    {
        ArrayList<BorrowingListBookMongo> borrowingList = getDetailedBorrowingList(myCustomerData.getUsername());
        for(BorrowingListBookMongo book : borrowingList)
            if(book.getStatus().equals("OVERDUE"))
                return true;
        return false;
    }

    /**
     * Checks if the user has written a review of the book.
     * @param bookTitle name to check in the database
     * @return TRUE if user already has, FALSE otherwise
     */
    private boolean hasWroteAreview(String bookTitle) throws MongoException
    {
        return (mongoConnectionManager.findDocumentByKeyValue("reviews", "username", myCustomerData.getUsername(), "bookTitle", bookTitle).hasNext());
    }

    /**
     * Checks if the book has any copy left.
     * @param bookTitle name to check in the database
     * @return TRUE has at least one copy, FALSE otherwise
     */
    private boolean areCopiesFinished(String bookTitle) throws MongoException
    {
        return (mongoConnectionManager.findDocumentByKeyValue("books", "Title", bookTitle).next().getInteger("copies") == 0);
    }

    private boolean hasReported(String username, String bookTitle) throws MongoException
    {
        return mongoConnectionManager.findReportedReview(username, bookTitle, getMyCustomerData().getUsername()).hasNext();
    }

    /*----------------------------------------Display functions-------------------------------------------------------*/

    /*----------------------------------------Add functions-----------------------------------------------------------*/
    /**
     * Adds the book to the current user reading list.
     * @param bookTitle name of the book
     */
    public void addBookToReadingList(String bookTitle) throws BookException, MongoException
    {
        if(hasBookInReadingList(bookTitle)){
            throw new BookException("The book is already in your reading list.");
        }
        else
        {
            mongoConnectionManager.addElementToList("customers", "username", myCustomerData.getUsername(), "readingList", bookTitle);
            var result = neo4JConnectionManager.addToReading(myCustomerData.getUsername(), bookTitle);
            if(!result) {
                //Rollback in MongoDB to remove it from the reading list
                mongoConnectionManager.removeElementFromList("customers", "username", myCustomerData.getUsername(), "readingList", bookTitle);
            }
        }
    }

    /**
     * Adds the book to the current user borrowing list.
     * @param bookTitle name of the book
     */
    public void addBookToBorrowingList(String bookTitle) throws BookException, MongoException
    {
        if(hasOverdueBookInBorrowingList()){
            throw new BookException("Before borrowing new books you need to return all the overdue books!");
        }
        else if(hasBookInBorrowingList(bookTitle)) {
            throw new BookException("The book is already in your borrowing list.");
        }
        else if(areCopiesFinished(bookTitle)) {
            throw new BookException("The book has no copies available.");
        }
        else
        {
            removeFromReadingList(bookTitle, Boolean.FALSE);
            Document borrowedBook = new Document().append("booktitle",bookTitle)
                    .append("borrowdate", DateConverter.toLong(LocalDate.now().toString()))
                    .append("returndate", DateConverter.toLong(LocalDate.now().plusMonths(1).toString()))
                    .append("status", "BORROWED");
            //We leave the newcopies as an int in case of rollback
            int newcopies = mongoConnectionManager.atomicAddBookToBorrowingList(myCustomerData.getUsername(), bookTitle, borrowedBook);
            var result = neo4JConnectionManager.borrowBook(myCustomerData.getUsername(), bookTitle);
            if (!result) {
                //Since this is a rollback we expected not to have errors and therefore we can do all of these operations divided
                //Rollback in MongoDB and Neo4j to add back both in mongoDB and Neo4j the book to the reading list
                mongoConnectionManager.addElementToList("customers", "username", myCustomerData.getUsername(), "readingList", bookTitle);
                neo4JConnectionManager.addToReading(myCustomerData.getUsername(), bookTitle);
                //Rollback in MongoDB to remove the book added and update the copies count
                mongoConnectionManager.removeElementFromList("customers", "username", myCustomerData.getUsername(), "borrowingList", borrowedBook);
                mongoConnectionManager.updateOneDocumentByKeyValue("books", "Title", bookTitle, "copies", (newcopies + 1));
            }
        }
    }

    /**
     * Adds a review to a book in the borrowing list of the user.
     * @param bookTitle name of the book
     * @param review whole document to add to the collection of reviews
     */
    public void addReview(String bookTitle, Document review) throws ReviewException, MongoException
    {
        if (hasWroteAreview(bookTitle)){
            throw new ReviewException("You already wrote a review for this book!");
        } else if(hasBookInBorrowingList(bookTitle)){
            //If we add a review we need to evaluate the new score of the book afterwards and update the embedded structure of the book
            mongoConnectionManager.atomicAddReview(bookTitle, review);
        } else {
            throw new ReviewException("You can't review the book, if you haven't read it.");
        }
    }

    /**
     * Adds or removes a like to a review.
     * @param username name of the user who wrote the review
     * @param bookTitle name of the book
     * @return TRUE if user adds the like, FALSE if user removes it
     */
    public boolean addOrRemoveLikeToReview(String username, String bookTitle) throws MongoException
    {
        ArrayList<String> nameList = mongoConnectionManager.findDocumentByKeyValue("reviews", "username", username, "bookTitle", bookTitle).next().get("likes", ArrayList.class);
        if(nameList.contains(myCustomerData.getUsername())){
            mongoConnectionManager.removeElementFromList("reviews", "username", username, "bookTitle", bookTitle, "likes", myCustomerData.getUsername());
            return false;
        }
        mongoConnectionManager.addElementToList("reviews", "username", username, "bookTitle", bookTitle, "likes", myCustomerData.getUsername());
        return true;
    }

    /**
     *
     * @param currentUser
     * @param userTarget
     */

    public void addToFollowed(String currentUser, String userTarget)
    {
        neo4JConnectionManager.addToFollowed(currentUser, userTarget);
    }

    /*----------------------------------------Generic functions-------------------------------------------------------*/

    /**
     * Adds or removes a book from the reading list
     * @param bookTitle name of the book
     * @param throwException if the function will throw or not an exception
     */
    public void removeFromReadingList(String bookTitle, Boolean throwException) throws BookException, MongoException
    {
        if(hasBookInReadingList(bookTitle)) {
            mongoConnectionManager.removeElementFromList("customers", "username", myCustomerData.getUsername(), "readingList", bookTitle);
            var result = neo4JConnectionManager.removeFromReading(myCustomerData.getUsername(), bookTitle);
            if (!result) {
                //Rollback in MongoDB to add book to reading list
                mongoConnectionManager.addElementToList("customers", "username", myCustomerData.getUsername(), "readingList", bookTitle);
            }
        } else if (throwException){
            throw new BookException("The book is not in your reading list.");
        }
    }

    /**
     * Reports the review of a user.
     * @param username name of the user who wrote the review
     * @param bookTitle name of the book
     */
    public void reportReview(String username, String bookTitle, String description) throws MongoException
    {
        if(hasReported(username, bookTitle)) {
            return; //returns without doing anything
        } else if(mongoConnectionManager.findReportedReview(username, bookTitle).hasNext()){
            //If review has already been reported at least once and the user has not reported it yet, we add the username to the list
            mongoConnectionManager.addNameToReportedReview(username, bookTitle, getMyCustomerData().getUsername());
        } else {
            ArrayList<String> user = new ArrayList<>();
            user.add(getMyCustomerData().getUsername());
            Document reportedReview = new Document()
                    .append("username", username)
                    .append("bookTitle", bookTitle)
                    .append("description", description)
                    .append("reportedBy", user);
            //If the review doesn't exist it creates it and the adds the name
            mongoConnectionManager.addReportedReview(reportedReview);
        }
    }

    /*----------------------------------------Suggestion functions-------------------------------------------------------*/

    /**
     * N4J
     * Function to return the books suggested to the user based on the borrowing list of the followed users
     * @param username Username of the currently logged user
     * @return List of String containing the books suggested
     */
    public List<String> showBooksSuggestion(String username)
    {
        List<String> books = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.suggestBookByFollows(username);
        for (var user : records) {
            books.add(String.valueOf(user.get(0)));
        }
        return books;
    }

    /**
     * N4J
     * Function to return the books suggested to the user based on the reading list of the followed users
     * @param username Username of the currently logged user
     * @return List of String containing the books suggested
     */
    public List<String> showBooksSuggestionReading(String username)
    {
        List<String> books = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.suggestBookByReadingList(username);
        for (var user : records) {
            books.add(String.valueOf(user.get(0)));
        }
        return books;
    }

    /**
     * N4J
     * Function to return the books suggested to the user based on the borrowing list of the followed users
     * @param username Username of the currently logged user
     * @param time Current time in unix timestamp
     * @return List of String containing the books suggested
     */
    public List<String> showBooksSuggestionTime(String username, Integer time)
    {
        List<String> books = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.suggestBookByFollowRecent(username, time-2592000, time);
        for (var user : records) {
            books.add(String.valueOf(user.get(0)));
        }
        return books;
    }

    /**
     * N4J
     * Function to return the users suggested to the user based on the books read
     * @param username Username of the currently logged user
     * @return List of String containing the users suggested
     */
    public List<String> showUsersSuggestion(String username)
    {
        List<String> users = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.suggestUserByBooks(username);
        for (var user : records) {
            users.add(String.valueOf(user.get(0)));
        }
        return users;
    }

    /**
     * N4J
     * Function to return the users suggested to the user based on the book read in the last 30 days
     * @param username Username of the currently logged user
     * @param time Current time in unix timestamp
     * @return List of String containing the users suggested
     */
    public List<String> showUsersSuggestionTime(String username, int time)
    {
        List<String> users = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.suggestUserByBooksRecent(username, time-2592000, time);
        for (var user : records) {
            users.add(String.valueOf(user.get(0)));
        }
        return users;
    }

    /**
     * N4J
     * Function to return the users suggested to the user based on the book in the reading list
     * @param username Username of the currently logged user
     * @return List of String containing the users suggested
     */
    public List<String> showUsersSuggestionReading(String username)
    {
        System.out.println("DIO PUTTANA");
        List<String> users = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.suggestUserByReadingList(username);
        for (var user : records) {
            System.out.println("DIOCANE " + user.get(0));
            users.add(String.valueOf(user.get(0)));
        }
        return users;
    }
}

    /*
    private static final Logger LOGGER = LogManager.getLogger(CustomerManager.class); //LOGGER
    private static CustomerManager instance = null; //SINGLETON PATTERN
    private CustomerMongo myCustomerData;

    public static CustomerManager getInstance()
    {
        if(instance == null)
        {
            LOGGER.info("getInstance() | Creating a new CustomerManager instance"); //LOG
            instance = new CustomerManager();
        }
        LOGGER.info("getInstance() | Returning the CustomerManager instance"); //LOG
        return instance;
    }

    public CustomerMongo getMyCustomerData() {
        return myCustomerData;
    }

    public void setMyCustomerData(Document myCustomerData)
    {
        //Saves all data in the structure
        this.myCustomerData = new CustomerMongo(myCustomerData);
        //Sets books to OVERDUE
        updateBorrowingList();
    }

    /*----------------------------------------Control functions-------------------------------------------------------

    public boolean hasBookInReadingList(String title)
    {
        return getReadingListTitles(myCustomerData.getUsername()).contains(title);
    }

    public boolean hasBookInBorrowingList(String title)
    {
        return getBorrowingListTitles(myCustomerData.getUsername()).contains(title);
    }

    public void updateBorrowingList()
    {
        ArrayList<String> overdueBooks = new ArrayList<>();
        for(BorrowingListBookMongo borrowedBook : getDetailedBorrowingList(myCustomerData.getUsername())){
            if((borrowedBook.getReturnDate().isBefore(LocalDate.now())) && (borrowedBook.getStatus().equals("BORROWED")))
                overdueBooks.add(borrowedBook.getBookTitle());
        }
        for(String bookTitle : overdueBooks)
        {
            mongoConnectionManager.updateElementOfList("customers","username", myCustomerData.getUsername(), "borrowingList.booktitle", bookTitle,"borrowingList.$.status", "OVERDUE");
        }
    }

    public boolean hasOverdueBookInBorrowingList()
    {
        ArrayList<BorrowingListBookMongo> borrowingList = mongoConnectionManager.findDocumentByKeyValue("customers", "username", myCustomerData.getUsername()).next().get("borrowingList", ArrayList.class);
        for(BorrowingListBookMongo book : borrowingList)
            if(book.getStatus().equals("OVERDUE"))
                return true;
        return false;
    }

    public boolean hasWroteAreview(String bookTitle)
    {
        return (mongoConnectionManager.findDocumentByKeyValue("reviews", "username", myCustomerData.getUsername(), "bookTitle", bookTitle).hasNext());
    }

    public boolean areCopiesFinished(String bookTitle)
    {
        return (mongoConnectionManager.findDocumentByKeyValue("books", "Title", bookTitle).next().getInteger("copies") == 0);
    }

    /*----------------------------------------Display functions-------------------------------------------------------

    /*----------------------------------------Add functions-----------------------------------------------------------

    public void addBookToReadingList(String bookTitle) throws BookException
    {
        if(hasBookInReadingList(bookTitle)) {
            throw new BookException("The book is already in your reading list.");
        }
        else
        {
            mongoConnectionManager.addElementToList("customers", "username", myCustomerData.getUsername(), "readingList", bookTitle);
        }
    }

    public void addBookToBorrowingList(String bookTitle) throws BookException
    {
        if(hasOverdueBookInBorrowingList()){
            throw new BookException("Before borrowing new books you need to return all the overdue books!");
        }
        else if(hasBookInBorrowingList(bookTitle)) {
            throw new BookException("The book is already in your borrowing list.");
        }
        else if(areCopiesFinished(bookTitle)) {
            throw new BookException("The book has no copies available.");
        }
        else
        {
            removeFromReadingList(bookTitle, Boolean.FALSE);
            Document borrowedBook = new Document().append("booktitle",bookTitle)
                    .append("borrowdate", DateConverter.toLong(LocalDate.now().toString()))
                    .append("returndate", DateConverter.toLong(LocalDate.now().plusMonths(1).toString()))
                    .append("status", "BORROWED");
            int newcopies = mongoConnectionManager.findDocumentByKeyValue("books", "Title", bookTitle).next().getInteger("copies") - 1;
            mongoConnectionManager.addElementToList("customers", "username", myCustomerData.getUsername(), "borrowingList", borrowedBook);
            mongoConnectionManager.updateOneDocumentByKeyValue("books", "Title", bookTitle, "copies", newcopies);
        }
    }

    public void addReview(String bookTitle, Document review) throws ReviewException
    {
        if (hasWroteAreview(bookTitle)){
            throw new ReviewException("You already wrote a review for this book!");
        } else if(hasBookInBorrowingList(bookTitle)){
            //If we add a review we need to evaluate the new score of the book afterwards
            mongoConnectionManager.addElement("reviews", review);
            calculateScore(bookTitle);
        } else {
            throw new ReviewException("You can't review the book, if you haven't read it.");
        }
    }

    public void addLikeToReview(String username, String bookTitle) throws ReviewException
    {
        ArrayList<String> nameList = mongoConnectionManager.findDocumentByKeyValue("reviews", "username", username, "bookTitle", bookTitle).next().get("likes", ArrayList.class);
        if(nameList.contains(username)){
            throw new ReviewException("You have already liked this review");
        }
        mongoConnectionManager.addElementToList("reviews", "username", username, "bookTitle", bookTitle, "likes", myCustomerData.getUsername());
    }

    /*----------------------------------------Generic functions-------------------------------------------------------

    public void removeFromReadingList(String bookTitle, Boolean throwException) throws BookException
    {
        if(hasBookInReadingList(bookTitle)) {
            mongoConnectionManager.removeElementFromList("customers", "username", myCustomerData.getUsername(), "readingList", bookTitle);
        } else if (throwException){
            throw new BookException("The book is not in your reading list.");
        }
    }

    public void reportReview(String username, String bookTitle)
    {
        Document reportedReview = new Document().append("username", username).append("bookTitle", bookTitle);
        if(mongoConnectionManager.findEmbeddedDocumentByKeyValue("admins", "reportedReviews", reportedReview).hasNext())
            return; //Returns without doing anything
        mongoConnectionManager.addElementToList("admins", "username","admin", "reportedReviews", reportedReview);
    }
}

*/
