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

	    public static class PreTemplate {
        @Required
        public String title;
       
        public String validate() {
            
            if((title == null || title.equals(""))) {
                return "Must have a title for the template";
            }
            if(ScheduleTemplate.find.where().eq("title", title).findUnique()!=null){
            	return "Template with title " + title + " already exists.";
            }
            
            return null;
        }
    }

	public static Result createScheduleTemplate(){

	Form<PreTemplate> filledForm = templateForm.bindFromRequest();
        
        if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(manageTemplates.render(ScheduleTemplate.find.all(), filledForm));
        } else {
            PreTemplate template = filledForm.get();
            ScheduleTemplate.create(template.title);
            return redirect(routes.TemplateController.templates());
        }
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
		return ok(manageTemplates.render(ScheduleTemplate.find.all(), templateForm));
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
