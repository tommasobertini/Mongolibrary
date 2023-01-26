package it.unipi.dii.inginf.lsmdb.mongolibrary.service;

import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.LoginException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo.ReviewMongo;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.Password;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Sorts.orderBy;

public class MongoConnectionManager implements AutoCloseable{
    private static final Logger LOGGER = LogManager.getLogger(MongoConnectionManager.class);
    private static MongoConnectionManager instance = null;
    private final MongoClient client;
    private final MongoDatabase database;
    private MongoCollection<Document> interestedCollection;
    private final Bson idFilter;

    private MongoConnectionManager()
    {
        //Create connections String to local instance
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        //Create a new connection to mongodb client
        client = MongoClients.create(uri);
        //Connect to mongolibrary database -> Creates db only when we insert a collection, and we insert something in it
        database = client.getDatabase("mongolibrary");
        //Gets the specific document for the reportedReviews managed by the admins
        idFilter = Filters.eq("_id", new ObjectId("63c98652d3cc6d14a4a3fdea"));
    }

    public static MongoConnectionManager getInstance()
    //Singleton Pattern
    {
        if(instance == null)
        {
            LOGGER.info("getInstance() | Creating a new MongoConnectionManager instance"); //LOG

            instance = new MongoConnectionManager();
        }
        LOGGER.info("getInstance() | Returning the MongoConnectionManager instance"); //LOG
        return instance;
    }

    @Override
    public void close()
    //Terminates the connection
    {
        LOGGER.info("close() | Closing the connection"); //LOG

        client.close();
    }

    /*----------------------------------------Find functions----------------------------------------------------------*/

    /**
     * Orders all documents in a specified collection and returns them.
     * @param collection name of the collection in the database
     * @param key name of the key in the document structure
     * @param order depending on value: -1 (orders in ascending) or 1 (orders in descending)
     * @return mongocursor of the document specified
     */
    public MongoCursor<Document> findDocumentsOrderedByOneKeyValue(String collection, String key, Integer pageNumber, Integer pageLength, Integer order) throws MongoException
    {
        LOGGER.info("findDocumentsOrderedByOneKeyValue() | Searching and sorting in " + collection + ", with key: " + key); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        if (order == -1) {
            return interestedCollection.find().sort(orderBy(descending(key))).skip(pageLength * (pageNumber - 1)).limit(pageLength).iterator();
        }

        return interestedCollection.find().sort(orderBy(ascending(key))).skip(pageLength * (pageNumber - 1)).limit(pageLength).iterator();
    }

    /**
     * Orders all documents in a specified collection and returns them.
     * @param collection name of the collection in the database
     * @param key name of the key in the document structure
     * @param value value to search throught the regex
     * @param order depending on value: -1 (orders in ascending) or 1 (orders in descending)
     * @return mongocursor of the document specified
     */
    public MongoCursor<Document> findDocumentsOrderedByOneKeyValue(String collection, String key, String value, Integer pageNumber, Integer pageLength, Integer order) throws MongoException
    {

        LOGGER.info("findDocumentsOrderedByOneKeyValue() | Searching and sorting in " + collection + " using regex, with key: " + key + ", the value " +  value); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter = regex(key,"^" + value, "i");

        if (order == -1) {
            return interestedCollection.find(filter).sort(orderBy(descending(key))).skip(pageLength * (pageNumber - 1)).limit(pageLength).iterator();
        }

        return interestedCollection.find(filter).sort(orderBy(ascending(key))).skip(pageLength * (pageNumber - 1)).limit(pageLength).iterator();
    }

    /**
     * Orders all documents in a specified collection and returns them.
     * @param collection name of the collection in the database
     * @param key1 name of the key in the document structure
     * @param value1 value that must be searched
     * @param key2 key on which we order the resulting query
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @param order depending on value: -1 (orders in ascending) or 1 (orders in descending)
     * @return mongocursor of the document specified
     */
    public MongoCursor<Document> findDocumentsByOneKeyValueAndOrderBySecondKey(String collection, String key1, String value1, String key2, Integer pageNumber, Integer pageLength, Integer order) throws MongoException
    {
        LOGGER.info("findDocumentsByOneKeyValueAndOrderBySecondKey() | Searching in " + collection + " with key: " + key1 + ", the value " +  value1 + " and sorting by " + key2); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter = eq(key1, value1);

        if (order == -1) {
            return interestedCollection.find(filter).sort(orderBy(descending(key2))).skip(pageLength * (pageNumber - 1)).limit(pageLength * pageNumber).iterator();
        }

        return interestedCollection.find(filter).sort(orderBy(ascending(key2))).skip(pageLength * (pageNumber - 1)).limit(pageLength * pageNumber).iterator();
    }

    /**
     * Searches a document in a specified collection and returns it.
     * @param collection name of the collection in the database
     * @param key name of the key in the document structure
     * @param value value searched
     * @return mongocursor of the document specified
     */
    public MongoCursor<Document> findDocumentByKeyValue(String collection, String key, String value) throws MongoException
    {
        LOGGER.info("findDocumentByKeyValue() | Searching in " + collection + " with key: " + key + ", the value " +  value); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        //Returns the cursor with all the documents found
        return interestedCollection.find(eq(key, value)).iterator();
    }

