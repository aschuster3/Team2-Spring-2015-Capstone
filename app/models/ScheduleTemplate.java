package models;

import play.db.ebean.Model;
import play.data.validation.Constraints.*;
import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ScheduleTemplate extends Model {

	@Id
	public String title;
	
	@Required
	@OneToMany(mappedBy="schedule", cascade=CascadeType.ALL)
	public List<SessionTemplate> sessions;
	
	public ScheduleTemplate(String title){
		this.title = title;
		this.sessions = new ArrayList<SessionTemplate>();
	}
	
	public boolean addSession(SessionTemplate session){
		if(!sessions.contains(session)){
			return sessions.add(session);
		}
		else return false;
	}
	
	public boolean deleteSession(SessionTemplate session){
		return sessions.remove(session);
	}
	
	public static void create(String title){
		ScheduleTemplate st = new ScheduleTemplate(title);
		st.save();
	}
	
	//NEED TO ADD AN UPDATE FOR SCHEDULE
	
	 public static Finder<String, ScheduleTemplate> find = new Finder<String, ScheduleTemplate>(
	            String.class, ScheduleTemplate.class);
}
