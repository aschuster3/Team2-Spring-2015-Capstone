package controllers;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import models.Learner;
import models.Session;
import models.UnapprovedUser;
import models.User;
import play.Play;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import util.CSVUtil;
import util.Tags;

@Security.Authenticated(Secured.class)
@With(SecuredAdminAction.class)
/**
 * Primary Controller for most admin related actions.
 *
 */
public class AdminController extends Controller {
    
    /**
     * Renders all active learners and their schedules.
     * 
     * @return
     */
    public static Result viewLearners() {
        List<Learner> learners = Learner.getAll();
        HashMap<String, List<Session>> learnerSchedules = new HashMap<String, List<Session>>();
        for(Learner l: learners) {
            learnerSchedules.put(l.email, Session.getLearnerSchedule(l.email));
        }
        
        return ok(views.html.studentsAdminView.render(learners, learnerSchedules, Learner.LEARNER_TYPES));
    }
    
    /**
     * Emails an individual learner their schedule with a pre-generated header and footer.
     * The header and footer are located in "public/template" and are named email_template_head.html
     * and email_template_tail.html.
     * 
     * @param learnerId
     * @return
     */
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
    
    /**
     * Sends every active learner their schedule.
     * 
     * @return
     */
    public static Result emailAllStudents() {
        List<Learner> learners = Learner.getAll();

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
    
    /**
     * Removes a learner from the database.
     * 
     * @param learnerId The UUID cooresponding to the learner
     * @return
     */
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
            email.setSubject("Approval to the Emory Dermatology Rotation Application");
            email.setFrom(Tags.ADMIN_EMAIL);
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

    /**
     * Action that generates the learnerCSV and presents it to the Admin.
     * 
     * @return
     */
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

    /**
     * Action that generates the coordinatorCSV and presents it to the Admin.  Currently,
     * there is no way for the Admin to access this from the web app.
     * 
     * @return
     */
    public static Result generateCoordinatorsCSV() {
        StringWriter csvStringWriter = new StringWriter();

        try {
            CSVUtil.writeCoordinatorCSV(User.getAllCoordinators(), csvStringWriter);
        } catch (IOException e) {
            return internalServerError("Server error: unable to write CSV coordinator data");
        }

        InputStream csvResponseStream = new ByteArrayInputStream(
                csvStringWriter.toString().getBytes(StandardCharsets.UTF_8));
        response().setContentType("text/csv");
        return ok(csvResponseStream);
    }

    /**
     * Deletes all finished learners.  Deletes will cascade to assigned sessions.
     */
    public static Result removeFinishedLearnersAndTheirSessions() {
        List<Learner> learners = Learner.find.all();
        List<Session> sessions;
        
        boolean learnerHasFinishedSessions = true;
        for (Learner learner: learners) {
            sessions = Session.getLearnerSchedule(learner.email);
            
            // today    
            Calendar cal = new GregorianCalendar();
            // reset hour, minutes, seconds and millis
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            Date today = cal.getTime();

            for(Session s: sessions) {
                if(today.before(s.date)) {
                    learnerHasFinishedSessions = false;
                    break;
                }
            }
            
            if(learnerHasFinishedSessions && !sessions.isEmpty()) {
                Learner.deleteLearnerAndTheirSchedule(learner);
            }
        }
        return status(NO_CONTENT);
    }

    /**
     * Deletes finished learners, their sessions, then returns the CSV of deleted learners.
     */
    public static Result removeLearnersAndGiveCSV() {
        Result r = generateLearnersCSV();
        removeFinishedLearnersAndTheirSessions();
        return r;
    }
}
