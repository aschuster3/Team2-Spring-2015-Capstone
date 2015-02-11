import models.UnapprovedUser;
import models.User;

import org.junit.*;

import com.google.common.collect.ImmutableMap;

import play.mvc.Result;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;


/**
*
* Test functionality of the application
*
*/
public class ApplicationTest {
    
    @Before
    public void setup() {
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));

        new User("Sharon", "Norahs", "sharon@gmail.com", "kitty", true).save();
        new User("Bob", "Lob", "bob@gmail.com", "secret", false).save();
    }

    @Test
    public void tryAuthenticateUser() {
        // Test a coordinator          
        
        assertThat(User.authenticate("bob@gmail.com", "secret")).isNotNull();
        assertThat(User.authenticate("bob@gmail.com", "secret").isAdmin).isFalse();
        assertThat(User.authenticate("bob@gmail.com", "plop")).isNull();
        assertThat(User.authenticate("jeff@gmail.com", "secret")).isNull();
        
        // Test an administrator
        
        assertThat(User.authenticate("sharon@gmail.com", "kitty")).isNotNull();
        assertThat(User.authenticate("sharon@gmail.com", "kitty").isAdmin).isTrue();;
        assertThat(User.authenticate("sharon@gmail.com", "wrongpassword")).isNull();
    }
    
    @Test
    public void authenticateSuccess() {
        Result result = callAction(
            controllers.routes.ref.Application.authenticate(),
            fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                "email", "bob@gmail.com",
                "password", "secret"))
        );
        assertThat(303).isEqualTo(status(result));
        assertThat("bob@gmail.com").isEqualTo(session(result).get("email"));
    }
    
    @Test
    public void authenticateFailure() {
        Result result = callAction(
            controllers.routes.ref.Application.authenticate(),
            fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                "email", "bob@gmail.com",
                "password", "badpassword"))
        );
        assertThat(BAD_REQUEST).isEqualTo(status(result));
        assertThat(session(result).get("email")).isNull();
    }
    
    @Test
    public void createUserSuccess() {
        Result result = callAction(
                controllers.routes.ref.Application.createUser(),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "firstName", "Big",
                        "lastName", "Octopus",
                        "email", "new_email@gmail.com",
                        "department", "Aquatic Life"))
        );
        
        assertThat(303).isEqualTo(status(result));
        assertThat(UnapprovedUser.find.byId("new_email@gmail.com")).isNotNull();
    }
    
    @Test
    public void createUserFailsOnUsedEmail() {
        Result result = callAction(
                controllers.routes.ref.Application.createUser(),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "firstName", "Rob",
                        "lastName", "Lob",
                        "email", "bob@gmail.com",
                        "department", "Imagineering"))
        );
        
        User bob = User.find.byId("bob@gmail.com");
        
        assertThat(BAD_REQUEST).isEqualTo(status(result));
        assertThat("Bob").isEqualTo(bob.firstName);
        assertThat("Lob").isEqualTo(bob.lastName);
        assertThat("secret").isEqualTo(bob.password);
    }
    
    @Test
    public void authenticated() {
        Result result = callAction(
            controllers.routes.ref.Application.index(),
            fakeRequest().withSession("email", "bob@gmail.com")
        );
        assertThat(OK).isEqualTo(status(result));
    } 
    
    @Test
    public void notAuthenticated() {
        Result result = callAction(
            controllers.routes.ref.Application.index(),
            fakeRequest()
        );
        assertThat(303).isEqualTo(status(result));
        assertThat("/login").isEqualTo(header("Location", result));
    }
}
