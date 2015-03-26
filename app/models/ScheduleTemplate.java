package models;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import play.data.validation.Constraints.*;
import javax.persistence.*;

import java.util.List;
import java.util.ArrayList;

@Entity
public class ScheduleTemplate extends Model {

	@Id
	public String title;
	
	//@Required
	//public Date starts_at;
	
	@Required
	public List<SessionTemplate> sessions;
	
	public ScheduleTemplate(String title){
		this.title = title;
		sessions = new ArrayList<SessionTemplate>();
	}
	
	public boolean addSession(SessionTemplate session){
		if(!sessions.contains(session))
			return sessions.add(session);
		else return false;
	}
	
	public boolean deleteSession(SessionTemplate session){
		return sessions.remove(session);
	}
	
	public static void create(String title){
		ScheduleTemplate st = new ScheduleTemplate(title);
		st.save();
	}
	
	 public static Finder<String, ScheduleTemplate> find = new Finder<String, ScheduleTemplate>(
	            String.class, ScheduleTemplate.class);
}
