package controllers;

import models.Learner;
import models.User;
import play.data.Form;
import play.data.validation.Constraints.EmailValidator;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.studentsPage;

@Security.Authenticated(Secured.class)
public class CoordinatorController extends Controller {
    
    static Form<PreLearner> learnerForm = Form.form(PreLearner.class);
    
    public static Result students() {
        String email = session().get("email");
        User user = User.find.where().eq("email", email).findUnique();
        
        if(user.isAdmin) {
            return redirect(routes.Application.index());
        } else {
            return ok(studentsPage.render(Learner.getAllOwnedBy(email), learnerForm));
        }
    }
    
    public static class PreLearner {
        @Required
        public String email;
        
        @Required
        public String firstName;
        
        @Required
        public String lastName;
        
        public String validate() {
            
            if((firstName == null && !firstName.equals("")) || (lastName == null && !lastName.equals(""))) {
                return "Must have a name for the student";
            }
            
            EmailValidator val = new EmailValidator();
            if(!val.isValid(email)) {
                return "The email address is not valid.";
            }
            
            return null;
        }
    }
    
    public static Result createLearner() {
        Form<PreLearner> filledForm = learnerForm.bindFromRequest();
        String ownerEmail = session().get("email");
        
        // Fix need for Owner info
        
        if (filledForm.hasGlobalErrors() || filledForm.hasErrors()) {
            return badRequest(studentsPage.render(Learner.getAllOwnedBy(ownerEmail), filledForm));
        } else {
            PreLearner learner = filledForm.get();
            Learner.create(learner.email, learner.firstName, learner.lastName, ownerEmail);
            return redirect(routes.CoordinatorController.students());
        }
    }

}
