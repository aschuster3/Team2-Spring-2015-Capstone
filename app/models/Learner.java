package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
@SuppressWarnings("serial")
public class Learner extends Model {

    public static final List<String> LEARNER_TYPES = Arrays.asList(
            "Sub-I",
            "Sub-I Medical Student",
            "Ambulatory Medical Student",
            "Dermatology Resident",
            "Pediatrics Resident",
            "Emory Internal Medicine",
            "Morehouse Internal Medicine",
            "Family Medicine",
            "Podiatry Resident",
            "Geriatrics Resident",
            "Rheumatology Resident",
            "Nurse Practitioner Student",
            "Physician Assistant Student",
            "Pediatrics Allergy Fellow",
            "International Student",
            "Pre-Med Student"
    );

    @Id
    public String email;

    @Required
    public String firstName;
    
    @Required
    public String lastName;
    
    @Required
    @ManyToOne
    public String ownerEmail;

    @Required
    public String learnerType;

    public static Finder<String, Learner> find = new Finder<String, Learner>(
            String.class, Learner.class);

    public Learner(String email, String first, String last, String ownerEmail, String learnerType) {
        this.email = email;
        this.firstName = first;
        this.lastName = last;
        this.ownerEmail = ownerEmail;
        this.learnerType = learnerType;
    }

    public Learner(String email, String first, String last, String ownerEmail) {
        this(email, first, last, ownerEmail, "");
    }

    public static void create(String email, String firstName, String lastName, String learnerType, String ownerEmail) {
        Learner newLearner = new Learner(email, firstName, lastName, ownerEmail, learnerType);
        newLearner.save();
    }

    /**
     * Delete cascades to registered Sessions.
     */
    public static void deleteLearnerAndTheirSchedule(Learner learner) {
        List<Session> schedule = Session.getLearnerSchedule(learner.email);
        for (Session session: schedule) {
            session.delete();
        }
        learner.delete();
    }
    
    public static List<Learner> getAll() {
        return Learner.find.orderBy("lastName, firstName").findList();
    }
    
    public static List<Learner> getAllOwnedBy(String ownerEmail) {
        return Learner.find.where().eq("ownerEmail", ownerEmail).orderBy("lastName, firstName").findList();
    }
}
