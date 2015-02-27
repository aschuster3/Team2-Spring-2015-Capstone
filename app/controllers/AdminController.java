package controllers;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import models.UnapprovedUser;
import models.User;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import views.html.coordinatorsPage;
import views.html.testViewUnapprovedUsers;

@With(SecuredAdminAction.class)
public class AdminController extends Controller {

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
            return redirect(routes.AdminController.viewAllCoordinators());
        } catch (MalformedURLException e) {
            return badRequest(views.html.coordinatorsPage.render(
                    UnapprovedUser.getAll(),
                    User.getAllCoordinators()));
        }
    }
    
    /**
     * This is a temporary test action to view current UnapprovedUsers
     * 
     * This should be replaced by the admin capabilities
     * 
     * @return
     */
    public static Result fetchUU() {
        // TODO: replace with an admin template
        return ok(testViewUnapprovedUsers.render(UnapprovedUser.getAll()));
    }
}
