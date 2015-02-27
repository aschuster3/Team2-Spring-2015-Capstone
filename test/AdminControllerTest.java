import static org.fest.assertions.Assertions.assertThat;
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
    
    @Before
    public void setup() {
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));

        new User("Sharon", "Norahs", "sharon@gmail.com", "kitty", true).save();
    }
    
    @Test
    public void adminApproveUnapprovedUser() {
        UnapprovedUser user = new UnapprovedUser("Adrian", "Brody", 
                "abrody@hotmail.com", "Brody Questing");
        user.save();
        
        assertThat(user.token).isNull();
        
        Result result = callAction(
                controllers.routes.ref.AdminController.approveUnapprovedUser(user.email),
                fakeRequest().withSession("email", "sharon@gmail.com")
        );
        
        // Gets the updated version of the user
        user = UnapprovedUser.find.byId(user.email);
        
        assertThat(303).isEqualTo(status(result));
        assertThat(user.token).isNotNull();
    }
    
    @Test
    public void adminDeleteUnapprovedUser() {
        UnapprovedUser user = new UnapprovedUser("Nicolas", "Cage", 
                "ncage@hotmail.com", "National Treasure");
        user.save();
        
        assertThat(user.token).isNull();
        
        Result result = callAction(
                controllers.routes.ref.AdminController.removeUnapprovedUser(user.email),
                fakeRequest().withSession("email", "sharon@gmail.com")
        );
        
        // Gets the updated version of the user
        user = UnapprovedUser.find.byId(user.email);
        
        assertThat(user).isNull();
    }
}
