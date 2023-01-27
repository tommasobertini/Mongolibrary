package it.unipi.dii.inginf.lsmdb.mongolibrary.service;

import com.mongodb.MongoException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.RegisterException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.RegisterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

public class RegisterManager {

    private static final Logger LOGGER = LogManager.getLogger(RegisterManager.class);

    /**
     * Checks if username and email are already in use.
     * @param username username to check
     * @param email email to check
     */
    public void doRegistrationCheck(String username, String email) throws RegisterException, MongoException
    {
        //Connect to MongoDB
        MongoConnectionManager mongoConnectionManager = MongoConnectionManager.getInstance();

        //Checks if username already in use
        if(mongoConnectionManager.findDocumentByKeyValue("customers", "username", username).hasNext())
        {
            LOGGER.error("doRegistrationCheck() | Username already in use"); //LOG
            throw new RegisterException("Username already in use"); //ERROR
        }

        //Checks if email already in use
        if(mongoConnectionManager.findDocumentByKeyValue("customers", "email", email).hasNext())
        {
            LOGGER.error("doRegistrationCheck() | Email already in use"); //LOG
            throw new RegisterException("Email already in use"); //ERROR
        }
    }

    /**
     * Saves the user as a document in the database.
     * @param user the document of the user to insert
     */
    public void doRegistration(Document user) throws MongoException
    {
        //Connect to MongoDB
        MongoConnectionManager mongoConnectionManager = MongoConnectionManager.getInstance();
        Neo4JConnectionManager neo4JConnectionManager = Neo4JConnectionManager.getInstance();
        //Add user if passed all the tests
        mongoConnectionManager.addElement("customers", user);
        var result = neo4JConnectionManager.register(user.getString("username"), user.getString("nationality"), user.getInteger("birthYear"));
        if (!result) {
            System.out.println("mongodb rollback");
            //Rollback in MongoDB to remove the book added
            mongoConnectionManager.removeElement("customers", "username", user.getString("username"));
        }
    }
}
