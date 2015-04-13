import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeGlobal;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.start;
import static play.test.Helpers.status;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.routes;
import models.Learner;
import models.UnapprovedUser;
import models.User;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import play.libs.Json;
import play.mvc.Result;


public class CoordinatorControllerTest {
    
    @Before
    public void setup() {
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
        
        ObjectMapper mapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Json.setObjectMapper(mapper);
        User.create(new User("John", "Stamos", "fullhouse@gmail.com", "olsens", false));
    }
    
    @Test
    public void createLearnerSuccess() {
        
        
        Result result = callAction(
                controllers.routes.ref.CoordinatorController.createLearner(),
                fakeRequest().withSession("email", "fullhouse@gmail.com")
                             .withFormUrlEncodedBody(ImmutableMap.of(
                                "email", "student@gmail.com",
                                "firstName", "John",
                                "lastName", "Doe",
                                "learnerType", "Sub-I"))
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
                                "lastName", "Doe",
                                "learnerType", "Sub-I"))
            );
        
        assertThat(400).isEqualTo(status(result));
        Learner stu = Learner.find.byId("student@college.edu");
        assertThat(stu).isNull();
    }

    @Test
    public void createrLearner_FailsIfLearnerExists() {
        new Learner("email@gmail.com", "first", "last", "fullhouse@gmail.com", "type").save();

        Result result = callAction(
                routes.ref.CoordinatorController.createLearner(),
                fakeRequest()
                        .withSession("email", "fullhouse@gmail.com")
                        .withFormUrlEncodedBody(ImmutableMap.of(
                                "email", "email@gmail.com",
                                "firstName", "some name",
                                "lastName", "some last name",
                                "learnerType", "type"
                        ))
        );

        assertThat(status(result)).isEqualTo(400);
        Learner existingLearner = Learner.find.byId("email@gmail.com");
        assertThat(existingLearner.firstName).isEqualTo("first");
    }
}
