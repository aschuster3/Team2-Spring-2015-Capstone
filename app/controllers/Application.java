package controllers;

import models.User;
import play.data.*;
import play.mvc.*;
import views.html.*;

/**
 * The application controller for general purpose tasks such as logging in and out.
 *
 */
public class Application extends Controller {
    
    static Form<Login> loginForm = Form.form(Login.class);
    static Form<User> signupForm = Form.form(User.class);

    @Security.Authenticated(Secured.class)
    public static Result index() {
        String email = session().get("email");
        User user = User.find.where().eq("email", email).findUnique();
        
        if(user.isAdmin) {
            return ok(index.render("You are an admin"));
        } else {
            return ok(index.render("You are a coordinator"));
        }
    }
    
    /**
     * Renders the login page.
     */
    public static Result login() {
        return ok(login.render(loginForm));
    }

    public static Result signup(){
    	return ok(signup.render(signupForm));
    }
    
    public static Result createUser(){
    	Form<User> filledForm = signupForm.bindFromRequest();
    	if (filledForm.hasErrors()) {
            return badRequest(signup.render(filledForm));
        } else {
        	return redirect(routes.Application.index());
        }
    }
    
    public static Result authenticate() {
        Form<Login> filledForm = loginForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(login.render(filledForm));
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
              return "Invalid user or password";
            }
            return null;
        }
    }
   
  
}
