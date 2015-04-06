package util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static final int MIN_PASSWORD_LENGTH = 8;

    public static String encrypt(String clearPassword) {
        if (clearPassword == null) {
            return null;
        }
        return BCrypt.hashpw(clearPassword, BCrypt.gensalt());
    }

    public static boolean check(String clearCandidate, String encryptedPassword) {
        if (clearCandidate == null || encryptedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(clearCandidate, encryptedPassword);
    }

    /**
     * Similar to Model.validate() method.
     *
     * @param clearPassword
     * @return null on valid clear password, otherwise String describing the error
     */
    public static String validateClearPassword(String clearPassword) {
        String lengthError = "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long";
        if (clearPassword == null) {
            return lengthError;
        }
        return clearPassword.length() >= MIN_PASSWORD_LENGTH ? null : lengthError;
    }
}