    /**
     * Searches a document in a specified collection and returns it.
     * @param collection name of the collection in the database
     * @param key1 name of the first key in the document structure
     * @param value1 first value searched
     * @param key2 name of the second key in the document structure
     * @param value2 second value searched
     * @return mongocursor of the document specified
     */
    public MongoCursor<Document> findDocumentByKeyValue(String collection, String key1, String value1, String key2, String value2) throws MongoException
    {
        LOGGER.info("findDocumentByKeyValue() | Searching in " + collection + " with keys " + key1 + " and " + key2 + ", the values " +  value1 + " and " + value2); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter1 = Filters.eq(key1, value1);
        Bson filter2 = Filters.eq(key2, value2);
        return interestedCollection.find(and(filter1, filter2)).iterator();
    }

    /**
     * Searches an embedded document in a specified collection and returns it.
     * @param collection name of the collection in the database
     * @param key name of the key in the document structure
     * @param value document searched
     * @return if present a mongocursor of the embedded document specified
     */
    public MongoCursor<Document> findEmbeddedDocumentByKeyValue(String collection, String key, Document value) throws MongoException
    {
        LOGGER.info("findEmbeddedDocumentByKeyValue() | Searching in " + collection + " with key: " + key + ", the document " +  value); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        return interestedCollection.find(eq(key, Document.parse(value.toJson()))).iterator();
    }

    /*----------------------------------------Add functions-----------------------------------------------------------*/

    /**
     * Adds a document to a specified collection.
     * @param collection name of the collection in the database
     * @param element document to add
     */
    public void addElement(String collection, Document element) throws MongoException
    //Adds user or book or review to mongoDB
    {
        LOGGER.info("addElement() | Adding to the " + collection + ", the document " + element); //LOG

        interestedCollection = database.getCollection(collection);
        interestedCollection.insertOne(element);
    }

    /**
     * Adds a document to a specified collection.
     * @param collection name of the collection in the database
     * @param key name of the key in the document structure
     * @param value value searched
     * @param List name of the list in which we need to add the element
     * @param listObject Document/value to add
     */
    public void addElementToList(String collection, String key, String value, String List, Object listObject) throws MongoException
    {
        LOGGER.info("addElementToList() | In collection " + collection + ", adding to the document with key " + key + " and value " + value + ", the element " + listObject + "to the list " + List); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter = Filters.eq(key, value);
        Bson update = Updates.push(List, listObject);
        interestedCollection.findOneAndUpdate(filter, update);
    }

    /**
     * Adds a document to a specified collection.
     * @param collection name of the collection in the database
     * @param key1 name of the first key in the document structure
     * @param value1 first value searched
     * @param key2 name of the second key in the document structure
     * @param value2 second value searched
     * @param List name of the list in which we need to add the element
     * @param listObject Document/value to add
     */
    public void addElementToList(String collection, String key1, String value1, String key2, String value2, String List, Object listObject) throws MongoException
    {
        LOGGER.info("addElementToList() | In collection " + collection + ", adding to the document with keys " + key1 + ", " + key2 + " and values " +  value1 + ", " + value2 + ", the element " + listObject + "to the list " + List); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter1 = Filters.eq(key1, value1);
        Bson filter2 = Filters.eq(key2, value2);
        Bson update = Updates.push(List, listObject);
        interestedCollection.findOneAndUpdate(and(filter1, filter2), update);
    }

    /*----------------------------------------Remove functions--------------------------------------------------------*/

    /**
     * Removes a document from a specified collection.
     * @param collection name of the collection in the database
     * @param key name of the key in the document structure
     * @param value value searched
     */
    public void removeElement(String collection, String key, String value) throws MongoException
    {
        LOGGER.info("removeElement() | In collection " + collection + ", removing the element with key " + key + " and  value " +  value); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter = Filters.eq(key, value);
        interestedCollection.deleteOne(filter);
    }

    /**
     * Removes a document from a specified collection.
     * @param collection name of the collection in the database
     * @param key1 name of the first key in the document structure
     * @param value1 first value searched
     * @param key2 name of the second key in the document structure
     * @param value2 second value searched
     */
    public void removeElement(String collection, String key1, String value1, String key2, String value2) throws MongoException
    {
        LOGGER.info("removeElement() | In collection " + collection + "removing the element with keys " + key1 + ", " + key2 + " and values " + value1 + ", " + value2); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter1 = Filters.eq(key1, value1);
        Bson filter2 = Filters.eq(key2, value2);
        interestedCollection.deleteOne(and(filter1, filter2));
    }

    /**
     * Removes a document/value from a specified document in a collection.
     * @param collection name of the collection in the database
     * @param key name of the key in the document structure
     * @param value value searched
     * @param list name of the list in which we need to remove the element
     * @param listObject Document/value to remove
     */
    public void removeElementFromList(String collection, String key, String value, String list, Object listObject) throws MongoException
    {
        LOGGER.info("removeElementFromList() | In collection " + collection + "removing the element " + listObject + " from " + list + " in document with key" + key + " and value " + value); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter = Filters.eq(key, value);
        Bson update = Updates.pull(list, listObject);
        interestedCollection.findOneAndUpdate(filter, update);
    }

