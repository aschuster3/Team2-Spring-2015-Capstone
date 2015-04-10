package controllers;

import java.util.ArrayList;

import models.ScheduleTemplate;
import models.Session;
import models.SessionTemplate;
import play.data.Form;
import play.mvc.*;
import views.html.*;
import play.data.validation.Constraints.Required;

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
        public boolean isAM;
        
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
            		.eq("isAM", isAM).findUnique()!=null){
            	return "Session with title " + title + ", on week " + week + 
					" day " + day + "in the " + isAM + " already exists in this schedule";
            } //MIGHT BE AN ISSUE WITH MULTIPLE SESSIONS WITH SAME INFO BUT DIFFERENT SCHEDULES!!
            
            return null;
        }
    }

	public static Result createScheduleTemplate(){

	Form<PreTemplate> filledForm = templateForm.bindFromRequest();
        
        if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(manageTemplates.render(ScheduleTemplate.find.all(), filledForm, sessionForm));
        } else {
            PreTemplate template = filledForm.get();
            ScheduleTemplate.create(template.title);
            return redirect(routes.TemplateController.templates());
        }
	}
	
	public static Result createSessionTemplate(){
		Form<PreSession> filledForm = sessionForm.bindFromRequest();

		if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(manageTemplates.render(ScheduleTemplate.find.all(), templateForm, filledForm));
        } else {
            PreSession template = filledForm.get();
            SessionTemplate st = SessionTemplate.create(template.title, template.week, template.day, template.isAM);
            //addSessionToSchedule(template.schedule, st.id);
            //System.out.println(st.schedule.title);
            return status(204);
        }
	}
	
	public static Result templates(){
		return ok(manageTemplates.render(ScheduleTemplate.find.all(), templateForm, sessionForm));
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
