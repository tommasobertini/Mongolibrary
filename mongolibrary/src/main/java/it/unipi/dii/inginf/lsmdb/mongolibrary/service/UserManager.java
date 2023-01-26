package it.unipi.dii.inginf.lsmdb.mongolibrary.service;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.mongolibrary.model.Customer;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.BookMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.BorrowingListBookMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.CustomerMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.ReviewMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.CustomBean;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.DateConverter;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.pagination.Page;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.pagination.Paged;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.pagination.Paging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.neo4j.driver.Record;

public class UserManager {

    protected final MongoConnectionManager mongoConnectionManager;
    protected final Neo4JConnectionManager neo4JConnectionManager;
    private static final Logger LOGGER = LogManager.getLogger(UserManager.class); //LOGGER

    public UserManager(){
        mongoConnectionManager = MongoConnectionManager.getInstance();
        neo4JConnectionManager = Neo4JConnectionManager.getInstance();
    }

    /*----------------------------------------Control functions-------------------------------------------------------*/

    /**
     * Retrieve from the database the borrowing list of the specified user.
     * @param username name used to search the borrowing list
     * @return ArrayList of documents: booktitle: String, borrowdate: int, returndate: int, status: String
     */
    private ArrayList<Document> getBorrowingListAsDocuments(String username) throws MongoException
    {
        return mongoConnectionManager.findDocumentByKeyValue("customers", "username", username).next()
                .get("borrowingList", ArrayList.class);
    }

    /**
     * Retrieve from the database the reading list of the specified user.
     * @param username name used to search the reading list
     * @return ArrayList of Strings
     */
    public ArrayList<String> getReadingListTitles(String username) throws MongoException
    {
        return mongoConnectionManager.findDocumentByKeyValue("customers", "username", username).next()
                .get("readingList", ArrayList.class);
    }

    /**
     * Retrieve from the database the title of the borrowing list of the specified user.
     * @param username name used to search the borrowing list
     * @return ArrayList of Strings
     */
    public ArrayList<String> getBorrowingListTitles(String username) throws MongoException
    {
        ArrayList<String> bookTitles = new ArrayList<>();

        for(Document book : getBorrowingListAsDocuments(username))
            bookTitles.add(book.getString("booktitle"));

        return bookTitles;
    }

    /**
     * Searches the borrowing list of the user in the database and returns it as an array of BorrowingListBookMongo.
     * @param username name used to search the borrowing list
     * @return ArrayList of BorrowingListBookMongo
     */
    public ArrayList<BorrowingListBookMongo> getDetailedBorrowingList(String username) throws MongoException
    {
        ArrayList<BorrowingListBookMongo> borrowedBooks = new ArrayList<>();

        for(Document borrowedSelected : getBorrowingListAsDocuments(username))
            borrowedBooks.add(new BorrowingListBookMongo(borrowedSelected));

        return borrowedBooks;
    }

    /*----------------------------------------Display functions-------------------------------------------------------*/

    /**
     * Returns a finite number of users sorted based on a parameter.
     * @param key parameter on which we sort
     * @param value the string to search into the db
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @param order either ascending or descending: -1 or 1
     * @return ArrayList of CustomerMongo
     */
    public ArrayList<CustomerMongo> displayUsersSorted(String key, String value, Integer pageNumber, Integer pageLength, Integer order) throws MongoException
    {
        String collection = "customers";
        ArrayList<CustomerMongo> customersFound = new ArrayList<>();

        MongoCursor<Document> cursor = mongoConnectionManager.findDocumentsOrderedByOneKeyValue(collection, key, value, pageNumber, pageLength, order);

        while(cursor.hasNext())
            customersFound.add(new CustomerMongo(cursor.next()));

        return customersFound;
    }

    /**
     * Returns a finite number of users sorted by key
     * @param key parameter on which we sort
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @param order either ascending or descending: -1 or 1
     * @return ArrayList of CustomerMongo
     */
    public ArrayList<CustomerMongo> displayUsersSorted(String key, Integer pageNumber, Integer pageLength, Integer order) throws MongoException
    {
        String collection = "customers";
        ArrayList<CustomerMongo> customersFound = new ArrayList<>();

        MongoCursor<Document> cursor = mongoConnectionManager.findDocumentsOrderedByOneKeyValue(collection, key, pageNumber, pageLength, order);

        while(cursor.hasNext())
            customersFound.add(new CustomerMongo(cursor.next()));

        return customersFound;
    }

