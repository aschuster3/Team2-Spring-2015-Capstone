package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

import models.Learner;
import models.Session;
import models.User;
import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.twirl.api.Html;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import util.Tags;
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
    
    public static Result emailLearnerSchedule(String learnerId) {
        Learner learner = Learner.find.where().eq("uuid", learnerId).findUnique();
        
        List<Session> schedule = Session.getLearnerSchedule(learner.email);
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(getHeader());
        
        Email email = new Email();
        email.setSubject("The following includes schedule details.");
        email.setFrom(Tags.ADMIN_EMAIL);
        email.addTo(learner.email);
        sb.append("<table style=\"width:80%\" border=\"1\">"
                + "<tr> <th>Clinic</th> <th>Physician</th> <th>Session Time</th> </tr>");
        for(Session session: schedule) {
            sb.append("<tr>");
            sb.append("<td>");
            sb.append(session.title);
            sb.append("</td>");
            sb.append("<td>");
            sb.append(session.physician);
            sb.append("</td>");
            sb.append("<td>");
            if(session.isAM) {
                sb.append("AM");
            } else {
                sb.append("PM");
            }
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        
        sb.append(getFooter());
        
        email.setBodyHtml(sb.toString());
        
        MailerPlugin.send(email);
        return status(NO_CONTENT);
    }
    
    public static Result emailAllStudents() {
        List<Learner> learners = Learner.getAllOwnedBy(session().get("email"));
        
        String header = getHeader();
        String footer = getFooter();
        
        List<Session> schedule;
        StringBuilder sb;
        for(Learner l: learners) {
            sb = new StringBuilder();
            sb.append(header);
            schedule = Session.getLearnerSchedule(l.email);
            
            Email email = new Email();
            email.setSubject("Welcome to the Emory Dermatology Rotation");
            email.setFrom(Tags.ADMIN_EMAIL);
            email.addTo(l.email);
            sb.append("<table style=\"width:80%\" border=\"1\">"
                    + "<tr> <th>Clinic</th> <th>Physician</th> <th>Session Time</th> </tr>");
            for(Session session: schedule) {
                sb.append("<tr>");
                sb.append("<td>");
                sb.append(session.title);
                sb.append("</td>");
                sb.append("<td>");
                sb.append(session.physician);
                sb.append("</td>");
                sb.append("<td>");
                if(session.isAM) {
                    sb.append("AM");
                } else {
                    sb.append("PM");
                }
                sb.append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            sb.append(footer);
            
            email.setBodyHtml(sb.toString());
            
            MailerPlugin.send(email);
        }
        
        return status(NO_CONTENT);
    }
    
    private static String getHeader() {
        String filePath = Play.application().path().getAbsolutePath() 
                + "/public/templates/email_template_head.txt";
        String everything = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if(br != null) {
                try{
                    br.close();
                } catch(IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        
        return everything;
    }
    
    private static String getFooter() {
        String filePath = Play.application().path().getAbsolutePath() 
                + "/public/templates/email_template_tail.txt";
        String everything = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if(br != null) {
                try{
                    br.close();
                } catch(IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        
        return everything;
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
