package it.unipi.dii.inginf.lsmdb.mongolibrary.service;

import it.unipi.dii.inginf.lsmdb.mongolibrary.exceptions.LoginException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class Neo4JConnectionManager implements AutoCloseable{

    private final Driver driver;
    private static Neo4JConnectionManager instance = null;

    private static final Logger LOGGER = LogManager.getLogger(Neo4JConnectionManager.class);

    public Neo4JConnectionManager(){
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    public static Neo4JConnectionManager getInstance() {
        if(instance == null){
            instance = new Neo4JConnectionManager();
        }
        return instance;
    }

    /**
     * Check if user exist in N4J database
     * @param currentUser user currently logged
     * @throws LoginException a
     */
    public boolean login(String currentUser) throws LoginException {
        if(currentUser == null) {
            LOGGER.error("Neo4JConnectionManager login() | User is null");
            throw new LoginException("USER NOT FOUND EXCEPTION");
        }
        try (Session session = driver.session()) {
            var result = session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH (u:User {name: $username})" +
                            "RETURN u.name",
                    parameters("username", currentUser)));
            return result.hasNext();
        } catch (Neo4jException e) {
            return false;
        }
    }

    /**
     * Add a new user in the N4J database
     * @param username username of the user
     * @param nationality nationality of the user
     * @param birthyear birthyear of the user
     */
    public boolean register(String username, String nationality, int birthyear) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run("USE mongolibrary " +
                            "MERGE (u:User {name: $username, nationality: $nationality, birthyear: $birthyear})",
                    parameters("username", username, "nationality", nationality, "birthyear", birthyear)));
            return true;
        } catch (Neo4jException e) {
            return false;
        }
    }

    /**
     * Add a new book in the N4J database
     * @param title title of the book
     * @param category category/genre of the book
     */
    public boolean addBook(String title, String category, String author, Integer date) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run("USE mongolibrary " +
                            "MERGE (b:Book {title: $title, genre: $category, author: $author, publishDate: $date})",
                    parameters("title", title, "category", category, "author", author, "date", date)));
            return true;
        } catch (Neo4jException e) {
            return false;
        }
    }

    /**
     * Create the FOLLOWS relationship between the user currently logged and the user selected
     * @param currentUser user currently logged
     * @param username username of the user to be followed
     */
    public boolean addToFollowed(String currentUser, String username) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run("USE mongolibrary " +
                            "MATCH (a:User {name: $currentUser}),(b:User {name: $username}) " +
                            "MERGE (a)-[r:FOLLOWS]->(b)",
                    parameters("currentUser",currentUser, "username", username)));
            return true;
        } catch (Neo4jException e) {
            return false;
        }
    }

    /**
     * Return the list of users that the currently logged user is following
     * @param currentUser user currently logged
     * @return List of records
     */
    public List<org.neo4j.driver.Record> showFollowed(String currentUser) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH (u:User {name : $name})-[r:FOLLOWS]->(u2:User) " +
                            "RETURN u2.name",
                    parameters("name", currentUser)).list());
        }
    }

    public List<org.neo4j.driver.Record> showFollowers(String currentUser) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH (u:User)-[r:FOLLOWS]->(u2:User {name : $name}) " +
                            "RETURN u.name",
                    parameters("name", currentUser)).list());
        }
    }

    /**
     * Add a book to the reading list creating a WANTS_TO_READ relationship
     * @param currentUser user currently logged
     * @param book title of the book added to the reading list
     */
    public boolean addToReading(String currentUser, String book) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run("USE mongolibrary " +
                            "MATCH (a:User {name: $currentUser}),(b:Book {title: $book}) " +
                            "MERGE (a)-[r:WANTS_TO_READ]->(b)",
                    parameters("currentUser",currentUser, "book", book)));
            return true;
        } catch (Neo4jException e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Remove book from reading list
     * @param currentUser user currently logged
     * @param book title of the book to be removed
     */
    public boolean removeFromReading(String currentUser, String book){
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run("USE mongolibrary " +
                            "MATCH (:User {name: $name})-[r:WANTS_TO_READ]->(:Book {title: $title}) " +
                            "DELETE r",
                    parameters("name", currentUser, "title", book)));
            return true;
        } catch (Neo4jException e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Display the reading list
     * @param currentUser user currently logged
     * @return List of Record containing the title of the books in the reading list
     */
    public List<org.neo4j.driver.Record> showReadingList(String currentUser) {
        try (Session session = driver.session()) {
            session.executeRead(tx -> tx.run(""));
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH (u:User {name : $name})-[r:WANTS_TO_READ]->(b:Book) " +
                            "RETURN b.title",
                    parameters("name", currentUser)).list());
        }
    }

    /**
     * Add a book to the borrowing list creating a BORROWED relationship
     * @param currentUser user currently logged
     * @param book title of the book to be added
     */
    public boolean borrowBook(String currentUser, String book) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run("USE mongolibrary " +
                            "MATCH (a:User),(b:Book) " +
                            "WHERE a.name = $currentUser AND b.title = $book " +
                            "CREATE (a)-[r:BORROWED]->(b)",
                    parameters("currentUser",currentUser, "book", book)));
            return true;
        } catch (Neo4jException e) {
            return false;
        }
    }

    /**
     * Display the borrowing list
     * @param currentUser user currently logged
     * @return List of Record containing the title of the books in the borrowing list
     */
    public List<org.neo4j.driver.Record> showBorrowingList(String currentUser) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH (u:User {name : $name})-[r:BORROWED]->(b:Book) " +
                            "RETURN b.title",
                    parameters("name", currentUser)).list());
        }
    }

    /**
     * Show best 5 suggested books based on followers
     * @param currentUser user currently logged
     * @return List of Record containing the top 5 books read by followed users
     */
    public List<org.neo4j.driver.Record> suggestBookByFollows(String currentUser) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH (n:User {name: $name})--(u:User) " +
                            "MATCH (:User {name: u.name})-[:BORROWED]->(b:Book) " +
                            "RETURN b.title, COUNT(b.title) ORDER BY COUNT(b.title) DESC LIMIT 5 ",
                    parameters("name",currentUser)).list());
        }
    }

    public List<org.neo4j.driver.Record> suggestBookByFollowRecent(String currentUser, int starttime, int endtime) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH (n:User {name: $name})--(u:User) " +
                            "MATCH (:User {name: u.name})-[r:BORROWED]->(b:Book) " +
                            "WHERE $starttime < r.borrowdate < $endtime " +
                            "RETURN b.title, COUNT(b.title) ORDER BY COUNT(b.title) DESC LIMIT 5",
                    parameters("name",currentUser, "starttime", starttime, "endtime", endtime)).list());
        }
    }

    public List<org.neo4j.driver.Record> suggestBookByReadingList(String currentUser) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH (n:User {name: $name})--(u:User)" +
                            "MATCH (:User {name: u.name})-[:WANTS_TO_READ]->(b:Book) " +
                            "RETURN b.title, COUNT(b.title) ORDER BY COUNT(b.title) DESC LIMIT 5",
                    parameters("name",currentUser)).list());
        }
    }

    /**
     * Show best 5 suggested users based on books read
     * @param currentUser user currently logged
     * @return List of Record containing the top 5 users by shared books
     */
    public List<org.neo4j.driver.Record> suggestUserByBooks(String currentUser) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH (:User {name: $name})-[:BORROWED]->(:Book)<-[:BORROWED]-(v:User) " +
                            "RETURN v.name, COUNT(v.name) ORDER BY COUNT(v.name) DESC LIMIT 5",
                    parameters("name", currentUser)).list());
        }
    }

    /**
     * Show best 5 suggested users based on books read
     * @param currentUser user currently logged
     * @param starttime
     * @param endtime currenttime
     * @return
     */
    public List<org.neo4j.driver.Record> suggestUserByBooksRecent(String currentUser, int starttime, int endtime) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH (:User {name: $name})-[r:BORROWED]->(:Book)<-[:BORROWED]-(v:User) " +
                            "WHERE $starttime < r.borrowdate < $endtime " +
                            "RETURN v.name, COUNT(v.name) ORDER BY COUNT(v.name) DESC LIMIT 5",
                    parameters("name", currentUser, "starttime", starttime, "endtime", endtime)).list());
        }
    }

    /**
     *
     * @param currentUser
     * @return
     */
    public List<org.neo4j.driver.Record> suggestUserByReadingList(String currentUser) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH (:User {name: $name})-[:WANTS_TO_READ]->(:Book)<-[:WANTS_TO_READ]-(v:User) " +
                            "RETURN v.name, COUNT(v.name) ORDER BY COUNT(v.name) DESC LIMIT 5",
                    parameters("name", currentUser)).list());
        }
    }

    /**
     *
     * @param genre
     * @return
     */
    public List<org.neo4j.driver.Record> bestBooksByGenre(String genre) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH ()-[:BORROWED]->(b:Book {genre: $genre}) " +
                            "RETURN b.title, COUNT(b.title) ORDER BY COUNT(b.title) DESC LIMIT 10",
                    parameters("genre", genre)).list());
        }
    }

    /**
     *
     * @param genre
     * @param starttime
     * @param endtime
     * @return
     */
    public List<org.neo4j.driver.Record> bestBooksByGenreLastMonth(String genre, Integer starttime, Integer endtime) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH ()-[r:BORROWED]->(b:Book {genre: $genre}) " +
                            "WHERE $start < r.borrowdate < $end " +
                            "RETURN b.title, COUNT(b.title) ORDER BY COUNT(b.title) DESC LIMIT 10",
                    parameters("genre", genre, "start", starttime, "end", endtime)).list());
        }
    }

    /**
     *
     * @return
     */
    public List<org.neo4j.driver.Record> bestBooks() {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                    "MATCH ()-[:BORROWED]->(b:Book) " +
                    "RETURN b.title, COUNT(b.title) ORDER BY COUNT(b.title) DESC LIMIT 10").list());
        }
    }

    /**
     *
     * @param starttime
     * @param endtime
     * @return
     */
    public List<Record> bestBooksLastMonth(Integer starttime, Integer endtime) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run("USE mongolibrary " +
                            "MATCH ()-[r:BORROWED]->(b:Book) " +
                            "WHERE $month < r.borrowdate < $current " +
                            "RETURN b.title, COUNT(b.title) ORDER BY COUNT(b.title) DESC LIMIT 10",
                    parameters("current", endtime, "month", starttime)).list());
        }
    }
}
