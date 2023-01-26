package it.unipi.dii.inginf.lsmdb.mongolibrary.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
/**
 Represent a password of the mongolibrary database.
 A Password object is composed by the SHA-256 hash of the real password
 and a randomly generated salt (in order to prevent rainbow table attacks)
 */
public class Password {
    private static final Logger LOGGER = LogManager.getLogger(Password.class);

    private static final String SALT_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SALT_SIZE = 8;
    private final String hashPassword;
    private final String salt;

    public Password ( String hashPassword, String salt) {
        this.hashPassword = hashPassword;
        this.salt = salt;
    }

    /**
     Generates hash and salt + contains the clearPassword for embedded document
     */
    public Password (String password) {
        try {
            // Generate alphanumeric string as salt
            salt = SecureRandom.getInstanceStrong()
                    .ints(
                            SALT_SIZE,
                            0,
                            SALT_CHARS.length())
                    .mapToObj(SALT_CHARS::charAt)
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Compute hash
        hashPassword = DigestUtils.sha256Hex(password + salt);
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public String getSalt() {
        return salt;
    }

    /**
     Check it the specified password's hash is the same of the current object
     */
    public boolean checkPassword (String password) {
        boolean result = DigestUtils.sha256Hex(password + salt).equals(hashPassword);
        if (!result) {
            LOGGER.error("checkPassword() | Password doesn't match");
        }
        return result;
    }

    public static Password fromDocument (Document document) {
        return new Password(
                document.getString("sha256"),
                document.getString("salt")
        );
    }

    public Document toDocument () {
        return new Document()
                .append("sha256", hashPassword)
                .append("salt", salt);
    }

    @Override
    public String toString() {
        return "Password{" +
                "hashPassword='" + hashPassword + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }
}
