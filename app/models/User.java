package models;

import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

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
    @Id
    public String email;
    
    @Required
    public String password;
    
    @Required
    public boolean isAdmin;
    
    @Required
    public String fullname;

    @Required
    @OneToMany
    public ArrayList<Learner> learners;
    
    public User(String email, String password, String fullname, boolean isAdmin) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.isAdmin = isAdmin;
        this.learners = new ArrayList<Learner>();
    }

    public static Finder<Long, User> find = new Finder<Long, User>(
            Long.class, User.class);
    
    public static User authenticate(String email, String password) {
        return find.where().eq("email", email)
               .eq("password", password).findUnique();
    }
}