    /**
     * Removes a document/value from a specified document in a collection.
     * @param collection name of the collection in the database
     * @param key1 name of the first key in the document structure
     * @param value1 first value searched
     * @param key2 name of the second key in the document structure
     * @param value2 second value searched
     * @param list name of the list in which we need to remove the element
     * @param listObject Document/value to remove
     */
    public void removeElementFromList(String collection, String key1, String value1, String key2, String value2, String list, Object listObject) throws MongoException
    {
        LOGGER.info("removeElementFromList() | In collection " + collection + "removing the element " + listObject + " from " + list + " in document with keys" + key1 + ", " + key2 + " and values " + value1 + ", " + value2); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter1 = Filters.eq(key1, value1);
        Bson filter2 = Filters.eq(key2, value2);
        Bson update = Updates.pull(list, listObject);
        interestedCollection.findOneAndUpdate(and(filter1, filter2) , update);
    }

    /**
     * Removes an embedded document from a specified document in a collection.
     * @param collection name of the collection in the database
     * @param documentKey name of the key of the document searched
     * @param documentValue value of the documentKey searched
     * @param documentList name of the array of embedded documents
     * @param embeddedKey name of the key of the embedded document searched
     * @param embeddedValue value of the embeddedKey searched
     */
    public void removeElementFromEmbeddedDocumentList(String collection, String documentKey, String documentValue, String documentList, String embeddedKey, String embeddedValue) throws MongoException
    {
        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        //Filter to know on which document we need to remove one element
        Bson filter1 = Filters.eq(documentKey, documentValue);
        //Filter to know which embedded document we need to remove
        Bson filter2 = Filters.eq(embeddedKey, embeddedValue);
        //Filter to do the removal of the embedded document
        Bson update = Updates.pull(documentList, filter2);

        //Remove the embedded document from the reviews of the book
        interestedCollection.findOneAndUpdate(filter1, update);
    }

    /*----------------------------------------Update functions--------------------------------------------------------*/

    /**
     * Updates a document/value of a specified document in a collection.
     * @param collection name of the collection in the database
     * @param key name of the key in the document structure
     * @param value value searched
     * @param keyToUpdate name of the key to update
     * @param valueUpdated Document/value to insert
     */
    public void updateOneDocumentByKeyValue(String collection, String key, String value, String keyToUpdate, Object valueUpdated) throws MongoException
    {
        LOGGER.info("updateOneDocumentByKeyValue() | In collection " + collection + " updating " + keyToUpdate + " with value "+ valueUpdated + " of document with key " + key + " and value " +  value); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        interestedCollection.updateOne(Filters.eq(key, value), Updates.set(keyToUpdate, valueUpdated));
    }

    /**
     * Updates a document/value of a specified document in a collection.
     * @param collection name of the collection in the database
     * @param key1 name of the first key in the document structure
     * @param value1 first value searched
     * @param key2 name of the second key in the document structure
     * @param value2 second value searched
     * @param list name of the key to update
     * @param listObject Document/value to update
     */
    public void updateElementOfList(String collection, String key1, String value1, String key2, String value2, String list, Object listObject) throws MongoException
    {
        LOGGER.info("updateOneDocumentByKeyValue() | In collection " + collection + " updating the list " + list + " with  "+ listObject + " of document with keys " + key1 + ", " + key2 + " and values " +  value1 + ", " + value2); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter1 = Filters.eq(key1, value1);
        Bson filter2 = Filters.eq(key2, value2);
        Bson update = Updates.set(list, listObject);
        interestedCollection.findOneAndUpdate(and(filter1, filter2), update);
    }

    /*----------------------------------------Reported reviews functions----------------------------------------------*/
    /* Description
     * We decided to add some specific functions to modify/remove/add the reported reviews because these are a particular
     * structure that has been added as a document into the "admins" collection to avoid adding a new collection to the
     * database
     */

    /**
     * Searches the review specified by username and bookTitle
     * @param username username of the writer of the review
     * @param bookTitle book that has the review
     * @return mongocursor of the document specified
     */
    public MongoCursor<Document> findReportedReview(String username, String bookTitle) throws MongoException
    {
        LOGGER.info("findReportedReview() | Searching " + username + ", " + bookTitle); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection("admins");
        Bson usernameFilter = Filters.eq("reportedReviews.username", username);
        Bson bookFilter = Filters.eq("reportedReviews.bookTitle", bookTitle);
        return interestedCollection.find(and(idFilter, and(usernameFilter, bookFilter))).iterator();
    }

