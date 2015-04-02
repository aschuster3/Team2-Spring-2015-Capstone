package controllers;

import java.util.ArrayList;

import models.ScheduleTemplate;
import models.Session;
import models.SessionTemplate;
import play.data.Form;
import play.mvc.*;
import views.html.*;

@Security.Authenticated(Secured.class)
public class TemplateController extends Controller {
	
	public static Result createScheduleTemplate(String title){
		return TODO;
	}
	
	public static Result createSessionTemplate(){
		return TODO;
	}
	
	public static Result templates(){
		return ok(manageTemplates.render(ScheduleTemplate.find.all()));
	}
	
	public static Result addSessionToSchedule(String scheduleTitle, String sessionID){
		SessionTemplate session = SessionTemplate.find.byId(sessionID);
		ScheduleTemplate schedule = ScheduleTemplate.find.byId(scheduleTitle);
		if (session == null) {
			return badRequest("add failed: session with id " + sessionID + " does not exist");
		}
		if (schedule == null) {
			return badRequest("add failed: schedule with title " + scheduleTitle + " does not exist");
		}
		if(!schedule.addSession(session)){
			return badRequest("session with title " + session.title + ", on week "
					+ session.week + " day " + session.day + "in the " + session.isAM + " already exists in this schedule");
		}
		return status(200);
		
	}
	
	@With(SecuredAdminAction.class)
	public static Result deleteSessionFromSchedule(String id) {
		SessionTemplate session = SessionTemplate.find.byId(id);
		if (session == null) {
			return badRequest("delete failed: session with id " + id + " does not exist");
		} else {
			session.delete();
			return status(200);
		}
	}
} 