    /**
     * Returns all the users based on a parameter.
     * @param key name of the key in the document
     * @param value value to be equal to
     * @return ArrayList of CustomerMongo
     */
    public ArrayList<CustomerMongo> displayUsersBasedOnAParameter(String key, String value) throws MongoException
    {
        String collection = "customers";
        ArrayList<CustomerMongo> customersFound = new ArrayList<>();

        MongoCursor<Document> cursor = mongoConnectionManager.findDocumentByKeyValue(collection, key, value);

        while(cursor.hasNext())
            customersFound.add(new CustomerMongo(cursor.next()));

        return customersFound;
    }

    /**
     * Retrieve from the database the infomrmations of a user.
     * @param username name used to retrieve the document values
     * @return Searched user details
     */
    public CustomerMongo displayUser(String username) throws MongoException
    //We can display the user data
    {
        return new CustomerMongo(mongoConnectionManager.findDocumentByKeyValue("customers", "username", username).next());
    }

    /**
     * Returns all the books in the list of a user.
     * @param list name of the list of all the books
     * @return ArrayList of BookMongo
     */
    public ArrayList<BookMongo> displayBooksList(ArrayList<String> list) throws MongoException
    //We can display both reading list and borrowing list as normal books
    {
        ArrayList<BookMongo> books = new ArrayList<>();

        for(String bookTitle : list){
            books.add(new BookMongo(mongoConnectionManager.findDocumentByKeyValue("books", "Title", bookTitle).next()));
        }

        return books;
    }

    /**
     * Returns a finite number of books sorted based on a parameter.
     * @param key parameter on which we sort
     * @param value the string to search into the db
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @param order either ascending or descending: -1 or 1
     * @return ArrayList of BookMongo
     */
    public ArrayList<BookMongo> displayBooksSorted(String key, String value, Integer pageNumber, Integer pageLength, Integer order) throws MongoException
    {
        String collection = "books";
        ArrayList<BookMongo> booksFound = new ArrayList<>();

        MongoCursor<Document> cursor = mongoConnectionManager.findDocumentsOrderedByOneKeyValue(collection, key, value, pageNumber, pageLength, order);

        while(cursor.hasNext())
            booksFound.add(new BookMongo(cursor.next()));

        return booksFound;
    }

    /**
     * Returns a finite number of books sorted by key
     * @param key parameter on which we sort
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @param order either ascending or descending: -1 or 1
     * @return ArrayList of BookMongo
     */
    public ArrayList<BookMongo> displayBooksSorted(String key, Integer pageNumber, Integer pageLength, Integer order) throws MongoException
    {
        String collection = "books";
        ArrayList<BookMongo> booksFound = new ArrayList<>();

        MongoCursor<Document> cursor = mongoConnectionManager.findDocumentsOrderedByOneKeyValue(collection, key, pageNumber, pageLength, order);

        while(cursor.hasNext())
            booksFound.add(new BookMongo(cursor.next()));

        return booksFound;
    }

    /**
     * Returns a finite number of books sorted by key
     * @param key parameter on which we sort
     * @param value any author or publisher
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return ArrayList of BookMongo
     */
    public ArrayList<BookMongo> displayBooksSortedByScore(String key, String value, Integer pageNumber, Integer pageLength) throws MongoException
    {
        ArrayList<BookMongo> booksFound = new ArrayList<>();

        for(Document book : mongoConnectionManager.highestRatedBooksBasedOnKeyValue(key, value,pageNumber, pageLength))
            booksFound.add(new BookMongo(book));

        return booksFound;
    }

    /**
     * Returns all the books based on a parameter.
     * @param key name of the key in the document
     * @param value value to be equal to
     * @return ArrayList of BookMongo
     */
    public ArrayList<BookMongo> displayBooksBasedOnAParameter(String key, String value) throws MongoException
    {
        String collection = "books";
        ArrayList<BookMongo> booksFound = new ArrayList<>();

        MongoCursor<Document> cursor = mongoConnectionManager.findDocumentByKeyValue(collection, key, value);

        while(cursor.hasNext())
            booksFound.add(new BookMongo(cursor.next()));

        return booksFound;
    }

    /**
     * Retrieve from the database the infomrmations of a book and the most recent 50 reviews.
     * @param bookTitle name used to retrieve the document values
     * @return Searched book details
     */
    public BookMongo displayBook(String bookTitle) throws MongoException
    {
        return new BookMongo(mongoConnectionManager.findDocumentByKeyValue("books", "Title", bookTitle).next());
    }

