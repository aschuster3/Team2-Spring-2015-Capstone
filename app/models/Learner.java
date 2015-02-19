package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
@SuppressWarnings("serial")
public class Learner extends Model {

    @Id
    public Long id;

    @Required
    public String firstName;
    
    @Required
    public String lastName;
    
    @Required
    public List<Session> sessions;
    
    @Required
    @ManyToOne
    public User owner;

    public static Finder<Long, Learner> find = new Finder<Long, Learner>(
            Long.class, Learner.class);
    
    public Learner(String first, String last, User owner) {
        this.firstName = first;
        this.lastName = last;
        this.owner = owner;
        this.sessions = new ArrayList<>();
    }
    
    public static void create(String firstName, String lastName, String ownerEmail) {
        User owner = User.find.ref(ownerEmail);
        Learner newLearner = new Learner(firstName, lastName, owner);
        newLearner.save();
    }
    
    public static List<Learner> getAll() {
        return Learner.find.all();
    }
    
    public static List<Learner> getAllOwnedBy(String ownerEmail) {
        return Learner.find.where().eq("owner.email", ownerEmail).findList();
    }
}
