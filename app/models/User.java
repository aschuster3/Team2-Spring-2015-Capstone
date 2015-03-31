package models;

import java.util.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import util.PhoneNumberFormatter;

@Entity
@SuppressWarnings("serial")
/**
 * This is the general user class.  All users have capabilities to login, but 
 * depending on the user's designation, they will either have administration
 * privileges or coordinator privileges.
 * 
 * @author Andrew Schuster
 */
public class User extends Model {

    @Required
    @Email
    @Id
    public String email;
    
    @Required
    public String password;
    
    @Required
    public boolean isAdmin;
    
    @Required
    public String firstName;
    
    @Required
    public String lastName;

    @Required
    public String phoneNumber;
    
    public String department;

    /*
    @OneToMany
    public List<Learner> learners;
    */
    
    public User(String firstName, String lastName, String email, String password, boolean isAdmin) {
        this(firstName, lastName, email, password, isAdmin, "unknown dept", "555-123-4567");
    }

    public User(
            String firstName, String lastName, String email, String password,
            boolean isAdmin, String department, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isAdmin = isAdmin;
        this.department = department;
        this.phoneNumber = phoneNumber;
        //this.learners = new ArrayList<Learner>();
    }

    public static Finder<String, User> find = new Finder<String, User>(
            String.class, User.class);
    
    public static User authenticate(String email, String password) {
        return find.where().eq("email", email)
               .eq("password", password).findUnique();
    }
    
    /**
     * For convenience to add encryption later
     * 
     * @param newPassword New password for account
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.save();
    }
    
    public static void create(User user) {
        user.phoneNumber = PhoneNumberFormatter.safeTransformToCommonFormat(user.phoneNumber);
        user.save();
    }
    
    public static List<User> getAll() {
        return find.all();
    }
    
    public static List<User> getAllCoordinators() {
        return find.where().eq("isAdmin", false).findList();
    }
    
    public String validate() {
        User user = User.find.where().eq("email", this.email).findUnique();
        if (user != null) {
            return "Email " + user.email + " is already taken.";
        }
        
        return null;
    }
}