    /**
     * Searches the review specified by username, bookTitle and reporter
     * @param username username of the writer of the review
     * @param bookTitle book that has the review
     * @param reporter username of who is reporting the review
     * @return mongocursor of the document specified
     */
    public MongoCursor<Document> findReportedReview(String username, String bookTitle, String reporter) throws MongoException
    {
        LOGGER.info("findReportedReview() | Searching " + username + ", " + bookTitle + " and " + reporter); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection("admins");
        Bson usernameFilter = Filters.eq("reportedReviews.username", username);
        Bson bookFilter = Filters.eq("reportedReviews.bookTitle", bookTitle);
        Bson reporterFilter = Filters.in("reportedReviews.reportedBy", reporter);
        return interestedCollection.find(and(idFilter, and(usernameFilter, bookFilter, reporterFilter))).iterator();
    }

    /**
     * Adds a new reported Review.
     * @param reportedReview the review to add
     */
    public void addReportedReview(Document reportedReview) throws MongoException
    {
        LOGGER.info("addReportedReview() | Adding in reported reviews, the review " + reportedReview);

        //Preliminary collection set
        interestedCollection = database.getCollection("admins");

        Bson update = Updates.push("reportedReviews", reportedReview);
        interestedCollection.findOneAndUpdate(idFilter, update);
    }

    /**
     * Adds the reporter to the specified review.
     * @param username username of the writer of the review
     * @param bookTitle book that has the review
     * @param reporter username of who is reporting the review
     */
    public void addNameToReportedReview(String username, String bookTitle, String reporter) throws MongoException
    {
        LOGGER.info("addNameToReportedReview() | Adding in reported reviews, to the review " + username + ", " + bookTitle + " the name " + reporter);

        //Preliminary collection set
        interestedCollection = database.getCollection("admins");
        Bson usernameFilter = Filters.eq("reportedReviews.username", username);
        Bson bookFilter = Filters.eq("reportedReviews.bookTitle", bookTitle);
        Bson update = Updates.push("reportedReviews.$.reportedBy", reporter);
        interestedCollection.findOneAndUpdate(and(idFilter, and(usernameFilter, bookFilter)), update);
    }

    /**
     * Removes from the database the specified reported review.
     * @param username username of the writer of the review
     * @param bookTitle book that has the review
     */
    public void removeReportedReview(String username, String bookTitle) throws MongoException
    {
        LOGGER.info("removeReportedReview() | Removing from reported reviews, the review " + username + ", " + bookTitle);

        //Preliminary collection set
        interestedCollection = database.getCollection("admins");
        Bson usernameFilter = Filters.eq("username", username);
        Bson bookFilter = Filters.eq("bookTitle", bookTitle);
        Bson update = Updates.pull("reportedReviews", and(usernameFilter, bookFilter));
        interestedCollection.updateOne(idFilter, update);
    }

    /*----------------------------------------Generic functions-------------------------------------------------------*/

    /**
     * Calculates and inserts into the database the new score of a book
     * @param bookTitle name used to retireve the document values adn then update it
     */
    public void calculateScore(String bookTitle) throws MongoException
    {
        LOGGER.info("calculateScore() | Recalculating the score for the book " + bookTitle);

        ArrayList<ReviewMongo> reviewsFound = new ArrayList<>();
        double newRating = 0.0;

        MongoCursor<Document> associatedReviews = findDocumentByKeyValue("reviews", "bookTitle", bookTitle);

        while(associatedReviews.hasNext())
            reviewsFound.add(new ReviewMongo(associatedReviews.next()));

        if(reviewsFound.size() == 0) {
            updateOneDocumentByKeyValue("books", "Title", bookTitle,"score", 0);
            return;
        }

        for(ReviewMongo reviews : reviewsFound)
            newRating = newRating + reviews.getScore();

        //Recalculates the score
        updateOneDocumentByKeyValue("books", "Title", bookTitle,"score", (newRating/reviewsFound.size()));
    }

    /*----------------------------------------Atomic Transactions-----------------------------------------------------*/

    /**
     * Modifies the borrowingList like as if it was a transaction
     * @param username name of the user that borrowed the book
     * @param bookTitle name of the book that has been borrowed
     * @param newStatus either "RETURNED" or "OVERDUE
     */
    public void atomicModifyBorrowingListBookStatus(String username, String bookTitle, String newStatus) throws MongoException
    {
        LOGGER.info("atomicModifyBorrowingListBookStatus() | updating " + username + "'s borrowing list for the book "+ bookTitle + " with new status " + newStatus);

        updateElementOfList("customers","username", username, "borrowingList.booktitle", bookTitle,"borrowingList.$.status", newStatus);
        if(newStatus.equals("RETURNED"))
        {
            try {
                int copies = findDocumentByKeyValue("books", "bookTitle", bookTitle).next().getInteger("copies");
                updateOneDocumentByKeyValue("books", "bookTitle", bookTitle, "copies", (copies + 1));
            } catch (MongoException me){
                LOGGER.error("atomicModifyBorrowingListBookStatus() | error during the function: " + me.getMessage());
                //Rollback of first update, we do not know the previous status but if we set it to "BORROWED" as default
                // If it was "OVERDUE" we have no problems because at the next login of the user it will be updated again
                updateElementOfList("customers","username", username, "borrowingList.booktitle", bookTitle,"borrowingList.$.status", "BORROWED");
                throw new MongoException(me.getMessage());
            }
        }
    }

