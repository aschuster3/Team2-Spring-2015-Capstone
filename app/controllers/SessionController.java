package controllers;

import models.Session;
import models.UnapprovedUser;
import play.Logger;
import play.data.*;
import play.mvc.*;
import views.html.*;

/**
 * The session controller for tasks that involve adding, deleting and editing sessions.
 *
 */
public class SessionController extends Controller {

	static Form<Session> sessionForm = Form.form(Session.class);
	
	public static Result createSession(){
		Form<Session> filledForm = sessionForm.bindFromRequest();
    	if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(addSessionForm.render(filledForm));
        } else {
            Session.create(filledForm.get());
        	return redirect(routes.Application.login());
        }
	}
	
	public static Result sessions(){
		return ok(addSessionForm.render(sessionForm));
	}
	
	public static Result addSession(){
	    	return ok(addSessionForm.render(sessionForm));
	    }
}
