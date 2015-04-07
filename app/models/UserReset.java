package models;

import javax.persistence.*;

import play.data.validation.Constraints.*;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@SuppressWarnings("serial")
public class UserReset extends Model {
    
    @Id
    public String userEmail;
    
    @Required
    public String resetToken;
    
    public static Finder<String, UserReset> find = new Finder<String, UserReset>(
            String.class, UserReset.class);
    
    public UserReset(String userEmail, String resetToken) {
        this.userEmail = userEmail;
        this.resetToken = resetToken;
    }

    /**
     * If a UserReset already exists for this userEmail, update the token.
     * Otherwise, create a new UserReset.
     */
    public static void create(String userEmail, String resetToken) {
        UserReset ur = new UserReset(userEmail, resetToken);
        if (UserReset.find.byId(userEmail) == null) {
            ur.save();
        } else {
            ur.update();
        }
    }

}
