package controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import models.Learner;
import models.UnapprovedUser;
import models.User;
import models.UserReset;
import play.Logger;
import play.core.Router;
import play.data.Form;
import play.data.validation.Constraints.EmailValidator;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.*;
import util.PasswordUtil;
import util.Tags;
import views.html.adminIndex;
import views.html.coordinatorIndex;
import views.html.coordinatorsPage;
import views.html.forgotPasswordForm;
import views.html.loginPage;
import views.html.passwordPage;
import views.html.registrationForm;
import views.html.resetPasswordPage;
import views.html.studentsPage;
import views.html.registrationFormConfirmation;
import views.html.support;
import views.html.coordinatorSupport;

/**
 * The application controller for general purpose tasks such as logging in and out.
 *
 */
public class Application extends Controller {
    
    static Form<Login> loginForm = Form.form(Login.class);
    static Form<UnapprovedUser> signupForm = Form.form(UnapprovedUser.class);
    static Form<ForgotPassword> forgotPasswordTemplate = Form.form(ForgotPassword.class);
    static Form<Password> passwordForm = Form.form(Password.class);

    /**
     * Determines whether the user is a coordinator or admin and renders the
     * correct home page.
     */
    @Security.Authenticated(Secured.class)
    public static Result index() {
        String email = session().get("email");
        User user = User.find.where().eq("email", email).findUnique();
        
        if(user.isAdmin) {
            return redirect(routes.AdminController.viewAllCoordinators());
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
    
    /**
     * Renders the signup page.
     */
    public static Result signup(){
    	return ok(registrationForm.render(signupForm));
    }
    
    /**
     * Renders the forgot password form.
     */
    public static Result forgotPassword() {
        return ok(forgotPasswordForm.render(forgotPasswordTemplate));
    }
    
    /**
     * Creates a new Unapproved User based on information the user provided in the
     * sign-up form.   
     */
    public static Result createUnapprovedUser(){
    	Form<UnapprovedUser> filledForm = signupForm.bindFromRequest();
    	if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(registrationForm.render(filledForm));
        } else {
            UnapprovedUser newUU = filledForm.get();
            String otherDepartment = filledForm.data().get("other_department");

            if (newUU.department.toLowerCase().startsWith("other")) {
                if (otherDepartment != null && !otherDepartment.isEmpty()) {
                    newUU.department = otherDepartment;
                } else {
                    filledForm.reject("Must specify 'other' department");
                    return badRequest(registrationForm.render(filledForm));
                }
            }

            UnapprovedUser.create(newUU);

        	return redirect(routes.Application.viewRegistrationFormConfirmation());
        }
    }
    
    /**
     * Renders the registration confirmation form.
     */
    public static Result viewRegistrationFormConfirmation() {
        return ok(registrationFormConfirmation.render());
    }
    
    /**
     * Authenticates a user based on the username and password entered in the login form.
     */
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
    
    /**
     * Clears the session information and renders the login page.
     */
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
     * then directed to the coordinator dashboard.
     * 
     * @param email
     * @return
     */
    public static Result addNewUser(String email) {
        Form<Password> filledForm = passwordForm.bindFromRequest();
        UnapprovedUser user = UnapprovedUser.find.byId(email);

        if (user == null) {
            return redirect(controllers.routes.Application.login());
        }

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
                            user.department,
                            user.phoneNumber)
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
        
        /**
         * A required method to determine if a form has errors
         * 
         * @return A String describing the issue or null if there is none.
         */
        public String validate() {
            String error = PasswordUtil.validateClearPassword(password);
            if (error != null) {
                return error;
            }

            if (!password.equals(passwordConfirm)) {
              return "Passwords do not match!";
            }
            return null;
        }
    }
    
