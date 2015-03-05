package controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Session;
import models.UnapprovedUser;
import play.Logger;
import play.data.*;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.*;
import views.html.*;

import java.util.List;
import java.util.Map.Entry;


/**
 * The session controller for tasks that involve adding, deleting and editing sessions.
 *
 */
@Security.Authenticated(Secured.class)
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

	public static Result jsonAllSessions() {
		return ok(Json.toJson(Session.getAll()));
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result updateSession(String id) {
		JsonNode json = request().body().asJson();
		Session session = Json.fromJson(json, Session.class);

		if (!session.id.equals(id)) {
			return badRequest("update failed: parameter id does not match session object id");
		}

		if (Session.find.byId(id) == null) {
			Session.create(session);
			return status(CREATED, Json.toJson(session));
		} else {
			session.update();
			return status(204);
		}
	}

	@With(SecuredAdminAction.class)
	@BodyParser.Of(BodyParser.Json.class)
	public static Result jsonCreateSession() {
		JsonNode json = request().body().asJson();
		Session session = Json.fromJson(json, Session.class);

		Session.create(session);
		return status(CREATED);
	}

	@With(SecuredAdminAction.class)
	public static Result deleteSession(String id) {
		Session session = Session.find.byId(id);
		if (session == null) {
			return badRequest("delete failed: session with id '" + id + "' does not exist");
		} else {
			session.delete();
			return status(204);
		}
	}
}
