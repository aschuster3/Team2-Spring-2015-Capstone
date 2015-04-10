package controllers;

import java.util.HashMap;
import java.util.List;

import models.Learner;
import models.Session;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.calendarCoordinator;
import views.html.studentsPage;

@Security.Authenticated(Secured.class)
public class CoordinatorController extends Controller {
    
    static Form<Learner.PreLearner> learnerForm = Form.form(Learner.PreLearner.class);
    
    public static Result students() {
        String email = session().get("email");
        User user = User.find.where().eq("email", email).findUnique();
        
        if(user.isAdmin) {
            return redirect(routes.Application.index());
        } else {
            List<Learner> learners = Learner.getAllOwnedBy(email);
            HashMap<String, List<Session>> learnerSchedules = new HashMap<String, List<Session>>();
            for(Learner l: learners) {
                learnerSchedules.put(l.email, Session.getLearnerSchedule(l.email));
            }
            
            return ok(studentsPage.render(learners, learnerSchedules, Learner.LEARNER_TYPES, learnerForm));
        }
    }

    public static Result createLearner() {
        Form<Learner.PreLearner> filledForm = learnerForm.bindFromRequest();
        String ownerEmail = session().get("email");
        
        // Fix need for Owner info
        
        if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            List<Learner> learners = Learner.getAllOwnedBy(ownerEmail);
            HashMap<String, List<Session>> learnerSchedules = new HashMap<String, List<Session>>();
            for(Learner l: learners) {
                learnerSchedules.put(l.email, Session.getLearnerSchedule(l.email));
            }
            
            return badRequest(studentsPage.render(learners, learnerSchedules, Learner.LEARNER_TYPES, filledForm));
        } else {
            Learner.PreLearner learner = filledForm.get();
            Learner.create(learner.email, learner.firstName, learner.lastName, learner.learnerType, ownerEmail);
            return redirect(routes.CoordinatorController.students());
        }
    }

    public static Result updateLearner(String learnerEmail) {
        return ok(play.libs.Json.toJson(Learner.find.byId(learnerEmail)));
    }

    public static Result viewCoordinatorCalendar() {
        return ok(calendarCoordinator.render(Learner.getAllOwnedBy(session().get("email"))));
    }

}
