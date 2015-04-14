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
import play.twirl.api.Html;
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
            return ok(studentsPageRenderWithForm(email, learnerForm));
        }
    }

    public static Result createLearner() {
        Form<Learner.PreLearner> filledForm = learnerForm.bindFromRequest();
        String ownerEmail = session().get("email");
        
        // Fix need for Owner info

        if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(studentsPageRenderWithForm(ownerEmail, filledForm));
        }

        Learner.PreLearner learner = filledForm.get();

        /*
         * Leaving this out of PreLearner.validate so that validate can
         * be used for updating Learners, too.
         */
        if (Learner.find.byId(learner.email) != null) {
            filledForm.reject("This email is already in use for a student");
            return badRequest(studentsPageRenderWithForm(ownerEmail, filledForm));
        }

        if (learnerFormUsesOtherLearnerType(filledForm)) {
            learner.learnerType = filledForm.data().get("otherLearnerType");
        }

        Learner.create(learner.email, learner.firstName, learner.lastName, learner.learnerType, ownerEmail);
        return redirect(routes.CoordinatorController.students());
    }

    private static boolean learnerFormUsesOtherLearnerType(Form<Learner.PreLearner> validFilledForm) {
        String learnerType = validFilledForm.data().get("learnerType");
        String otherLearnerType = validFilledForm.data().get("otherLearnerType");
        return learnerType.toLowerCase().startsWith("other")
                && otherLearnerType != null
                && !otherLearnerType.isEmpty();
    }

    public static Result updateLearner(String learnerEmail) {
        return ok(play.libs.Json.toJson(Learner.find.byId(learnerEmail)));
    }

    public static Result viewCoordinatorCalendar() {
        return ok(calendarCoordinator.render(Learner.getAllOwnedBy(session().get("email"))));
    }

    private static Html studentsPageRenderWithForm(String ownerEmail, Form<Learner.PreLearner> form) {
        List<Learner> learners = Learner.getAllOwnedBy(ownerEmail);
        HashMap<String, List<Session>> learnerSchedules = new HashMap<String, List<Session>>();
        for(Learner l: learners) {
            learnerSchedules.put(l.email, Session.getLearnerSchedule(l.email));
        }
        return studentsPage.render(learners, learnerSchedules, Learner.LEARNER_TYPES, form);
    }

}
