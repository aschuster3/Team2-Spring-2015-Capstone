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

    public static Result index() {
        return ok("You did it!");
    }

    public static Result index(String name) {
        return ok("Hello " + name);
    }
    
    /**
     * Redirects the user to the login page.
     */
    public static Result login() {
        return ok(login.render(loginForm));
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
