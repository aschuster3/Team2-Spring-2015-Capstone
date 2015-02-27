package models;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import play.data.validation.Constraints.*;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

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
    
    public Session(String id, String sessionName, String startTime, String endTime, String isFree){
    	this.id = id;
    	this.sessionName = sessionName;
    	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    	
    	try{
    		this.startTime = formatter.parse(startTime);
    		this.endTime = formatter.parse(endTime);
    	}
    	catch(Exception e){
    		
    	}
    	if(isFree.equalsIgnoreCase("true")||isFree.equalsIgnoreCase("yes")) this.isFree = true;
    	else this.isFree = false;
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