    /**
     * Adds to the borrowingList a book like as if it was a transaction
     * @param username name of the user that borrowed the book
     * @param bookTitle name of the book that has been borrowed
     * @param borrowedBook new document to add to the list of borrowed books
     */
    public int atomicAddBookToBorrowingList(String username, String bookTitle, Document borrowedBook) throws MongoException
    {
        LOGGER.info("atomicAddBookToBorrowingList() | updating " + username + "'s borrowing list adding the book "+ bookTitle);

        int newcopies = findDocumentByKeyValue("books", "Title", bookTitle).next().getInteger("copies") - 1;
        //Adds the book to the borrowing list of the user
        addElementToList("customers", "username", username, "borrowingList", borrowedBook);
        try {
            //Updates the book copies in the DB
            updateOneDocumentByKeyValue("books", "Title", bookTitle, "copies", newcopies);
        } catch (MongoException me){
            LOGGER.error("atomicAddBookToBorrowingList() | error during the function: " + me.getMessage());
            //Rollback of the previous action, therefore we remove the book from the borrowing list
            removeElementFromList("customers", "username", username, "borrowingList", borrowedBook);
            //We propagate it to avoid adding the borrowed book to neo4j
            throw new MongoException(me.getMessage());
        }
        return newcopies;
    }

    /**
     * Deletes a review and updates the score of the corrisponding book like as if it was a transaction
     * @param collection name of the collection : "reviews"
     * @param username name of the user that wrote the review
     * @param bookTitle name of the book that has the review
     */
    public void atomicDeleteReview(String collection, String username, String bookTitle) throws MongoException
    {
        LOGGER.info("atomicDeleteReview() | removing the review with username " + username + "and book "+ bookTitle);

        //First we need to save the review to eventually rollback the operation
        Document reviewToRemove = findDocumentByKeyValue(collection, "username", username, "bookTitle", bookTitle).next();
        //Removes the review
        removeElement(collection, "username", username, "bookTitle", bookTitle);
        try{
            //Calculates the new score of the book after the removal using reviews collection
            calculateScore(bookTitle);
            //First we need to save the review to eventually rollback the operation
            Document reportedReviewToRemove = findReportedReview(username, bookTitle).next();
            //Removes it from the reported reviews list
            removeReportedReview(username, bookTitle);
            try{
                //Removes it from the embedded list of the book, if not present it doesn't do anything
                removeElementFromEmbeddedDocumentList("books", "Title", bookTitle, "reviews", "username", username);
            } catch (MongoException me){
                addReportedReview(reportedReviewToRemove);
                throw new MongoException(me.getMessage());
            }
        } catch (MongoException me){
            LOGGER.error("atomicDeleteReview() | error during the function: " + me.getMessage());
            //Rollback the operation of removal
            addElement(collection, reviewToRemove);
            //We need to recalculate the score with the review again
            calculateScore(bookTitle);
            throw new MongoException(me.getMessage());
        }
    }

    /**
     * Deletes a review and updates the score of the corrisponding book like as if it was a transaction
     * @param bookTitle name of the book that has the review
     * @param review review document to add
     */
    public void atomicAddReview(String bookTitle, Document review) throws MongoException
    {
        LOGGER.info("atomicAddReview() | adding the review " + review + "for the book "+ bookTitle);

        //Adds review to the list of embedded reviews of the book
        addElementToList("books", "Title", bookTitle, "reviews", review);
        try{
            addElement("reviews", review);
            try {
                calculateScore(bookTitle);
                removeOldestReview(bookTitle);
            } catch (MongoException me) {
                //remove the review since ei
                removeElement("reviews", "bookTitle", review.getString("bookTitle"), "username", review.getString("username"));
                //We need to recalculate the score with the review again
                calculateScore(bookTitle);
                throw new MongoException(me.getMessage());
            }
        } catch (MongoException me){
            LOGGER.error("atomicAddReview() | error during the function: " + me.getMessage());
            //Rollback the add operation
            removeElementFromList("books", "Title", bookTitle, "reviews", review);
            throw new MongoException(me.getMessage());
        }
    }

    /*----------------------------------------Pipeline functions------------------------------------------------------*/

