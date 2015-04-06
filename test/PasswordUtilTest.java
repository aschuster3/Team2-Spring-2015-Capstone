import org.junit.Test;
import static org.junit.Assert.*;
import util.PasswordUtil;

public class PasswordUtilTest {

    private String testClearPassword = "the_clear_password";

    @Test
    public void encryptReturnsDifferentResult() {
        String encryptedPassword = PasswordUtil.encrypt(testClearPassword);
        assertNotEquals(encryptedPassword, testClearPassword);
    }

    @Test
    public void encryptIgnoresNullInput() {
        String encryptedPassword = PasswordUtil.encrypt(null);
        assertNull(encryptedPassword);
    }

    @Test
    public void checkReturnsTrueOnSameClearPassword() {
        String encryptedPassword = PasswordUtil.encrypt(testClearPassword);
        boolean checkResult = PasswordUtil.check(testClearPassword, encryptedPassword);
        assertTrue(checkResult);
    }

    @Test
    public void checkReturnsFalseOnEncryptedCandidat() {
        String encryptedPassword = PasswordUtil.encrypt(testClearPassword);
        boolean checkResult = PasswordUtil.check(encryptedPassword, encryptedPassword);
        assertFalse(checkResult);
    }

    @Test
    public void checkReturnsFalseOnIncorrectClearPassword() {
        String encryptedPassword = PasswordUtil.encrypt(testClearPassword);
        boolean checkResult = PasswordUtil.check("The_Clear_PassWord", encryptedPassword);
        assertFalse(checkResult);
    }

    @Test
    public void validateReturnsErrorOnShortPassword() {
        String validate = PasswordUtil.validateClearPassword("a");
        assertNotNull(validate);
    }

    @Test
    public void validateReturnsErrorOnNullPassword() {
        String validate = PasswordUtil.validateClearPassword(null);
        assertNotNull(validate);
    }

    @Test
    public void validateReturnsNullOnValidPassword() {
        String validate = PasswordUtil.validateClearPassword(testClearPassword);
        assertNull(validate);
    }
}
