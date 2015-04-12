package controllers;

import java.util.ArrayList;
import java.util.List;

import models.ScheduleTemplate;
import models.Session;
import models.SessionTemplate;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import views.html.*;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;

@With(SecuredAdminAction.class)
public class TemplateController extends Controller {
	
	static Form<PreTemplate> templateForm = Form.form(PreTemplate.class);
	static Form<PreSession> sessionForm = Form.form(PreSession.class);
	
	public static class PreTemplate {
		
        @Required
        public String title;
       
        public String validate() {
            
            if((title == null || title.equals(""))) {
                return "Must have a title for the template";
            }
            if(ScheduleTemplate.find.where().eq("title", title).findUnique()!=null){
            	return "Template with title \"" +  title + "\" already exists.";
            }
            
            return null;
        }
    }

	public static class PreSession {
		
        @Required
        public String title;
        
        @Required
        public int week;
        
        @Required
        public int day;
        
        @Required
        public String isAM;
        
        public String schedule;
                       
        public String validate() {
            if((title == null || title.equals(""))) {
                return "Must have a title for the template";
            }
            if(week <= 0){
            	return "Must have a positive integer value for week.";
            }
            if(day <= 0 || day > 7){
            	return "Must have a positive integer value between 1 and 7 for day.";
            }
            if(SessionTemplate.find.where().eq("title", title).eq("week", week).eq("day", day)
            		.eq("isAM", isAM).eq("schedule.title", schedule).findUnique()!=null){
            	return "This session already exists in this schedule.";
            } 
            
            return null;
        }
    }

	public static Result createScheduleTemplate(){

	Form<PreTemplate> filledForm = templateForm.bindFromRequest();
        
        if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(manageTemplates.render(ScheduleTemplate.find.all(), filledForm, sessionForm, ""));
        } else {
            PreTemplate template = filledForm.get();
            ScheduleTemplate.create(template.title);
            return redirect(routes.TemplateController.templates());
        }
	}
	
	public static Result createSessionTemplate(String scheduleID){
		Form<PreSession> filledForm = sessionForm.bindFromRequest();
		/*adding schedule to form, converting a JSON and then rebinding so that it goes back through the
		validator*/
		PreSession template = filledForm.get();
		template.schedule = scheduleID;
		filledForm = sessionForm.bind(Json.toJson(template));
        boolean AMtime = true;
        if(template.isAM.equalsIgnoreCase("false")){
        	AMtime = false;
        }
		
		if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(manageTemplates.render(ScheduleTemplate.find.all(), templateForm, filledForm, scheduleID));
        }
		else {
            SessionTemplate st = new SessionTemplate(template.title, template.week, template.day, AMtime);
            return addSessionToSchedule(scheduleID, st);
        }
	}
	
	public static Result templates(){
		return ok(manageTemplates.render(ScheduleTemplate.find.all(), templateForm, sessionForm, ""));
	}
	
	public static Result addSessionToSchedule(String scheduleTitle, SessionTemplate session){
		//SessionTemplate session = SessionTemplate.find.byId(sessionID);
		ScheduleTemplate schedule = ScheduleTemplate.find.byId(scheduleTitle);
		if (session == null) {
			return badRequest("add failed: session with id " + session.id + " does not exist");
		}
		if (schedule == null) {
			return badRequest("add failed: schedule with title " + scheduleTitle + " does not exist");
		}
		if(!schedule.addSession(session)){
			return badRequest("session with title " + session.title + ", on week "
					+ session.week + " day " + session.day + "in the " + session.isAM + " already exists in this schedule");
		}
		schedule.save();
		return redirect(routes.TemplateController.templates());
		
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
