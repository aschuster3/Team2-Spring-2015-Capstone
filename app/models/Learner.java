package models;

import java.util.ArrayList;

import play.db.ebean.*;
import play.data.validation.Constraints.*;

import javax.persistence.*;

@Entity
@SuppressWarnings("serial")
public class Learner extends Model {

    @Id
    public Long id;

    @Required
    public String fullname;
    
    @Required
    public ArrayList<Session> sessions;

    public static Finder<Long, Learner> find = new Finder<Long, Learner>(
            Long.class, Learner.class);

}
