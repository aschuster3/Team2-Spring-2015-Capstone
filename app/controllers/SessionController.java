package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import play.data.*;
import play.libs.Json;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.*;
import views.html.*;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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

	public static Result getSession(String id) {
		Session session = Session.find.byId(id);

		if (session == null) {
			return badRequest("No session with id <" + id + ">" + " exists");
		} else {
			return ok(Json.toJson(session));
		}
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result updateSession(String id) {
		JsonNode json = request().body().asJson();
		Session sessionWithNewData = Json.fromJson(json, Session.class);

		if (!sessionWithNewData.id.equals(id)) {
			return badRequest("update failed: parameter id does not match session object id");
		}
		
		if (sessionWithNewData.assignedLearner != null && sessionWithNewData.assignedLearner.equals("error")) {
		    return badRequest("update failed: please choose a valid student");
		}

		Session existingSession = Session.find.byId(id);

		if (existingSession == null) {
			Session.create(sessionWithNewData);
			return status(CREATED, Json.toJson(sessionWithNewData));
		}

		// trying to assign a new learner to a taken session
		String currentLearnerId = existingSession.assignedLearner;
		String newLearnerId = sessionWithNewData.assignedLearner;
		if (currentLearnerId != null
				&& newLearnerId != null
				&& !currentLearnerId.equals(newLearnerId)) {
			return badRequest("update failed: session is already taken by another learner");
		}

		sessionWithNewData.update();

		/* Model.update() ignores null values */
		if (sessionWithNewData.assignedLearner == null) {
			Ebean.update(sessionWithNewData, new HashSet<String>(Arrays.asList("assignedLearner")));
		}
		return status(204);
	}

	@With(SecuredAdminAction.class)
	@BodyParser.Of(BodyParser.Json.class)
	public static Result jsonCreateSession() {
		JsonNode json = request().body().asJson();
		Session session = Json.fromJson(json, Session.class);

		Session.create(session);
		return status(CREATED, Json.toJson(session));
	}

	@With(SecuredAdminAction.class)
	public static Result deleteSession(String id) {
		Session session = Session.find.byId(id);
		if (session == null) {
			return status(204);
		}
		sendDeletedSessionNotifications(session);
		session.delete();
		return status(204);
	}

	private static void sendDeletedSessionNotifications(Session session) {
		if (session == null || session.assignedLearner == null) {
			return;
		}

		Learner learner = Learner.find.byId(session.assignedLearner);
		if (learner != null) {
			sendDeletedSessionNotificationTo(learner.email, session);
		}
		
		if (learner.ownerEmail == null) {
			return;
		}

		User coordinator = User.find.byId(learner.ownerEmail);
		if (coordinator != null) {
			sendDeletedSessionNotificationTo(coordinator.email, session);
		}
	}

	private static void sendDeletedSessionNotificationTo(String emailAddress, Session session) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
		String dateString = dateFormat.format(session.date);

		String subject = "[Emory Dermatology] Clinic Cancellation for " + dateString;
		String message = "This is a notification that the following clinic has been cancelled:\n"
				+ "\n\t" + session.title
				+ "\n\t" + session.physician
				+ "\n\t" + dateString + " " + (session.isAM ? "(AM)" : "(PM)");

		Email email = new Email();
		email.addTo(emailAddress);
		email.setSubject(subject);
		email.setBodyText(message);

		MailerPlugin.send(email);
	}

	/**
	 * Expects JSON with two fields:
	 *   1) recurringSessionGroup [object],
	 *   2) session [object]
	 * @return list of created sessions
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result createSessionRecurrenceGroup() {
		JsonNode json = request().body().asJson();
		JsonNode recGroupJson = json.get("recurringSessionGroup");
		JsonNode sessionJson = json.get("session");

		if (recGroupJson == null || sessionJson == null) {
			return status(BAD_REQUEST,
					"JSON must contain a 'session' object " +
							"and a 'recurringSessionGroup' object");
		}

		RecurringSessionGroup recGroup =
				Json.fromJson(recGroupJson, RecurringSessionGroup.class);
		Session baseSession =
				Json.fromJson(sessionJson, Session.class);

		RecurringSessionGroup.create(recGroup);

		baseSession.recurringGroupId = recGroup.id;
		Session.create(baseSession);

		List<Session> createdSessions = recGroup.generateNewOccurrences(52);
		createdSessions.add(baseSession);

		return status(CREATED, Json.toJson(createdSessions));
	}

	/**
	 * Deletes the RecurrenceGroup object, along with all
	 * sessions that are in the Recurrence group that come after
	 * the session with the given sessionId
	 *
	 * i.e. delete all on or after Session.find.byId(sessionId)
	 */
	public static Result deleteSessionRecurrenceGroup(String sessionId) {
		Session startSession = Session.find.byId(sessionId);
		RecurringSessionGroup recGroup =
				RecurringSessionGroup.find.byId(startSession.recurringGroupId);

		if (startSession == null || recGroup == null) {
			return badRequest("invalid sessionId, or sessionId not part of recurrence group");
		}

		List<Session> sessionsInRecGroup = recGroup.allSessions();

		for (Session session: sessionsInRecGroup) {
			if (session.date.after(startSession.date) || session.date.equals(startSession.date)) {
				session.delete();
			} else {
				session.recurringGroupId = null;
				session.save();
			}
		}
		recGroup.delete();

		return status(204);
	}
	
	public static Result createScheduleSessions(String scheduleID, String date){
		ScheduleTemplate schedule = ScheduleTemplate.find.byId(scheduleID);
		if(schedule == null){
			return badRequest("Failed: Schedule with title " + scheduleID + " does not exist");
		}
		Date startDate; 
		DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    	try{
    		startDate = formatter.parse(date);
    		for (SessionTemplate session: schedule.sessions){
    			int days = (session.week - 1)*7 + (session.day - 1);
    			Calendar cal = Calendar.getInstance();
    	        cal.setTime(startDate);
    	        cal.add(Calendar.DATE, days); 
    			Session.create(new Session(null, session.location, cal.getTime(), session.physician, session.isAM, scheduleID));
    		}
    	}
    	catch(Exception e){
    		return badRequest("Failed: Date not formatted correctly.");
		}
		return status(200);
	}
}
