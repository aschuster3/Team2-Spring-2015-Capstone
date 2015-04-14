package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.databind.JsonNode;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.libs.Json;

@Entity
@SuppressWarnings("serial")
public class Learner extends Model {

    public static final List<String> LEARNER_TYPES = Arrays.asList(
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

    @Column(unique=true)
    public String uuid;

    public String firstName;

    public String lastName;

    public String ownerEmail;

    public String learnerType;

    public static Finder<String, Learner> find = new Finder<String, Learner>(
            String.class, Learner.class);

    public Learner(String email, String first, String last, String ownerEmail, String learnerType) {
        this.email = email;
        this.firstName = first;
        this.lastName = last;
        this.ownerEmail = ownerEmail;
        this.learnerType = learnerType;
        this.uuid = UUID.randomUUID().toString();
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

    public static void deleteLearnerButFreeTheirSessions(Learner learner) {
        List<Session> schedule = Session.getLearnerSchedule(learner.email);
        for (Session session: schedule) {
            session.assignedLearner = null;
        }
        learner.delete();
    }
    
    public static List<Learner> getAll() {
        return Learner.find.orderBy("lastName, firstName").findList();
    }

    public static List<Learner> getAllOrderByType() {
        return Learner.find.orderBy("learnerType, lastName, firstName").findList();
    }
    
    public static List<Learner> getAllOwnedBy(String ownerEmail) {
        return Learner.find.where().eq("ownerEmail", ownerEmail).orderBy("lastName, firstName").findList();
    }

    /**
     * This is Learner validation for Learner updates
     * which are not passed through any kind of
     * form constraints or validation.
     *
     * @return null on success, or an error message.
     */
    public String validate() {
        JsonNode json = Json.toJson(this);
        Form<PreLearner> preLearnerForm = new Form<>(PreLearner.class);
        preLearnerForm = preLearnerForm.bind(json);

        if (preLearnerForm.hasGlobalErrors()) {
            return preLearnerForm.globalError().message();
        }

        if (preLearnerForm.hasErrors()) {
            return "Missing required field.";
        }

        return null;
    }

    public PreLearner toPreLearner() {
        PreLearner preLearner = new PreLearner();
        preLearner.email = this.email;
        preLearner.firstName = this.firstName;
        preLearner.lastName = this.lastName;
        preLearner.learnerType = this.learnerType;
        return preLearner;
    }

    public static class PreLearner {
        @Required
        public String email;

        @Required
        public String firstName;

        @Required
        public String lastName;

        @Required
        public String learnerType;

        public String validate() {

            if((firstName == null && !firstName.equals("")) || (lastName == null && !lastName.equals(""))) {
                return "Must have a name for the student.";
            }

            Constraints.EmailValidator val = new Constraints.EmailValidator();
            if(!val.isValid(email)) {
                return "The email address is not valid.";
            }

            return null;
        }
    }
}
