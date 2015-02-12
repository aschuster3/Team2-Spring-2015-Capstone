package controllers;

import models.User;
import models.UnapprovedUser;
import play.data.*;
import play.mvc.*;
import views.html.*;

/**
 * The application controller for general purpose tasks such as logging in and out.
 *
 */
public class Application extends Controller {
    
    static Form<Login> loginForm = Form.form(Login.class);
    static Form<UnapprovedUser> signupForm = Form.form(UnapprovedUser.class);
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
        return TODO;
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
    
    
    public static Result removeUnapprovedUser(String userEmail) {
        UnapprovedUser user = UnapprovedUser.find.byId(userEmail);
        if(user != null) {
            user.delete();
        }
        return redirect(routes.Application.fetchUU());
    }
    
    public static Result approveUnapprovedUser(String userEmail) {
        // Send an email with the user info
        return TODO;
    }
    
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
    
    public static Result setPassword(String token) {
        UnapprovedUser user = UnapprovedUser.find.where().eq("token", token).findUnique();
        if (user == null) {
            return redirect(routes.Application.login());
        }
        return ok(testViewUserSignup.render(passwordForm, user));
    }
    
    public static Result addNewUser(String email) {
        Form<Password> filledForm = passwordForm.bindFromRequest();
        UnapprovedUser user = UnapprovedUser.find.byId(email);
        if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(testViewUserSignup.render(filledForm, user));
        } else {
            session().clear();
            User.create(
                new User(user.firstName, user.lastName, user.email, filledForm.get().password, false)
            );
            session("email", user.email);
            user.delete();
            return redirect(
                routes.Application.index()
            );
        }
    }
   
    /**
     * This is a temporary test action to view current unapproved users
     * 
     * @return
     */
    public static Result fetchUU() {
        return ok(testViewUnapprovedUsers.render(UnapprovedUser.getAll()));
    }
  
}
