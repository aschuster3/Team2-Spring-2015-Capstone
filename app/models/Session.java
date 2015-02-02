package models;

import java.util.Date;

import play.db.ebean.*;
import play.data.validation.Constraints.*;

import javax.persistence.*;

@Entity
@SuppressWarnings("serial")
public class Session extends Model {

    @Id
    public Long id;

    @Required
    public String sessionName;
    
    @Required
    public Date startTime;
    
    @Required
    public Date endTime;
    
    public static Finder<Long, Session> find = new Finder<Long, Session>(
            Long.class, Session.class);

}