    /**
     * Sends a new token to the email provided by the user in the forgot password form so 
     * that the user can reset their password.
     */
    public static Result sendNewPassword() {
        Form<ForgotPassword> filledForm = forgotPasswordTemplate.bindFromRequest();
        if(filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            // We want to clear the form, but keep the error message
            filledForm.data().clear();
            return badRequest(forgotPasswordForm.render(filledForm));
        } else {
            String userEmail = filledForm.get().email;
            
            String token = UUID.randomUUID().toString();

            try {
                // Verifies that the URL is not malformed
                sendResetPasswordEmail(userEmail, token);
                UserReset.create(userEmail, token);
                return redirect(routes.Application.viewForgotPasswordConfirmation());
            } catch (MalformedURLException e) {
                return internalServerError("Server error: unable to generate valid URL for password reset page.");
            }
        }
    }

    /**
     * Renders a page that confirms the new password has been sent to the user's email.
     */
    public static Result viewForgotPasswordConfirmation() {
        return ok(views.html.forgotPasswordFormConfirmation.render());
    }

    /**
     * Sends a new token to the email associated with the user ID so 
     * that the user can reset their password. Functionality only available to admins.
     * 
     * @param userUUID the ID of the user whose password is being reset
     */
    @With(SecuredAdminAction.class)
    public static Result sendNewPasswordToUser(String userUUID) {
        User coordinator = User.find.where().eq("uuid", userUUID).findUnique();
        if (coordinator == null) {
            return badRequest(views.html.coordinatorsPage.render(
                    UnapprovedUser.getAll(),
                    User.getAllCoordinators()
            ));
        }

        try {
            String token = UUID.randomUUID().toString();
            sendResetPasswordEmail(coordinator.email, token);
            UserReset.create(coordinator.email, token);
            return status(NO_CONTENT);
        } catch (MalformedURLException e) {
            return internalServerError("Server error: unable to generate valid URL for password reset page.");
        }
    }

    private static void sendResetPasswordEmail(String userEmail, String token) throws MalformedURLException {
        URL url = new URL(Tags.SITE_BASE_URL + (routes.Application.resetPassword(token)).url());

        Email email = new Email();
        email.setSubject(Tags.EMAIL_SUBJECT_RESET_PASSWORD);
        email.setFrom(Tags.ADMIN_EMAIL);
        email.addTo(userEmail);
        email.setBodyText("Your new password is available here: " + url.toString());

        MailerPlugin.send(email);
    }
    
    /**
     * Renders the reset password page.
     * 
     * @param token from email sent to user
     */
    public static Result resetPassword(String token) {
        UserReset userReset = UserReset.find.where().eq("resetToken", token).findUnique();
        if (userReset == null) {
            return redirect(routes.Application.login());
        }

        User user = User.find.where().eq("email", userReset.userEmail).findUnique();
        if (user == null) {
            return redirect(routes.Application.login());
        }

        return ok(resetPasswordPage.render(passwordForm, user));
    }
    
    /**
     * Updates the user's password with new password from the reset password form.
     * 
     * @param email of user
     */
    public static Result changeUserPassword(String email) {
        Form<Password> filledForm = passwordForm.bindFromRequest();
        User user = User.find.byId(email);
        if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(resetPasswordPage.render(filledForm, user));
        } else {
            session().clear();
            user.changePassword(filledForm.get().password);
            
            // Delete password reset token
            UserReset.find.byId(user.email).delete();
            
            session("email", user.email);
            return redirect(
                routes.Application.index()
            );
        }
    }
  

    /**
	 * A static class made for the purpose of generating a form for password retrieval.
	 */ 
    public static class ForgotPassword {
        @Required
        public String email;
        
        /**
         * A required method to determine if a form has errors
         * 
         * @return A String describing the issue or null if there is none.
         */
        public String validate() {
            EmailValidator val = new EmailValidator();
            if(!val.isValid(this.email)) {
                return "\"" + this.email + "\" is not a valid email address.";
            }

            if (User.find.byId(this.email) == null) {
                return "There is no account associated with the email address \"" + this.email + "\".";
            }
            return null;
        }
    }

    /**
     * Renders the admin support page.
     */
    public static Result viewSupportPage() {
        return ok(support.render());
    }

    /**
     * Renders the coordinator support page.
     */
    public static Result viewCoordinatorSupport() {
        return ok(coordinatorSupport.render());
    }
}
