import models.Learner;
import models.User;
import org.junit.*;
import static org.junit.Assert.*;
import util.CSVUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

public class CSVUtilTest {

    private StringWriter testWriter;

    @Before
    public void setup() {
        testWriter = new StringWriter();
    }

    @Test
    public void writeLearnerCSV() throws IOException {
        List<Learner> testLearners = Arrays.asList(
                new Learner("email1", "first1", "last1", "owner1", "type1"),
                new Learner("email2", "first2", "last2", "owner2", "type2")
        );

        // enclose strings in back-quotes
        String expectedCSVString = ("`Last Name`,`First Name`,`Email`,`Type`,`Coordinator Email`" + "\n"
                + "`last1`,`first1`,`email1`,`type1`,`owner1`" + "\n"
                + "`last2`,`first2`,`email2`,`type2`,`owner2`" + "\n").replace('`', '"');

        CSVUtil.writeLearnerCSV(testLearners, testWriter);
        String actualCSVString = testWriter.toString();

        assertEquals(expectedCSVString, actualCSVString);
    }

    @Test
    public void writeCoordinatorCSV() throws IOException {
        List<User> testCoordinators = Arrays.asList(
                new User("first1", "last1", "email1", "password1", false, "dept1", "phonenumber1"),
                new User("first2", "last2", "email2", "password2", false, "dept2", "phonenumber2")
        );
        String expectedCSVString = ("`Last Name`,`First Name`,`Email`,`Department`,`Phone Number`" + "\n"
                + "`last1`,`first1`,`email1`,`dept1`,`phonenumber1`" + "\n"
                + "`last2`,`first2`,`email2`,`dept2`,`phonenumber2`" + "\n").replace('`', '"');

        CSVUtil.writeCoordinatorCSV(testCoordinators, testWriter);
        String actualCSVString = testWriter.toString();

        assertEquals(expectedCSVString, actualCSVString);
    }
}