    /**
     * Pipeline that returns the most borrowed books in a time period by people born in a certain interval of time
     * @param startYear smallest birthYear included
     * @param finalYear biggest birthYear included
     * @param startDate smallest borrowdate included
     * @param endDate biggest borrowdate included
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return most borrowed books as Documents: _id: String (Name of book), timesBorrowed: int
     */
    public ArrayList<Document> mostBorrowedBooksBasedOnTimeAndBirthYear(Integer startYear, Integer finalYear, Integer startDate, Integer endDate, Integer pageNumber, Integer pageLength) throws MongoException
    {
        LOGGER.info("mostBorrowedBooksBasedOnTimeAndBirthYear() | Searching top books borrowed in time period between "+ startDate + " and " + endDate + " by people born between " + startYear + " and "+ finalYear);

        //Preliminary collection set
        interestedCollection = database.getCollection("customers");

        //Filter users based on their birth year
        Bson start = Filters.gte("birthYear", startYear);
        Bson end = Filters.lte("birthYear", finalYear);

        //Filter borrowing list's books based on their borrow date
        Bson startTime = Filters.gte("borrowingList.borrowdate", startDate);
        Bson endTime = Filters.lte("borrowingList.borrowdate", endDate);

        //Sorting books
        Bson sortStage = Sorts.descending("timesBorrowed");

        //Conversion in List of documents
        return interestedCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(and(start, end)),
                        Aggregates.match(and(startTime, endTime)),
                        Aggregates.unwind("$borrowingList"),
                        Aggregates.group("$borrowingList.booktitle", Accumulators.sum("timesBorrowed", 1)),
                        Aggregates.sort(sortStage),
                        Aggregates.skip(pageLength * (pageNumber - 1)),
                        Aggregates.limit(pageLength)
                )
        ).into(new ArrayList<>());
    }

    /**
     * Pipeline that returns the average number of borrowed books by each group of people based on nationality and a period
     * @param startDate smallest borrowdate included
     * @param endDate biggest borrowdate included
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return average number of books borrowed by each nationailty of people as Documents: _id: String (Nationality), averageNumberOfBooksTaken: double
     */
    public ArrayList<Document> averageNumberOfBorrowedBooksPerUserNationalityInAPeriod(Integer startDate, Integer endDate, Integer pageNumber, Integer pageLength) throws MongoException
    {
        LOGGER.info("averageNumberOfBorrowedBooksPerUserNationalityInAPeriod() | Calculating average number of borrewed books by nationality between "+ startDate + " and " + endDate);

        //Preliminary collection set
        interestedCollection = database.getCollection("customers");

        //Filter borrowing list's books based on their borrow date
        Bson startTime = Filters.gte("borrowingList.borrowdate", startDate);
        Bson endTime = Filters.lte("borrowingList.borrowdate", endDate);

        //Sorting nationalities
        Bson sortStage = Sorts.descending("averageNumberOfBooksTaken");

        //Conversion in list of Documents
        return interestedCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(and(startTime, endTime)),
                        Aggregates.project(
                                Projections.fields(
                                        Projections.excludeId(),
                                        Projections.include("username", "nationality"),
                                        Projections.computed("numberOfBooks", new Document("$size", "$borrowingList"))
                                )
                        ),
                        Aggregates.group("$nationality", Accumulators.avg("averageNumberOfBooksTaken", "$numberOfBooks")),
                        Aggregates.sort(sortStage),
                        Aggregates.skip(pageLength * (pageNumber - 1)),
                        Aggregates.limit(pageLength)
                )
        ).into(new ArrayList<>());
    }

    /**
     * Pipeline that returns the users who received the most likes on the reviews that they have written within a time period and a selected score
     * @param startDate smallest reviewTime included
     * @param endDate biggest reviewTime included
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return top users as said above and the number of likes received as Document: _id: String (username), numberOfLikesReceived: int
     */
    public ArrayList<Document> usersThatWroteTheMostLikedReviewsInATimePeriodWithAScore(Integer startDate, Integer endDate, Integer score, Integer pageNumber, Integer pageLength) throws MongoException
    {
        LOGGER.info("usersThatWroteTheMostLikedReviewsInATimePeriodWithAScore() | Searching the users who wrote the most liked reviews between "+ startDate + " and " + endDate);

        //Preliminary collection set
        interestedCollection = database.getCollection("reviews");

        //Filter for the score
        Bson scoreFilter = Filters.eq("score", score);

        //Filter reviews in the time period given
        Bson dateFilter1 = Filters.gte("reviewTime", startDate);
        Bson dateFilter2 = Filters.lte("reviewTime", endDate);

        //Sorting the reviews
        Bson sortStage = Sorts.descending("numberOfLikesReceived");

        return interestedCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(scoreFilter),
                        Aggregates.match(and(dateFilter1, dateFilter2)),
                        Aggregates.project(
                                Projections.fields(
                                        Projections.excludeId(),
                                        Projections.include("username"),
                                        Projections.computed("numberOfLikes", new Document("$size", "$likes"))
                                )
                        ),
                        Aggregates.group("$username", Accumulators.sum("numberOfLikesReceived", "$numberOfLikes")),
                        Aggregates.sort(sortStage),
                        Aggregates.skip(pageLength * (pageNumber - 1)),
                        Aggregates.limit(pageLength)
                )
        ).into(new ArrayList<>());
    }

    /**
     * Pipeline that returns the books with the highest average number of likes per review in a period
     * @param startDate smallest reviewTime included
     * @param endDate biggest reviewTime included
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return books with the highest average number of likes per review as Document: _id: String (bookTitle), averageNumberOfLikes: double
     */
    public ArrayList<Document> averageNumberOfLikesPerReviewOfBooksInAPeriod(Integer startDate, Integer endDate, Integer pageNumber, Integer pageLength) throws MongoException
    {
        LOGGER.info("averageNumberOfLikesPerReviewOfBooksInAPeriod() | Calculating the average number of likes per review of books between "+ startDate + " and " + endDate);

        //Preliminary collection set
        interestedCollection = database.getCollection("reviews");

        //Filter reviews in the time period given
        Bson dateFilter1 = Filters.gte("reviewTime", startDate);
        Bson dateFilter2 = Filters.lte("reviewTime", endDate);

        //Sorting the reviews
        Bson sortStage = Sorts.descending("averageNumberOfLikes");

        return interestedCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(and(dateFilter1, dateFilter2)),
                        Aggregates.project(
                                Projections.fields(
                                        Projections.include("bookTitle"),
                                        Projections.computed("numberOfLikes", new Document("$size", "$likes"))
                                )
                        ),
                        Aggregates.group("$bookTitle", Accumulators.avg("averageNumberOfLikes", "$numberOfLikes")),
                        Aggregates.sort(sortStage),
                        Aggregates.skip(pageLength * (pageNumber - 1)),
                        Aggregates.limit(pageLength)
                )
        ).into(new ArrayList<>());
    }

    /**
     * Pipeline that returns the most scored books specified by a key
     * @param key either "authors", "publisher" or anything else
     * @param value a string that identifies the authors, the publisher or anything else
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return most scored books as Documents (same structure as the one in the collection)
     */
    public ArrayList<Document> highestRatedBooksBasedOnKeyValue(String key, String value, Integer pageNumber, Integer pageLength) throws MongoException
    {
        LOGGER.info("highestRatedBooksBasedOnKeyValue() | Searching top rated books with key " + key + ",value " + value);

        //Preliminary collection set
        interestedCollection = database.getCollection("books");
        //Filter books based on author or publisher
        Bson filter = regex(key,"^" + value, "i"); //key = "authors", "publisher" or anything that is a string; value = any name
        //Sorting books by score
        Bson sortStage = Sorts.descending("score");
        return interestedCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(filter),
                        Aggregates.sort(sortStage),
                        Aggregates.skip(pageLength * (pageNumber - 1)),
                        Aggregates.limit(pageLength)
                )
        ).into(new ArrayList<>());
    }

    /**
     * Pipeline that removes the oldest review in the embbedded structure of the book
     * @param bookTitle book that has the review to remove
     */
    public void removeOldestReview(String bookTitle) throws MongoException
    {
        LOGGER.info("removeOldestReview() | Removing the oldest review of " + bookTitle);

        String collection = "books";
        String documentKey = "Title";
        //Preliminary collection set
        interestedCollection = database.getCollection(collection);
        //Filter by bookTitle
        Bson filter1 = Filters.eq(documentKey, bookTitle);

        // Use the aggregate method to find the embedded document with the lowest value
        Document review = interestedCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(filter1),
                        Aggregates.project(
                                Projections.fields(
                                        Projections.excludeId(),
                                        Projections.include("reviews.username", "reviews.reviewTime"),
                                        Projections.computed("numberOfReviews", new Document("$size", "$reviews"))
                                )
                        ),
                        Aggregates.unwind("$reviews"),
                        Aggregates.sort(Sorts.ascending("reviews.reviewTime")),
                        Aggregates.limit(1)
                )
        ).first();

        //If the book has 50 or fewer reviews we do not need to remove any review
        if (review == null || review.getInteger("numberOfReviews") <= 50) {
            LOGGER.info("removeOldestReview() | The book " + bookTitle + " has not reached the 51 reviews threshold, so we do not remove any review");
            return;
        }

        // Get the username of the embedded review to remove
        String embeddedUsername = review.get("reviews", Document.class).getString("username");

        //Function that removes the embedded document
        removeElementFromEmbeddedDocumentList(collection, documentKey, bookTitle, "reviews", "username", embeddedUsername);
    }

    /**
     * Pipeline that returns all the reported reviews in order of how many reports they have
     * @param pageNumber the number page in which we are
     * @param pageLength how many elements we want to display
     * @return Arraylist of Documents: {username: String, bookTitle: String, description: String}, numberOfReports: int
     */
    public ArrayList<Document> findAllReportedReviewsSorted(Integer pageNumber, Integer pageLength) throws MongoException
    {
        LOGGER.info("findAllReportedReviewsSorted() | Sorting all reported reviews");

        //Preliminary collection set
        interestedCollection = database.getCollection("admins");
        Bson sortStage = Sorts.descending("numberOfReports");

        //Returns only sorted embedded documents
        return interestedCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(idFilter),
                        Aggregates.unwind("$reportedReviews"),
                        Aggregates.project(
                                Projections.fields(
                                        Projections.excludeId(),
                                        Projections.include("reportedReviews.username", "reportedReviews.bookTitle", "reportedReviews.description"),
                                        Projections.computed("numberOfReports", new Document("$size", "$reportedReviews.reportedBy"))
                                )
                        ),
                        Aggregates.sort(sortStage),
                        Aggregates.skip(pageLength * (pageNumber - 1)),
                        Aggregates.limit(pageLength)
                )).into(new ArrayList<>());
    }
}

    /*
    private static final Logger LOGGER = LogManager.getLogger(MongoConnectionManager.class);
    private static MongoConnectionManager instance = null;
    private ConnectionString uri;
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> interestedCollection;

    private MongoConnectionManager()
    {
        //Create connections String to local instance
        uri = new ConnectionString("mongodb://localhost:27017");
        //Create a new connection to mongodb client
        client = MongoClients.create(uri);
        //Connect to mongolibrary database -> Creates db only when we insert a collection, and we insert something in it
        database = client.getDatabase("mongolibrary");
    }

    public static MongoConnectionManager getInstance()
    //Singleton Pattern
    {
        if(instance == null)
        {
            LOGGER.info("getInstance() | Creating a new MongoConnectionManager instance"); //LOG

            instance = new MongoConnectionManager();
        }
        LOGGER.info("getInstance() | Returning the MongoConnectionManager instance"); //LOG
        return instance;
    }

    public FindIterable<Document> retrieveDocumentsOrderedByOneKeyValue(String collection, String key, Integer order)
    {
        LOGGER.info("retrieveDocumentsOrderedByOneParameter() | Ordering " + collection + " with "); //LOG

        interestedCollection = database.getCollection(collection);

        switch (order){
            case -1:
                return interestedCollection.find().sort(descending(key));
            default:
                return interestedCollection.find().sort(ascending(key));
        }
    }

    public void addElement(String collection, Document element)
    //Adds user or book or review to mongoDB
    {
        LOGGER.info("addElement() | Adding to " + collection + ": " + element.toString()); //LOG

        interestedCollection = database.getCollection(collection);
        interestedCollection.insertOne(element);
    }

    public void removeElement(String collection, String key, String value)
    {
        interestedCollection = database.getCollection(collection);
        Bson filter = Filters.eq(key, value);
        interestedCollection.deleteOne(filter);
    }

    public void removeElement(String collection, String key1, String value1, String key2, String value2)
    {
        interestedCollection = database.getCollection(collection);
        Bson filter1 = Filters.eq(key1, value1);
        Bson filter2 = Filters.eq(key2, value2);
        interestedCollection.deleteOne(and(filter1, filter2));
    }

    public MongoCursor<Document> findDocumentByKeyValue(String collection, String key, String value)
    {
        LOGGER.info("findDocumentByField() | Searching in " + collection + " with key " + key + ": " +  value); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        //Returns the cursor with all the documents found
        return interestedCollection.find(eq(key, value)).iterator();
    }

    public MongoCursor<Document> findDocumentByKeyValue(String collection, String key1, String value1, String key2, String value2)
    {
        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter1 = Filters.eq(key1, value1);
        Bson filter2 = Filters.eq(key2, value2);
        return interestedCollection.find(and(filter1, filter2)).iterator();
    }

    public MongoCursor<Document> findEmbeddedDocumentByKeyValue(String collection, String key, Document value)
    {
        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        return interestedCollection.find(eq(key, Document.parse(value.toJson()))).iterator();
    }

    public void updateOneDocumentByKeyValue(String collection, String key, String value, String keyToUpdate, Object valueUpdated)
    {
        LOGGER.info("updateOneDocumentByField() | Updating in " + collection + " with key " + key + ": " +  value); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        interestedCollection.updateOne(Filters.eq(key, value), Updates.set(keyToUpdate, valueUpdated));
    }

    public void updateElementOfList(String collection, String key1, String value1, String key2, String value2, String List, Object listObject)
    {
        LOGGER.info("addElementToList() | Updating in customers with key: " + value1); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter1 = Filters.eq(key1, value1);
        Bson filter2 = Filters.eq(key2, value2);
        Bson update = Updates.set(List, listObject);
        interestedCollection.findOneAndUpdate(and(filter1, filter2), update);
    }

    public void addElementToList(String collection, String key, String value, String List, Object listObject)
    {
        LOGGER.info("addElementToList() | Updating in customers with key: " + value); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter = Filters.eq(key, value);
        Bson update = Updates.push(List, listObject);
        interestedCollection.findOneAndUpdate(filter, update);
    }

    public void addElementToList(String collection, String key1, String value1, String key2, String value2, String List, Object listObject)
    {
        LOGGER.info("addElementToList() | Updating in customers with key: "); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter1 = Filters.eq(key1, value1);
        Bson filter2 = Filters.eq(key2, value2);
        Bson update = Updates.push(List, listObject);
        interestedCollection.findOneAndUpdate(and(filter1, filter2), update);
    }

    public void removeElementFromList(String collection, String key, String value, String List, Object listObject)
    {
        LOGGER.info("removeElementFromList() | Updating in customers with key: " + value); //LOG

        //Preliminary collection set
        interestedCollection = database.getCollection(collection);

        Bson filter = Filters.eq(key, value);
        Bson update = Updates.pull(List, listObject);
        interestedCollection.findOneAndUpdate(filter, update);
    }

    @Override
    public void close() throws Exception
    //Terminates the connection
    {
        LOGGER.info("close() | Closing the connection"); //LOG

        client.close();
    }

    public ArrayList<Document> printAllBooksWithId()
    {
        //Preliminary collection set
        interestedCollection = database.getCollection("books");
        ArrayList<Document> result = interestedCollection.aggregate(
                Arrays.asList(
                        Aggregates.project(
                                Projections.fields(
                                        Projections.include("Title")
                                )
                        )
                )
        ).into(new ArrayList<>());

        return result;
    }
    */

