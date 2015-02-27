import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.TEMPORARY_REDIRECT;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeGlobal;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.start;
import static play.test.Helpers.status;
import models.UnapprovedUser;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Result;


public class AdminControllerTest {

    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String COORDINATOR_EMAIL = "coord@gmail.com";
    private static final String UNREGISTERED_EMAIL = "unregistered@gmail.com";

    
    @Before
    public void setup() {
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));

        new User("Admin", "User", ADMIN_EMAIL, "adminpassword", true).save();
        new User("Coord", "User", COORDINATOR_EMAIL, "coordpassword", false, "dept").save();
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
    public void viewCoordinators_unregisteredSession_fails() {
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
        
        assertThat(303).isEqualTo(status(result));
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
}
