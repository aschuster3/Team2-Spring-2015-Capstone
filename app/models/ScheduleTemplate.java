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
	public String learnerType;
	
	@Required
	@OneToMany(mappedBy="schedule", cascade=CascadeType.ALL)
	public List<SessionTemplate> sessions;
	
	public ScheduleTemplate(String title, String learnerType){
		this.title = title;
		this.learnerType = learnerType;
		this.sessions = new ArrayList<SessionTemplate>();
	}
	
	public boolean addSession(SessionTemplate session){
		if(!sessions.contains(session)){
			return sessions.add(session);
		}
		else return false;
	}
	
	public boolean deleteSession(SessionTemplate session){
		boolean result = sessions.remove(session);
		if(result){
			return SessionTemplate.delete(session);
		}
		return result;
	}
	
	public static void create(String title, String learnerType){
		ScheduleTemplate st = new ScheduleTemplate(title, learnerType);
		st.save();
	}
	
	public boolean updateTitle(String title){
		if(ScheduleTemplate.find.byId(title) == null){
			this.title = title;
			this.save();
			return true;
		}
		return false;
	}
	
	public void updateLearnerType(String learnerType){
		this.learnerType = learnerType;
		this.save();
	}
	
	 public static Finder<String, ScheduleTemplate> find = new Finder<String, ScheduleTemplate>(
	            String.class, ScheduleTemplate.class);
}
