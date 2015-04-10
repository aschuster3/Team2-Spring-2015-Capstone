import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.NO_CONTENT;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.TEMPORARY_REDIRECT;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeGlobal;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.start;
import static play.test.Helpers.status;

import controllers.routes;
import models.Learner;
import models.Session;
import models.UnapprovedUser;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.mvc.Result;

import java.util.Date;


public class AdminControllerTest {

    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String COORDINATOR_EMAIL = "coord@gmail.com";
    private static final String UNREGISTERED_EMAIL = "unregistered@gmail.com";

    
    @Before
    public void setup() {
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));

        User.create(new User("Admin", "User", ADMIN_EMAIL, "adminpassword", true));
        User.create(new User("Coord", "User", COORDINATOR_EMAIL, "coordpassword", false, "dept", "555-123-4567"));
    }

    @Test
    public void viewCoordinators_adminSession_succeeds() {
        Result result = callAction(
                controllers.routes.ref.AdminController.viewAllCoordinators(),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );

        assertThat(OK).isEqualTo(status(result));
    }

    @Test
    public void viewCoordinators_coordinatorSession_fails() {
        Result result = callAction(
                controllers.routes.ref.AdminController.viewAllCoordinators(),
                fakeRequest().withSession("email", COORDINATOR_EMAIL)
        );

        assertThat(303).isEqualTo(status(result));
    }

    @Test
    public void viewCoordinators_invalidSession_fails() {
        Result result = callAction(
                controllers.routes.ref.AdminController.viewAllCoordinators(),
                fakeRequest().withSession("email", UNREGISTERED_EMAIL)
        );

        assertThat(303).isEqualTo(status(result));
    }
    
    @Test
    public void approveUnapprovedUser_adminSession_succeeds() {
        UnapprovedUser user = new UnapprovedUser("Adrian", "Brody", 
                "abrody@hotmail.com", "Brody Questing");
        user.save();
        
        assertThat(user.token).isNull();
        
        Result result = callAction(
                controllers.routes.ref.AdminController.approveUnapprovedUser(user.email),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );
        
        // Gets the updated version of the user
        user = UnapprovedUser.find.byId(user.email);
        
        assertThat(status(result)).isEqualTo(NO_CONTENT);
        assertThat(user.token).isNotNull();
    }

    @Test
    public void approveUnapprovedUser_coordinatorSession_fails() {
        testApproveUnapprovedUser_Fails(COORDINATOR_EMAIL);
    }

    @Test
    public void approveUnapprovedUser_unregisteredSession_fails() {
        testApproveUnapprovedUser_Fails(UNREGISTERED_EMAIL);
    }

    private void testApproveUnapprovedUser_Fails(String nonAdminEmail) {
        UnapprovedUser user = new UnapprovedUser("Adrian", "Brody",
                "abrody@hotmail.com", "Brody Questing");
        user.save();

        assertThat(user.token).isNull();

        Result result = callAction(
                controllers.routes.ref.AdminController.approveUnapprovedUser(user.email),
                fakeRequest().withSession("email", nonAdminEmail)
        );

        user = UnapprovedUser.find.byId(user.email);
        assertThat(user.token).isNull();
    }
    
    @Test
    public void deleteUnapprovedUser_adminSession_succeeds() {
        UnapprovedUser user = new UnapprovedUser("Nicolas", "Cage", 
                "ncage@hotmail.com", "National Treasure");
        user.save();
        
        assertThat(user.token).isNull();
        
        Result result = callAction(
                controllers.routes.ref.AdminController.removeUnapprovedUser(user.email),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );
        
        // Gets the updated version of the user
        user = UnapprovedUser.find.byId(user.email);
        
        assertThat(user).isNull();
    }

    @Test
    public void deleteUnapprovedUser_coordinatorSession_fails() {
        testDeleteUnapprovedUser_Fails(COORDINATOR_EMAIL);
    }

    @Test
    public void deleteUnapprovedUser_unregisteredSession_fails() {
        testDeleteUnapprovedUser_Fails(UNREGISTERED_EMAIL);
    }

    private void testDeleteUnapprovedUser_Fails(String nonAdminEmail) {
        UnapprovedUser user = new UnapprovedUser("Nicolas", "Cage",
                "ncage@hotmail.com", "National Treasure");
        user.save();

        Result result = callAction(
                controllers.routes.ref.AdminController.removeUnapprovedUser(user.email),
                fakeRequest().withSession("email", nonAdminEmail)
        );

        user = UnapprovedUser.find.byId(user.email);
        assertThat(user).isNotNull();
    }

    @Test
    public void removeAllLearnersAndTheirSessions_RemovesLearnersFromDatabase() {
        Learner.create("email", "first", "last", "type", "owner");
        Learner.create("email2", "first2", "last2", "type", "owner2");

        Result result = callAction(
                routes.ref.AdminController.removeAllLearnersAndTheirSessions(),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );
        int resultingLearnerCount = Learner.find.all().size();

        assertThat(status(result)).isEqualTo(NO_CONTENT);
        assertThat(resultingLearnerCount).isEqualTo(0);
    }

    @Test
    public void removeAllLearnersAndTheirSessions_RemovesFilledSessionsFromDatabase() {
        Learner.create("email", "first", "last", "type", "owner");

        Session filledSession1 = new Session(null, "title", new Date(0));
        filledSession1.assignedLearner = "email";

        Session filledSession2 = new Session(null, "title", new Date(0));
        filledSession2.assignedLearner = "email";

        Session freeSession = new Session(null, "title", new Date(0));

        Session.create(filledSession1);
        Session.create(filledSession2);
        Session.create(freeSession);

        Result result = callAction(
                routes.ref.AdminController.removeAllLearnersAndTheirSessions(),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );
        int resultingSessionCount = Session.find.all().size();

        assertThat(resultingSessionCount).isEqualTo(1);
        assertThat(Session.find.byId(freeSession.id)).isNotNull();
    }
}
