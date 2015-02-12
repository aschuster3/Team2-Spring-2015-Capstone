package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.EmailValidator;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
@SuppressWarnings("serial")
/**
 * This is the class for users who have registered but are not yet approved by 
 * system administrator. Unapproved users do not have the ability to login.
 * 
 * @author Julia Rapoport
 */
public class UnapprovedUser extends Model {
    
    @Required
    @Email
    @Id
    public String email;
    
    @Required
    public String department;
    
    @Required
    public String firstName;
   
    @Required
    public String lastName;

    public String token;
    
    public UnapprovedUser(String firstName, String lastName, String email, String department) {
        this.email = email;
        this.department = department;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static Finder<String, UnapprovedUser> find = new Finder<String, UnapprovedUser>(
            String.class, UnapprovedUser.class);
    
    public static void create(UnapprovedUser user) {
        // TODO: Move this to a place where it will be generated after approval!
        user.token = UUID.randomUUID().toString();
        
        user.save();
    }
    
    public static List<UnapprovedUser> getAll() {
        return UnapprovedUser.find.all();
    }
    
    public String validate() {
    	UnapprovedUser unapprovedUser = UnapprovedUser.find.byId(this.email);
    	User user = User.find.byId(this.email);
        if (unapprovedUser != null || user != null) {
            return "Email " + this.email + " is already taken.";
        }
        EmailValidator val = new EmailValidator();
        if(!val.isValid(this.email)) {
            return "The email address is not valid.";
        }
        
        return null;
    }

}
