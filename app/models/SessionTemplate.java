package models;
import java.util.UUID;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import play.data.validation.Constraints.*;
import javax.persistence.*;

/**
 * This is a class for generating templates of sessions. Only admins can create
 * session templates. Session templates are added to schedule templates.
 * 
 * @author Julia Rapoport
 *
 */
@Entity
public class SessionTemplate extends Model implements Comparable<SessionTemplate> {

	@Id
	public String id;
	
	@Required
	public String location;
	
	@Required
	public String physician;
	
	@Required
	public int week;
	
	@Required
	public int day;
	
	@Required
	public boolean isAM;
	
	@ManyToOne
	public ScheduleTemplate schedule;
			
	public SessionTemplate(String location, String physician, int week, int day, boolean isAM){
    	this.id = UUID.randomUUID().toString();
    	this.location = location;
		this.physician = physician;
		this.week = week;
		this.day = day;
		this.isAM = isAM;
	}
	
	public SessionTemplate(String location, String physician, String week, String day, String isAM){
    	this.id = UUID.randomUUID().toString();
    	this.location = location;
		this.physician = physician;
		this.week = (int)Integer.valueOf(week);
		this.day = Integer.valueOf(day);
		this.isAM = (isAM.equalsIgnoreCase("true")||isAM.equalsIgnoreCase("yes")||isAM.equalsIgnoreCase("AM"));
	}
	
	public static void create(String location, String physician, String week, String day, String isAM){
		SessionTemplate st = new SessionTemplate(location, physician, week, day, isAM);
		st.save();
	}
	
	public static SessionTemplate create(String location, String physician, int week, int day, boolean isAM){
		SessionTemplate st = new SessionTemplate(location, physician, week, day, isAM);
		st.save();
		return st;
	}
	
	public static SessionTemplate create(String location, String physician, int week, int day, boolean isAM, String schedule){
		ScheduleTemplate st = ScheduleTemplate.find.byId(schedule);
		if (st == null){
			return null;
		}
		SessionTemplate session = new SessionTemplate(location, physician, week, day, isAM);
		session.save();
		return session;
	}
	
	public static Boolean delete(SessionTemplate st){
		if(SessionTemplate.find.byId(st.id)!=null){
			st.delete();
			return true;
		}
		return false;
	}
	
	public void updateLocation(String location){
		this.location = location;
		this.save();
	}
	
	public void updatePhysician(String physician){
		this.physician = physician;
		this.save();
	}
	
	public void updateWeek(int week){
		this.week = week;
		this.save();
	}
	
	public void updateDay(int day){
		this.day = day;
		this.save();
	}
	
	public void updateAM(boolean isAM){
		this.isAM = isAM;
		this.save();
	}
		
    public static Finder<String, SessionTemplate> find = new Finder<String, SessionTemplate>(
            String.class, SessionTemplate.class);

	@Override
	public int compareTo(SessionTemplate other) {
		if (this == other) {
			return 0;
		}

		if (this.week < other.week) {
			return -1;
		} else if (this.week > other.week) {
			return 1;
		}

		// occurs on same week
		if (this.day < other.day) {
			return -1;
		} else if (this.day > other.day) {
			return 1;
		}

		// occurs on same week and day
		if (this.isAM && !other.isAM) {
			return -1;
		} else if (!this.isAM && other.isAM) {
			return 1;
		} else {
			return 0;
		}
	}
}