    /**
     * Returns all the reviews of a book.
     * @param bookTitle name of the book that has the reviews
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @param order either ascending or descending: -1 or 1
     * @return ArrayList of ReviewMongo
     */
    public ArrayList<ReviewMongo> displayReviewsList(String bookTitle, Integer pageNumber, Integer pageLength, Integer order) throws MongoException
    {
        String collection = "reviews";
        String key = "bookTitle";
        ArrayList<ReviewMongo> reviewsFound = new ArrayList<>();

        MongoCursor<Document> associatedReviews = mongoConnectionManager.findDocumentsOrderedByOneKeyValue(collection, key, bookTitle, pageNumber, pageLength, order);
        while(associatedReviews.hasNext())
            reviewsFound.add(new ReviewMongo(associatedReviews.next()));
        return reviewsFound;
    }

    /**
     * Returns all the reviews of a book
     * @param bookTitle name of the book that has the reviews
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @param order either ascending or descending: -1 or 1
     * @return ArrayList of ReviewMongo
     */
    public ArrayList<ReviewMongo> displayReviewsList(String bookTitle, String orderingKey, Integer pageNumber, Integer pageLength, Integer order) throws MongoException
    {
        String collection = "reviews";
        String key = "bookTitle";
        ArrayList<ReviewMongo> reviewsFound = new ArrayList<>();

        MongoCursor<Document> associatedReviews = mongoConnectionManager.findDocumentsByOneKeyValueAndOrderBySecondKey(collection, key, bookTitle, orderingKey,pageNumber, pageLength, order);
        while(associatedReviews.hasNext())
            reviewsFound.add(new ReviewMongo(associatedReviews.next()));
        return reviewsFound;
    }

    /**
     * N4J
     * Return a list of all the followed users
     * @param username username of the currently logged user
     * @return List of String
     */
    public List<String> displayFollows(String username)
    {
        List<String> follows = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.showFollowed(username);
        for (var user : records) {
            follows.add(String.valueOf(user.get(0)));
        }

        for (int i=0; i<follows.size(); i++){
            String x = follows.get(i).replaceAll("\"", "");
            follows.set(i, x);
        }

        return follows;
    }

    /**
     * N4J
     * Return a list of all the books in the reading list
     * @param username username of the currently logged user
     * @return List of String
     */
    public List<String> displayReadingList(String username)
    {
        List<String> books = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.showReadingList(username);
        for (var book: records) {
            books.add(String.valueOf(book.get(0)));
        }
        return books;
    }

    /**
     * N4J
     * Return a list of all the books in the borrowing list
     * @param username username of the currently logged user
     * @return List of String
     */
    public List<String> displayBorrowingList(String username)
    {
        List<String> books = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.showBorrowingList(username);
        for (var book: records) {
            books.add(String.valueOf(book.get(0)));
        }
        return books;
    }

    /*----------------------------------------Add functions-----------------------------------------------------------*/

    /*----------------------------------------Generic functions-------------------------------------------------------*/

    /**
     * N4J
     * Function to return the most borrowed books of all time
     * @return List of String containing titles of the books
     */
    public List<String> displayBestBook() {
        List<String> books = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.bestBooks();
        for (var book: records) {
            books.add(String.valueOf(book.get(0)));
        }
        return books;
    }

    /**
     * N4J
     * Function to return the most borrowed books of all time of a specific genre
     * @param genre Genre that the user selected
     * @return List of String containing titles of the books
     */
    public List<String> displayBestBookByGenre(String genre) {
        List<String> books = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.bestBooksByGenre(genre);
        for (var book: records) {
            books.add(String.valueOf(book.get(0)));
        }
        return books;
    }

    /**
     * N4J
     * Function to return the most borrowed books in the last 30 days
     * @param time current time in unix timestamp
     * @return List of String containing titles of the books
     */
    public List<String> displayBestBookRecent(Integer time) {
        List<String> books = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.bestBooksLastMonth(time-2592000, time);
        for (var book: records) {
            books.add(String.valueOf(book.get(0)));
        }
        return books;
    }

