package models;

import java.util.Date;
import java.util.List;

import play.db.ebean.*;
import play.data.validation.Constraints.*;

import javax.persistence.*;

import java.util.UUID;

@Entity
@SuppressWarnings("serial")
public class Session extends Model {

    @Id
    public String id;

    @Required
    public String sessionName;
    
    @Required
    public Date startTime;
    
    @Required
    public Date endTime;
    
    @Required
    public Boolean isFree; 
    
    public Session(String id, String sessionName, Date startTime, Date endTime, Boolean isFree){
    	this.id = id;
    	this.sessionName = sessionName;
    	this.startTime = startTime;
    	this.endTime = endTime;
    	this.isFree = isFree;
    }
    
    public Session(String sessionName, Date startTime, Date endTime){
    	this.id = UUID.randomUUID().toString();
    	this.sessionName = sessionName;
    	this.startTime = startTime;
    	this.endTime = endTime;
    	this.isFree = true;
    }
    public static Finder<String, Session> find = new Finder<String, Session>(
            String.class, Session.class);
    
    public static void create(Session session){
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

}
