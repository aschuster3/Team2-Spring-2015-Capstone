package controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import models.Learner;
import models.UnapprovedUser;
import models.User;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.EmailValidator;
import play.data.validation.Constraints.Required;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.adminIndex;
import views.html.coordinatorIndex;
import views.html.forgotPasswordForm;
import views.html.loginPage;
import views.html.passwordPage;
import views.html.registrationForm;
import views.html.studentsPage;
import views.html.testViewUnapprovedUsers;
import views.html.testViewUserSignup;

/**
 * The application controller for general purpose tasks such as logging in and out.
 *
 */
public class Application extends Controller {
    
    static Form<Login> loginForm = Form.form(Login.class);
    static Form<UnapprovedUser> signupForm = Form.form(UnapprovedUser.class);
    static Form<ForgotPassword> forgotPasswordTemplate = Form.form(ForgotPassword.class);
    static Form<Password> passwordForm = Form.form(Password.class);

    @Security.Authenticated(Secured.class)
    public static Result index() {
        String email = session().get("email");
        User user = User.find.where().eq("email", email).findUnique();
        
        if(user.isAdmin) {
            return ok(adminIndex.render());
        } else {
            return ok(coordinatorIndex.render());
        }
    }
    
    /**
     * Renders the login page.
     */
    public static Result login() {
        return ok(loginPage.render(loginForm));
    }

    public static Result signup(){
    	return ok(registrationForm.render(signupForm));
    }
    
    public static Result forgotPassword() {
        return ok(forgotPasswordForm.render(forgotPasswordTemplate));
    }
    
    public static Result createUnapprovedUser(){
    	Form<UnapprovedUser> filledForm = signupForm.bindFromRequest();
    	if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(registrationForm.render(filledForm));
        } else {
            UnapprovedUser.create(filledForm.get());
        	return redirect(routes.Application.login());
        }
    }
    
    public static Result authenticate() {
        Form<Login> filledForm = loginForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(loginPage.render(filledForm));
        } else {
            session().clear();
            session("email", filledForm.get().email);
            return redirect(
                routes.Application.index()
            );
        }
    }
    
    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(
            routes.Application.login()
        );
    }
    
    /**
     * A static class made for the purpose of generating a form.
     */
    public static class Login {
        
        public String email;
        public String password;
        
        /**
         * A required method to determine if a form has errors
         * 
         * @return A String describing the issue or null if there is none.
         */
        public String validate() {
            if (User.authenticate(email, password) == null) {
              return "Invalid email or password";
            }
            return null;
        }
    }
    
    /**
     * Once an UnapprovedUser has been emailed their custom
     * sign-up link, they will be made to set a password for their
     * account.  This method populates the screen where they will
     * be making their password.
     * 
     * @param token
     * @return
     */
    public static Result setPassword(String token) {
        UnapprovedUser user = UnapprovedUser.find.where().eq("token", token).findUnique();
        if (user == null) {
            return redirect(routes.Application.login());
        }
        return ok(passwordPage.render(passwordForm, user));
    }
    
    /**
     * Once an acceptable password is made, the user is saved and
     * then directed to the coordinator dashboard
     * 
     * @param email
     * @return
     */
    public static Result addNewUser(String email) {
        Form<Password> filledForm = passwordForm.bindFromRequest();
        UnapprovedUser user = UnapprovedUser.find.byId(email);
        if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(passwordPage.render(filledForm, user));
        } else {
            session().clear();
            User.create(
                new User(user.firstName,
                        user.lastName,
                        user.email,
                        filledForm.get().password,
                        false,
                        user.department)
            );
            session("email", user.email);
            user.delete();
            return redirect(
                routes.Application.index()
            );
        }
    }
    
    /**
     * A form filler for the password form used in creating a
     * a new user
     */
    public static class Password {
        
        public String password;
        public String passwordConfirm;
        
        public String validate() {
            if (password == null) {
                return "Darn";
            } else if (!password.equals(passwordConfirm)) {
              return "Passwords do not match!";
            }
            return null;
        }
    }
    
    public static class ForgotPassword {
        @Required
        public String email;
        
        public String validate() {
            EmailValidator val = new EmailValidator();
            if(!val.isValid(this.email)) {
                return "The email address is not valid.";
            }
            return null;
        }
    }
    
    public static Result sendNewPassword() {
        Form<ForgotPassword> filledForm = forgotPasswordTemplate.bindFromRequest();
        if(filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(forgotPasswordForm.render(forgotPasswordTemplate));
        } else {
            Email email = new Email();
            email.setSubject("Reset Password");
            email.setFrom("admin@emory.edu");
            email.addTo(filledForm.get().email);
            email.setBodyText("Your new password is available here");
            
            MailerPlugin.send(email);
            return redirect(routes.Application.login());
        }
    }
  
}
