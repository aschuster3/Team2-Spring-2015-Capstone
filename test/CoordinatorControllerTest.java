import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeGlobal;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.start;
import static play.test.Helpers.status;
import models.Learner;
import models.UnapprovedUser;
import models.User;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import play.mvc.Result;


public class CoordinatorControllerTest {
    
    @Before
    public void setup() {
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
        
        new User("John", "Stamos", "fullhouse@gmail.com", "olsens", false).save();
    }
    
    @Test
    public void createLearnerSuccess() {
        
        
        Result result = callAction(
                controllers.routes.ref.CoordinatorController.createLearner(),
                fakeRequest().withSession("email", "fullhouse@gmail.com")
                             .withFormUrlEncodedBody(ImmutableMap.of(
                                "email", "student@gmail.com",
                                "firstName", "John",
                                "lastName", "Doe"))
            );
        
        assertThat(303).isEqualTo(status(result));
        Learner stu = Learner.find.byId("student@gmail.com");
        assertThat(stu).isNotNull();
        assertThat(stu.ownerEmail).isEqualTo("fullhouse@gmail.com");
    }
    
    @Test
    public void createLearnerFailedBadEmail() {
        
        
        Result result = callAction(
                controllers.routes.ref.CoordinatorController.createLearner(),
                fakeRequest().withSession("email", "fullhouse@gmail.com")
                             .withFormUrlEncodedBody(ImmutableMap.of(
                                "email", "notanemail",
                                "firstName", "Name",
                                "lastName", "Doe"))
            );
        
        assertThat(400).isEqualTo(status(result));
        Learner stu = Learner.find.byId("student@college.edu");
        assertThat(stu).isNull();
    }
}
