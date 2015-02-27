package controllers;

import models.Session;
import models.UnapprovedUser;
import play.Logger;
import play.data.*;
import play.data.validation.ValidationError;
import play.mvc.*;
import views.html.*;

import java.util.List;
import java.util.Map.Entry;


/**
 * The session controller for tasks that involve adding, deleting and editing sessions.
 *
 */
public class SessionController extends Controller {

	static Form<Session> sessionForm = Form.form(Session.class);
	
	public static Result createSession(){
		Form<Session> filledForm = sessionForm.bindFromRequest();
    	if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(addSessionForm.render(sessionForm));
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
