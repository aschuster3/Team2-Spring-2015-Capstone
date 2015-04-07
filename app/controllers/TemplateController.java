package controllers;

import java.util.ArrayList;

import models.ScheduleTemplate;
import models.Session;
import models.SessionTemplate;
import play.data.Form;
import play.mvc.*;
import views.html.*;

@With(SecuredAdminAction.class)
public class TemplateController extends Controller {
	
	public static Result createScheduleTemplate(String title){
		if(ScheduleTemplate.find.byId(title)!=null)
			return badRequest("Schedule with title " + title + " already exists.");
		ScheduleTemplate.create(title);
		return status(204);
	}
	
	public static Result createSessionTemplate(String title, int week, int day, boolean isAM){
		if(SessionTemplate.find.where().eq("title", title).eq("week", week).eq("day", day).eq("isAM", isAM).findUnique()!=null){
			return badRequest("session with title " + title + ", on week " + week + 
					" day " + day + "in the " + isAM + " already exists in this schedule");
		}
		SessionTemplate.create(title, week, day, isAM);
		return status(204);	
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
