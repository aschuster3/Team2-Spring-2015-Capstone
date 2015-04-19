package controllers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import models.Learner;
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

		@Required
		public String learnerType;

		public String validate() {

			if ((title == null || title.equals(""))) {
				return "Must have a title for the template";
			}
			if ((learnerType == null || learnerType.equals(""))) {
				return "Must have a learner type allowed to sign up for this schedule.";
			}
			if (ScheduleTemplate.find.where().eq("title", title).findUnique() != null) {
				return "Template with title \"" + title + "\" already exists.";
			}

			return null;
		}
	}

	public static class PreSession {

		@Required
		public String location;

		@Required
		public String physician;

		@Required
		public int week;

		@Required
		public int day;

		@Required
		public String isAM;

		public String schedule;

		public String validate() {
			if (location == null || location.equals("")) {
				return "Must have a location for the session.";
			}
			if ((physician == null || physician.equals(""))) {
				return "Must have a physician for the session.";
			}
			if (week < 1) {
				return "Must have a positive integer value for week.";
			}
			if (day < 1 || day > 7) {
				return "Must have a positive integer value between 1 and 7 for day.";
			}
			if (SessionTemplate.find.where().eq("location", location)
					.eq("physician", physician).eq("week", week).eq("day", day)
					.eq("isAM", isAM).eq("schedule.uuid", schedule)
					.findUnique() != null) {
				return "This session already exists in this schedule.";
			}
			if (SessionTemplate.find.where().eq("physician", physician)
					.eq("week", week).eq("day", day).eq("isAM", isAM)
					.eq("schedule.uuid", schedule).findUnique() != null) {
				return physician + " is already booked at this time.";
			}
			if (SessionTemplate.find.where().eq("week", week).eq("day", day)
					.eq("isAM", isAM).eq("schedule.uuid", schedule)
					.findUnique() != null) {
				return "Session already exists at this time on this day in this schedule.";
			}
			return null;
		}
	}

	public static Result createScheduleTemplate() {

		Form<PreTemplate> filledForm = templateForm.bindFromRequest();

		if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
			return badRequest(manageTemplates.render(
					ScheduleTemplate.find.all(), filledForm, sessionForm, "",
					Learner.LEARNER_TYPES));
		} else {
			PreTemplate template = filledForm.get();
			ScheduleTemplate.create(template.title, template.learnerType);
			return redirect(routes.TemplateController.templates());
		}
	}

	public static Result createSessionTemplate(String scheduleID) {
		Form<PreSession> filledForm = sessionForm.bindFromRequest();
		/*
		 * adding schedule to form, converting a JSON and then rebinding so that
		 * it goes back through the validator
		 */
		if (filledForm.errors().size() > 1) {
			return badRequest(manageTemplates.render(
					ScheduleTemplate.find.all(), templateForm, filledForm,
					scheduleID, Learner.LEARNER_TYPES));
		}

		PreSession template = filledForm.get();
		template.schedule = scheduleID;
		filledForm = sessionForm.bind(Json.toJson(template));

		if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
			return badRequest(manageTemplates.render(
					ScheduleTemplate.find.all(), templateForm, filledForm,
					scheduleID, Learner.LEARNER_TYPES));
		} else {
			boolean AMtime = true;
			if (template.isAM.equalsIgnoreCase("false")) {
				AMtime = false;
			}
			SessionTemplate st = new SessionTemplate(template.location,
					template.physician, template.week, template.day, AMtime);
			return addSessionToSchedule(scheduleID, st);
		}
	}

	public static Result templates() {
		return ok(manageTemplates.render(ScheduleTemplate.find.all(),
				templateForm, sessionForm, "", Learner.LEARNER_TYPES));
	}

	public static Result jsonTemplates() {
		return ok(Json.toJson(ScheduleTemplate.find.all()));
	}

	public static Result addSessionToSchedule(String scheduleID,
			SessionTemplate session) {
		ScheduleTemplate schedule = ScheduleTemplate.find.where()
				.eq("uuid", scheduleID).findUnique();
		if (session == null) {
			return badRequest("add failed: session with id " + session.id
					+ " does not exist");
		}
		if (schedule == null) {
			return badRequest("add failed: schedule with id " + scheduleID
					+ " does not exist");
		}
		if (!schedule.addSession(session)) {
			return badRequest("session with physician " + session.physician
					+ ", on week " + session.week + " day " + session.day
					+ "in the " + session.isAM
					+ " already exists in this schedule");
		}
		schedule.save();
		return redirect(routes.TemplateController.templates());

	}

	public static Result deleteSessionFromSchedule(String id) {
		SessionTemplate session = SessionTemplate.find.byId(id);
		if (session == null) {
			return badRequest("delete failed: session with id " + id
					+ " does not exist");
		} else {
			session.delete();
			return status(200);
		}
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result updateSessionTemplate(String sessionId) {
		JsonNode sessionJson = request().body().asJson();
        SessionTemplate updatedSessionData = Json.fromJson(sessionJson, SessionTemplate.class);
        SessionTemplate existingSession = SessionTemplate.find.byId(sessionId);
    
        if (existingSession == null) {
            return badRequest("Session ID does not exist");
        }
        
        if(existingSession.schedule!=null){
        	SessionTemplate st = SessionTemplate.find.where().eq("week", updatedSessionData.week)
        		.eq("day", updatedSessionData.day).eq("isAM", updatedSessionData.isAM)
        		.eq("schedule", existingSession.schedule).findUnique();
        	if(st != null){
        		if(st.id != existingSession.id)
        		return badRequest("A session already exists at this time in this schedule.");
        	}
        }

    	existingSession.updateLocation(updatedSessionData.location);
    	existingSession.updatePhysician(updatedSessionData.physician);
    	existingSession.updateWeek(updatedSessionData.week);
    	existingSession.updateDay(updatedSessionData.day);
    	existingSession.updateAM(updatedSessionData.isAM);

        return ok(Json.toJson(SessionTemplate.find.byId(existingSession.id)));
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public static Result updateLearnerType(String scheduleId){
		JsonNode scheduleJson = request().body().asJson();
		ScheduleTemplate updatedLearnerType = Json.fromJson(scheduleJson, ScheduleTemplate.class);
		ScheduleTemplate existingSchedule = ScheduleTemplate.find.byId(scheduleId);
		
		if (existingSchedule == null) {
            return badRequest("Schedule ID does not exist");
        }
        existingSchedule.updateLearnerType(updatedLearnerType.learnerType);

        return ok(Json.toJson(ScheduleTemplate.find.byId(existingSchedule.uuid)));
	}

}
