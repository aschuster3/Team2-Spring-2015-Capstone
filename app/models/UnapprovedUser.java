package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.EmailValidator;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import util.PhoneNumberFormatter;

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

    @Required
    public String phoneNumber;

    public String token;


    public UnapprovedUser(String firstName, String lastName, String email, String department) {
        this(firstName, lastName, email, department, "555-123-4567");
    }

    public UnapprovedUser(
            String firstName, String lastName, String email,
            String department, String phoneNumber) {
        this.email = email;
        this.department = department;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    public static Finder<String, UnapprovedUser> find = new Finder<String, UnapprovedUser>(
            String.class, UnapprovedUser.class);
    
    public static void create(UnapprovedUser user) {
        user.phoneNumber = PhoneNumberFormatter.safeTransformToCommonFormat(user.phoneNumber);
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

        if (!PhoneNumberFormatter.isValidNumber(this.phoneNumber)) {
            if (PhoneNumberFormatter.isMissingAreaCode(this.phoneNumber)) {
                return "Invalid phone number (area code is required).";
            } else {
                return "Invalid phone number.";
            }
        }
        
        return null;
    }

}