    /**
     * N4J
     * Function to return the most borrowed books in the last 30 days of a specific genre
     * @param genre Genre that the user (or admin) selected
     * @param time current time in unix timestamp
     * @return List of String containing titles of the books
     */
    public List<String> displayBestBookByGenreRecent(String genre, Integer time) {
        List<String> books = new ArrayList<>();
        List<Record> records = neo4JConnectionManager.bestBooksByGenreLastMonth(genre, time-2592000, time);
        for (var book: records) {
            books.add(String.valueOf(book.get(0)));
        }
        return books;
    }
}

    /*
    protected final MongoConnectionManager mongoConnectionManager;

    public UserManager(){
        mongoConnectionManager = MongoConnectionManager.getInstance();
    }

    /*----------------------------------------Control functions-------------------------------------------------------

    public ArrayList<Document> getBorrowingListAsDocuments(String username)
    {
        return mongoConnectionManager.findDocumentByKeyValue("customers", "username", username).next()
                .get("borrowingList", ArrayList.class);
    }

    public ArrayList<String> getReadingListTitles(String username)
    {
        return mongoConnectionManager.findDocumentByKeyValue("customers", "username", username).next()
                .get("readingList", ArrayList.class);
    }

    public ArrayList<String> getBorrowingListTitles(String username)
    {
        ArrayList<String> bookTitles = new ArrayList<>();

        for(Document book : getBorrowingListAsDocuments(username))
            bookTitles.add(book.getString("booktitle"));

        return bookTitles;
    }

    public ArrayList<BorrowingListBookMongo> getDetailedBorrowingList(String username)
    {
        ArrayList<BorrowingListBookMongo> borrowedBooks = new ArrayList<>();

        for(Document borrowedSelected : getBorrowingListAsDocuments(username))
            borrowedBooks.add(new BorrowingListBookMongo(borrowedSelected));

        return borrowedBooks;
    }

    /*----------------------------------------Display functions-------------------------------------------------------
    public ArrayList<CustomerMongo> displayUsersSorted(String key, Integer howManyDocuments, Integer order)
    {
        String collection = "customers";
        ArrayList<CustomerMongo> customersFound = new ArrayList<>();

        MongoCursor<Document> cursor = null;

        if (howManyDocuments == -1){
            // all users
            cursor = mongoConnectionManager.retrieveDocumentsOrderedByOneKeyValue(collection, key, order)
                    .iterator();
        }else{
            cursor = mongoConnectionManager.retrieveDocumentsOrderedByOneKeyValue(collection, key, order)
                    .limit(howManyDocuments).iterator();
        }


        while(cursor.hasNext())
            customersFound.add(new CustomerMongo(cursor.next()));

        return customersFound;
    }

    public ArrayList<CustomerMongo> displayUsersBasedOnAParameter(String key, String value)
    {
        String collection = "customers";
        ArrayList<CustomerMongo> customersFound = new ArrayList<>();

        MongoCursor<Document> cursor = mongoConnectionManager.findDocumentByKeyValue(collection, key, value);

        while(cursor.hasNext())
            customersFound.add(new CustomerMongo(cursor.next()));

        return customersFound;
    }

    public CustomerMongo displayUser(String username)
    //We can display the user data
    {
        return new CustomerMongo(mongoConnectionManager.findDocumentByKeyValue("customers", "username", username).next());
    }

    public ArrayList<BookMongo> displayBooksList(ArrayList<String> list)
    //We can display both reading list and borrowing list as normal books
    {
        ArrayList<BookMongo> books = new ArrayList<>();

        for(String bookTitle : list){
            books.add(new BookMongo(mongoConnectionManager.findDocumentByKeyValue("books", "Title", bookTitle).next()));
        }

        return books;
    }

    public ArrayList<BookMongo> displayBooksSorted(String key, Integer howManyDocuments, Integer order)
    {
        String collection = "books";
        ArrayList<BookMongo> booksFound = new ArrayList<>();

        MongoCursor<Document> cursor = null;

        if(howManyDocuments == -1){
            // all books
             cursor = mongoConnectionManager.retrieveDocumentsOrderedByOneKeyValue(collection, key, order)
                    .iterator();
        }else{
            // limit
            cursor = mongoConnectionManager.retrieveDocumentsOrderedByOneKeyValue(collection, key, order)
                    .limit(howManyDocuments).iterator();
        }


        while(cursor.hasNext())
            booksFound.add(new BookMongo(cursor.next()));

        return booksFound;
    }

    public ArrayList<BookMongo> displayBooksBasedOnAParameter(String key, String value)
    {
        String collection = "books";
        ArrayList<BookMongo> booksFound = new ArrayList<>();

        MongoCursor<Document> cursor = mongoConnectionManager.findDocumentByKeyValue(collection, key, value);

        while(cursor.hasNext())
            booksFound.add(new BookMongo(cursor.next()));

        return booksFound;
    }

    public BookMongo displayBook(String book)
    {
        return new BookMongo(mongoConnectionManager.findDocumentByKeyValue("books", "Title", book).next());
    }

    public ArrayList<ReviewMongo> displayReviews(String book)
    {
        ArrayList<ReviewMongo> reviewsFound = new ArrayList<>();
        MongoCursor<Document> associatedReviews = mongoConnectionManager.findDocumentByKeyValue("reviews", "bookTitle", book);

        while(associatedReviews.hasNext())
            reviewsFound.add(new ReviewMongo(associatedReviews.next()));

        return reviewsFound;
    }

    /*----------------------------------------Add functions-----------------------------------------------------------

    /*----------------------------------------Generic functions-------------------------------------------------------

    public void calculateScore(String bookTitle)
    {
        ArrayList<ReviewMongo> reviewsFound = new ArrayList<>();
        Double newRating = 0.0;

        MongoCursor<Document> associatedReviews = mongoConnectionManager.findDocumentByKeyValue("reviews", "bookTitle", bookTitle);

        while(associatedReviews.hasNext())
            reviewsFound.add(new ReviewMongo(associatedReviews.next()));

        if (reviewsFound.size() == 0)
            return;

        for(ReviewMongo reviews : reviewsFound)
            newRating = newRating + reviews.getScore();

        mongoConnectionManager.updateOneDocumentByKeyValue("books", "Title", bookTitle,"score", (newRating/reviewsFound.size())); //Recalculates the score
    }

    public Paged<BookMongo> getBookMongo(int pageNumber, int size)
    {
        List<BookMongo> books = displayBooksSorted("Title", -1, 1);

        List<BookMongo> paged = books.stream()
                                     .skip((pageNumber-1) * size)
                                     .limit(size)
                                     .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) books.size() / size);

        System.out.println("book size: " + books.size());
        System.out.println("size: " + size);
        System.out.println("totalPages: " + totalPages);

        return new Paged<>(new Page<>(paged, totalPages), Paging.of(totalPages, pageNumber, size));

        //return new Paged<>();
    }

    public Paged<BookMongo> getReadingList(int pageNumber, int size, String username)
    {
        List<BookMongo> books = displayBooksList(getReadingListTitles(username));

        List<BookMongo> paged = books.stream()
                .skip((pageNumber-1) * size)
                .limit(size)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) books.size() / size);

        System.out.println("book size: " + books.size());
        System.out.println("size: " + size);
        System.out.println("totalPages: " + totalPages);

        return new Paged<>(new Page<>(paged, totalPages), Paging.of(totalPages, pageNumber, size));

    }

    public Paged<BorrowingListBookMongo> getBorrowingList(int pageNumber, int size, String username)
    {
        List<BorrowingListBookMongo> books = getDetailedBorrowingList(username);

        List<BorrowingListBookMongo> paged = books.stream()
                .skip((pageNumber-1) * size)
                .limit(size)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) books.size() / size);

        System.out.println("book size: " + books.size());
        System.out.println("size: " + size);
        System.out.println("totalPages: " + totalPages);

        return new Paged<>(new Page<>(paged, totalPages), Paging.of(totalPages, pageNumber, size));

    }

    public Paged<CustomerMongo> getUsers(int pageNumber, int size)
    {
        List<CustomerMongo> customers = displayUsersSorted("username", -1, 1);

        List<CustomerMongo> paged = customers.stream()
                .skip((pageNumber-1) * size)
                .limit(size)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) customers.size() / size);

        System.out.println("book size: " + customers.size());
        System.out.println("size: " + size);
        System.out.println("totalPages: " + totalPages);

        return new Paged<>(new Page<>(paged, totalPages), Paging.of(totalPages, pageNumber, size));
    }

    public void create(){
        //Creating a JSONObject object
        JSONObject jsonObject = new JSONObject();

        ArrayList<BookMongo> books = displayBooksSorted("Title", -1, 1);

        for(int i = 0; i< books.size(); i++) {
            jsonObject.put("_id", books.get(i));
        }



        StringBuilder sb = new StringBuilder();

        for(Document x : mongoConnectionManager.printAllBooksWithId()){
            sb.append("{ \"_id\": {\"$oid\": ");
            sb.append("\"");
            sb.append(x.getObjectId("_id"));
            sb.append("\"");
            sb.append("}, \"Title\": ");
            sb.append("\"");
            sb.append(x.getString("Title"));
            sb.append("\" }, ");
        }

        try {
            FileWriter file = new FileWriter("C:\\Users\\tommy\\Desktop\\output.json");
            file.write(sb.toString());
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("JSON file created: "+jsonObject);
    }

    */