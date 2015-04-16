package controllers;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import models.Learner;
import models.Session;
import models.UnapprovedUser;
import models.User;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import util.CSVUtil;

@With(SecuredAdminAction.class)
public class AdminController extends Controller {
    
    public static Result viewLearners() {
        List<Learner> learners = Learner.getAll();
        HashMap<String, List<Session>> learnerSchedules = new HashMap<String, List<Session>>();
        for(Learner l: learners) {
            learnerSchedules.put(l.email, Session.getLearnerSchedule(l.email));
        }
        
        return ok(views.html.studentsAdminView.render(learners, learnerSchedules, Learner.LEARNER_TYPES));
    }
    
    public static Result emailLearnerSchedule(String learnerId) {
        Learner learner = Learner.find.where().eq("uuid", learnerId).findUnique();
        
        Email email = new Email();
        email.setSubject("The following includes schedule details.");
        email.setFrom("admin@emory.edu");
        email.addTo(learner.email);
        email.setBodyText("Test");
        
        MailerPlugin.send(email);
        return status(NO_CONTENT);
    }
    
    public static Result emailAllStudents() {
        List<Learner> learners = Learner.getAll();
        for(Learner l: learners) {
            Email email = new Email();
            email.setSubject("The following includes schedule details.");
            email.setFrom("admin@emory.edu");
            email.addTo(l.email);
            email.setBodyText("Test");
            
            MailerPlugin.send(email);
        }
        
        return status(NO_CONTENT);
    }
    
    public static Result deleteLearner(String learnerId) {
        Learner learner = Learner.find.where().eq("uuid", learnerId).findUnique();
        if(learner != null) {
            Learner.deleteLearnerButFreeTheirSessions(learner);
        }
        return redirect(routes.AdminController.viewLearners());
    }

    public static Result viewAllCoordinators() {
        return ok(views.html.coordinatorsPage.render(
                UnapprovedUser.getAll(),
                User.getAllCoordinators()));
    }
    
    /**
     * Removes an UnapprovedUser from the database when 
     * and administrator presses the "delete" button
     * 
     * @param userEmail
     * @return
     */
    public static Result removeUnapprovedUser(String userEmail) {
        UnapprovedUser user = UnapprovedUser.find.byId(userEmail);
        if(user != null) {
            user.delete();
        }
        return redirect(routes.AdminController.viewAllCoordinators());
    }
    
    /**
     * Approves an UnapprovedUser by generating a random token
     * and assigning it to them.  Once assigned a token, the email
     * address associated with the UnapprovedUser is sent a custom link
     * to complete the creation of their account.
     * 
     * @param userEmail
     * @return
     */
    public static Result approveUnapprovedUser(String userEmail) {
        UnapprovedUser user = UnapprovedUser.find.byId(userEmail);
        
        user.token = UUID.randomUUID().toString();

        try {
            // Verifies that the URL is not malformed
            URL url = new URL("http://localhost:9000" + (routes.Application.setPassword(user.token)).url());
            user.save();
            
            Email email = new Email();
            email.setSubject("Test Email");
            email.setFrom("admin@emory.edu");
            email.addTo(userEmail);
            email.setBodyText("Go to " + url.toString() + 
                    " to complete your signup process");
            
            MailerPlugin.send(email);
            return status(NO_CONTENT);
        } catch (MalformedURLException e) {
            return badRequest(views.html.coordinatorsPage.render(
                    UnapprovedUser.getAll(),
                    User.getAllCoordinators()));
        }
    }

    public static Result viewCalendar() {
        return ok(views.html.calendarAdmin.render());
    }

    public static Result testViewCSVStuff() {
        return ok(views.html.testCSV.render());
    }

    public static Result generateLearnersCSV() {
        StringWriter csvStringWriter = new StringWriter();

        try {
            CSVUtil.writeLearnerCSV(Learner.getAllOrderByType(), csvStringWriter);
        } catch (IOException e) {
            return internalServerError("server error: unable to write CSV learner data");
        }

        InputStream csvResponseStream = new ByteArrayInputStream(
                csvStringWriter.toString().getBytes(StandardCharsets.UTF_8));
        //response().setContentType("text/csv");
        return ok(csvResponseStream);
    }

    public static Result generateCoordinatorsCSV() {
        StringWriter csvStringWriter = new StringWriter();

        try {
            CSVUtil.writeCoordinatorCSV(User.getAllCoordinators(), csvStringWriter);
        } catch (IOException e) {
            return internalServerError("server error: unable to write CSV coordinator data");
        }

        InputStream csvResponseStream = new ByteArrayInputStream(
                csvStringWriter.toString().getBytes(StandardCharsets.UTF_8));
        response().setContentType("text/csv");
        return ok(csvResponseStream);
    }

    /**
     * Deletes all learners.  Deletes will cascade to assigned sessions.
     */
    public static Result removeAllLearnersAndTheirSessions() {
        List<Learner> learners = Learner.find.all();
        for (Learner learner: learners) {
            Learner.deleteLearnerAndTheirSchedule(learner);
        }
        return status(NO_CONTENT);
    }

    /**
     * Deletes all learners, their sessions, then returns the CSV of learners.
     *
     * Temporary controller action:  We should not make this one operation in final product.
     */
    public static Result removeLearnersAndGiveCSV() {
        Result r = generateLearnersCSV();
        removeAllLearnersAndTheirSessions();
        return r;
    }
}
