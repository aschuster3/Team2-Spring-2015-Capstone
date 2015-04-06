import models.UnapprovedUser;
import models.User;
import models.UserReset;

import org.junit.*;

import com.google.common.collect.ImmutableMap;

import play.Logger;
import play.mvc.Result;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;


/**
*
* Test functionality of the application
*
*/
public class ApplicationTest {

    private static final String ADMIN_EMAIL = "sharon@gmail.com";
    
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
    public void createUnapprovedUserSuccess() {
        Result result = callAction(
                controllers.routes.ref.Application.createUnapprovedUser(),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "firstName", "Big",
                        "lastName", "Octopus",
                        "email", "new_email@gmail.com",
                        "department", "Aquatic Life",
                        "phoneNumber", "917-555-1234"))
        );
        
        assertThat(303).isEqualTo(status(result));
        assertThat(UnapprovedUser.find.byId("new_email@gmail.com")).isNotNull();
    }
    
    @Test
    public void createUnapprovedUserFailsOnUsedEmail() {
        Result result = callAction(
                controllers.routes.ref.Application.createUnapprovedUser(),
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
    public void completeAccountCreation() {
        UnapprovedUser user = new UnapprovedUser("Fran", "the Man", "useless@address.com", "birds");
        UnapprovedUser.create(user);
        
        String userEmail = user.email;
        

        assertThat(UnapprovedUser.find.byId(userEmail)).isNotNull();
        
        Result result = callAction(
                controllers.routes.ref.Application.addNewUser(user.email),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "password", "Iluvcats2010",
                        "passwordConfirm", "Iluvcats2010"))
        );
        
        assertThat(303).isEqualTo(status(result));
        assertThat(UnapprovedUser.find.byId(userEmail)).isNull();
        assertThat(User.find.byId(userEmail)).isNotNull();
    }
    
    @Test
    public void accountCreationFailedBadPassword() {
        UnapprovedUser user = new UnapprovedUser("Fran", "the Man", "useless@address.com", "birds");
        UnapprovedUser.create(user);
        
        String userEmail = user.email;
        

        assertThat(UnapprovedUser.find.byId(userEmail)).isNotNull();
        
        Result result = callAction(
                controllers.routes.ref.Application.addNewUser(user.email),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "password", "Iluvcats2010",
                        "passwordConfirm", "woofbark"))
        );
        
        assertThat(BAD_REQUEST).isEqualTo(status(result));
        assertThat(UnapprovedUser.find.byId(userEmail)).isNotNull();
        assertThat(User.find.byId(userEmail)).isNull();
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
    
    @Test
    public void requestPasswordResetSuccess() {
        new User("Larry", "Lobster", "bluelagoon@gmail.com", "posewithme", false).save();
        
        Result result = callAction(
                controllers.routes.ref.Application.sendNewPassword(),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "email", "bluelagoon@gmail.com"))
            );
        
        assertThat(303).isEqualTo(status(result));
        assertThat(UserReset.find.byId("bluelagoon@gmail.com")).isNotNull();
    }
    
    @Test
    public void requestPasswordResetFail() {
        
        Result result = callAction(
                controllers.routes.ref.Application.sendNewPassword(),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "email", "somerandomemail@gmail.com"))
            );
        
        assertThat(BAD_REQUEST).isEqualTo(status(result));
        assertThat(UserReset.find.byId("somerandomemail@gmail.com")).isNull();
    }

    @Test
    public void adminInitiatedPasswordResetSuccess() {
        User.create(new User("first", "last", "email@gmail.com", "password", false));

        Result result = callAction(
                controllers.routes.ref.Application.sendNewPasswordToUser("email@gmail.com"),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );

        assertThat(status(result)).isEqualTo(303);
        assertThat(UserReset.find.byId("email@gmail.com")).isNotNull();
    }

    @Test
    public void adminInitiatedPasswordResetFail() {
        Result result = callAction(
                controllers.routes.ref.Application.sendNewPasswordToUser("unregisteredUser@gmail.com"),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        assertThat(UserReset.find.byId("unregisteredUser@gmail.com")).isNull();
    }
    
    @Test
    public void resetPasswordSucceed() {
        new User("Jeff", "Jefferson", "jeff@gmail.com", "password", false).save();
        
        User user = User.find.byId("jeff@gmail.com");
        UserReset.create(user.email, "abcdefghijklmnopqrstuvwxyz");
        
        assertThat(user.password).isEqualTo("password");
        
        Result result = callAction(
                controllers.routes.ref.Application.changeUserPassword(user.email),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "password", "newPassword",
                        "passwordConfirm", "newPassword"))
            );
        
        user = User.find.byId("jeff@gmail.com");
        
        assertThat(303).isEqualTo(status(result));
        assertThat(user.password).isEqualTo("newPassword");
    }
}
