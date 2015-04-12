import controllers.routes;
import models.UnapprovedUser;
import models.User;
import models.UserReset;

import org.junit.*;

import com.google.common.collect.ImmutableMap;

import play.Logger;
import play.mvc.Result;
import util.PasswordUtil;

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

        User.create(new User("Sharon", "Norahs", "sharon@gmail.com", "kitty", true));
        User.create(new User("Bob", "Lob", "bob@gmail.com", "secret", false));
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
        assertThat(PasswordUtil.check("secret", bob.password));
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
    public void accountCreation_NoExistingUnapprovedUser_RedirectsToLogin() {
        Result result = callAction(
                routes.ref.Application.addNewUser("non-unapproved-user-email@gmail.com"),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "password", "ThePassword",
                        "passwordConfirm", "ThePassword"
                ))
        );

        assertThat(status(result)).isEqualTo(303);
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
        User.create(new User("Larry", "Lobster", "bluelagoon@gmail.com", "posewithme", false));
        
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
    public void twoConsecutivePasswordResetRequests_UsesSecondTokenForAuthorization() {
        User.create(new User("Larry", "Lobster", "bluelagoon@gmail.com", "posewithme", false));

        Result firstResult = callAction(
                controllers.routes.ref.Application.sendNewPassword(),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "email", "bluelagoon@gmail.com"))
        );
        String firstToken = UserReset.find.byId("bluelagoon@gmail.com").resetToken;

        Result secondResult = callAction(
                controllers.routes.ref.Application.sendNewPassword(),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "email", "bluelagoon@gmail.com"))
        );
        String secondToken = UserReset.find.byId("bluelagoon@gmail.com").resetToken;

        assertThat(status(secondResult)).isEqualTo(303);
        assertThat(secondToken).isNotEqualTo(firstToken);
    }

    @Test
    public void adminInitiatedPasswordResetSuccess() {
        User.create(new User("first", "last", "email@gmail.com", "password", false));

        Result result = callAction(
                controllers.routes.ref.Application.sendNewPasswordToUser("email@gmail.com"),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );

        assertThat(status(result)).isEqualTo(NO_CONTENT);
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
        User.create(new User("Jeff", "Jefferson", "jeff@gmail.com", "password", false));
        
        User user = User.find.byId("jeff@gmail.com");
        UserReset.create(user.email, "abcdefghijklmnopqrstuvwxyz");
        
        assertThat(PasswordUtil.check("password", user.password)).isTrue();
        
        Result result = callAction(
                controllers.routes.ref.Application.changeUserPassword(user.email),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "password", "newPassword",
                        "passwordConfirm", "newPassword"))
            );
        
        user = User.find.byId("jeff@gmail.com");
        
        assertThat(303).isEqualTo(status(result));
        assertThat(PasswordUtil.check("newPassword", user.password)).isTrue();
    }

    @Test
    public void cannotViewResetPasswordPageWithInvalidToken() {
        User.create(new User("Jeff", "Jefferson", "jeff@gmail.com", "password", false));
        String validToken = "validtoken";
        String invalidToken = "___invalidtoken___";

        User user = User.find.byId("jeff@gmail.com");
        UserReset.create(user.email, validToken);

        Result result = callAction(
                routes.ref.Application.resetPassword(invalidToken),
                fakeRequest()
        );

        assertThat(status(result)).isEqualTo(303);
    }
}
