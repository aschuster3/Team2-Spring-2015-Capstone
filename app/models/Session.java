package models;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import play.data.validation.Constraints.*;

import java.util.UUID;

import javax.persistence.*;

import play.db.ebean.Model;


@Entity
@SuppressWarnings("serial")
/**
 * This is the class for sessions to be added to the calendar.
 * Admins may create sessions. Both admins and users can view sessions.
 * 
 * @author Julia Rapoport
 */
public class Session extends Model {

	/* for the front-end calendar */
	public static final String TYPE_TAKEN = "invalid";
	public static final String TYPE_FREE = "info";

    @Id
    public String id;

    @Required
    public String title;
    
    @Required
    public Date starts_at;
    
    @Required
    public Date ends_at;

	@ManyToOne
    public Learner assignedLearner;

	@Transient  // only for front-end Javascript objects
	private String type;
    
    public Session(String id, String title, Date starts_at, Date ends_at){
    	this.id = id;
    	this.title = title;
    	this.starts_at = starts_at;
    	this.ends_at = ends_at;
    }
    
    public Session(String id, String title, String starts_at, String ends_at){
    	this.id = id;
    	this.title = title;
    	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    	
    	try{
    		this.starts_at = formatter.parse(starts_at);
    		this.ends_at = formatter.parse(ends_at);
    	}
    	catch(Exception e){
    		
    	}
    }	
    
    public Session(String title, Date starts_at, Date ends_at){
    	this.id = UUID.randomUUID().toString();
    	this.title = title;
    	this.starts_at = starts_at;
    	this.ends_at = ends_at;
    }

    public static Finder<String, Session> find = new Finder<String, Session>(
            String.class, Session.class);
    
    public static void create(Session session) {
    	session.save();
    }

    public static List<Session> getAll() {
        return Session.find.all();
    }
    
    public String validate() {
    	Session session = Session.find.byId(this.id);
    	if (session != null) {
    		return "Session " + this.id + " already exists.";
    	}
    	return null;
    }

	private boolean isFree() {
		return this.assignedLearner == null;
	}

	public String getType() {
		return this.isFree() ? TYPE_FREE : TYPE_TAKEN;
	}
}
