package it.unipi.dii.inginf.lsmdb.mongolibrary.service;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.LoginException;
import it.unipi.dii.inginf.lsmdb.mongolibrary.util.Password;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

public class LoginManager {

    private static final Logger LOGGER = LogManager.getLogger(LoginManager.class);

    /**
     * Checks if the user is in the database.
     * @param mode admin or customer entering into the database
     * @param username username used to sign up
     * @param password password used to secure the account
     */
    public void doLogin(String mode, String username, String password) throws LoginException, MongoException
    {
        //Connects the client to mongolibrary DB
        MongoConnectionManager mongoConnectionManager = MongoConnectionManager.getInstance();
        Neo4JConnectionManager neo4JConnectionManager = Neo4JConnectionManager.getInstance();
        //Searches for the specified user in DB
        MongoCursor<Document> cursor = mongoConnectionManager.findDocumentByKeyValue(mode, "username", username);

        if(cursor.hasNext())
        {
            LOGGER.info("loginCheck() | I have found the searched user"); //LOG

            Document userFound = cursor.next();

            Password userPassword = new Password(
                    userFound.getString("password"),
                    userFound.getString("salt")
            ); //CREATES PASSWORD OBJECT TO COMPARE IT

            if(mode.equals("customers")) //CUSTOMERS
            {
                if(userFound.getBoolean("status") == Boolean.FALSE)
                {
                    LOGGER.error("doLogin() | User has been banned"); //LOG
                    throw new LoginException("This user has been banned"); //ERROR
                }
                else if (userPassword.checkPassword(password) == Boolean.FALSE)
                {
                    LOGGER.error("doLogin() | Customer password doesn't match");//LOG
                    throw new LoginException("Error, check your password"); //ERROR
                }
                CustomerManager.getInstance().setMyCustomerData(userFound);//CREATES CUSTOMER AS GIVEN
            }
            else if (mode.equals("admins")) //ADMINS
            {
                if (userPassword.checkPassword(password) == Boolean.FALSE) {
                    LOGGER.error("doLogin() | Admin password doesn't match"); //LOG
                    throw new LoginException("Error, check your password"); //ERROR
                }
                AdminManager.getInstance().setMyAdminData(userFound); //CREATES ADMIN AS GIVEN
            }
            if (!neo4JConnectionManager.login(userFound.getString("username"))) {
                // TODO: ERRORE USER NON TROVATO
            }
        }
        else
        {
            LOGGER.error("loginCheck() | User not found"); //LOG
            throw new LoginException("Error, the specified user doesn't exist"); //ERROR
        }
    }
}
